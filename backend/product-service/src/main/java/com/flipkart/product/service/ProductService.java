package com.flipkart.product.service;

import com.flipkart.product.dto.Dtos.*;
import com.flipkart.product.exception.ApiException;
import com.flipkart.product.model.Product;
import com.flipkart.product.model.Review;
import com.flipkart.product.repository.ProductRepository;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class ProductService {

    /** Display order of the top navigation categories, same as Flipkart's category strip. */
    private static final List<String> CATEGORY_ORDER =
            List.of("grocery", "mobiles", "fashion", "electronics", "home", "beauty", "two-wheelers");

    private final ProductRepository repository;
    private final MongoTemplate mongo;

    public ProductService(ProductRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    // ------------------------------------------------------------------ browse & search

    public record SearchParams(String q, String category, List<String> subCategories, List<String> brands,
                               Integer minPrice, Integer maxPrice, Double minRating, Integer minDiscount,
                               boolean assuredOnly, boolean inStockOnly, String sort, int page, int size) {}

    public PageResponse<Product> search(SearchParams p) {
        Criteria criteria = baseCriteria(p.q(), p.category());
        List<Criteria> filters = new ArrayList<>();
        if (p.subCategories() != null && !p.subCategories().isEmpty()) filters.add(Criteria.where("subCategory").in(p.subCategories()));
        if (p.brands() != null && !p.brands().isEmpty()) filters.add(Criteria.where("brand").in(p.brands()));
        if (p.minPrice() != null || p.maxPrice() != null) {
            Criteria price = Criteria.where("price");
            if (p.minPrice() != null) price = price.gte(p.minPrice());
            if (p.maxPrice() != null) price = price.lte(p.maxPrice());
            filters.add(price);
        }
        if (p.minRating() != null) filters.add(Criteria.where("rating").gte(p.minRating()));
        if (p.minDiscount() != null) filters.add(Criteria.where("discount").gte(p.minDiscount()));
        if (p.assuredOnly()) filters.add(Criteria.where("assured").is(true));
        if (p.inStockOnly()) filters.add(Criteria.where("stock").gt(0));
        if (!filters.isEmpty()) {
            filters.add(0, criteria);
            criteria = new Criteria().andOperator(filters.toArray(new Criteria[0]));
        }

        int size = Math.min(Math.max(p.size(), 1), 60);
        int page = Math.max(p.page(), 0);
        Query query = new Query(criteria);
        long total = mongo.count(query, Product.class);

        query.with(sortFor(p.sort())).skip((long) page * size).limit(size);
        query.fields().exclude("reviews", "description", "specifications");
        List<Product> content = mongo.find(query, Product.class);
        return new PageResponse<>(content, total, (int) Math.ceil(total / (double) size), page, size);
    }

    /** Brands / sub-categories / price range for the filter sidebar of the current listing. */
    public Facets facets(String q, String category) {
        Criteria base = baseCriteria(q, category);
        List<Facet> brands = groupCount(base, "brand");
        List<Facet> subCategories = groupCount(base, "subCategory");

        Aggregation range = newAggregation(match(base),
                group().min("price").as("min").max("price").as("max"));
        Document r = mongo.aggregate(range, Product.class, Document.class).getUniqueMappedResult();
        int min = r == null ? 0 : ((Number) r.get("min")).intValue();
        int max = r == null ? 0 : ((Number) r.get("max")).intValue();
        return new Facets(brands, subCategories, min, max);
    }

    public List<Category> categories() {
        Aggregation agg = newAggregation(
                sort(Sort.by(Sort.Direction.DESC, "ratingCount")),
                group("category").first("categoryName").as("name").count().as("count").first("thumbnail").as("image")
                        .addToSet("subCategory").as("subCategories"));
        List<Document> docs = mongo.aggregate(agg, Product.class, Document.class).getMappedResults();
        return docs.stream()
                .map(d -> new Category(d.getString("_id"), d.getString("name"),
                        ((Number) d.get("count")).longValue(), d.getString("image"),
                        d.getList("subCategories", String.class).stream().sorted().toList()))
                .sorted(Comparator.comparingInt(c -> {
                    int i = CATEGORY_ORDER.indexOf(c.slug());
                    return i < 0 ? 99 : i;
                }))
                .toList();
    }

    public List<Suggestion> suggest(String q) {
        if (!StringUtils.hasText(q)) return List.of();
        Query query = new Query(baseCriteria(q, null))
                .with(Sort.by(Sort.Direction.DESC, "ratingCount")).limit(8);
        query.fields().include("title", "category", "categoryName", "subCategory", "thumbnail");
        List<Product> products = mongo.find(query, Product.class);

        List<Suggestion> result = new ArrayList<>();
        // Flipkart shows "<query> in <Category>" rows first.
        products.stream().map(Product::getSubCategory).distinct().limit(2).forEach(sub -> {
            Product first = products.stream().filter(p -> sub.equals(p.getSubCategory())).findFirst().orElseThrow();
            result.add(new Suggestion("category", null, sub, first.getCategory(), null));
        });
        products.forEach(p -> result.add(new Suggestion("product", p.getId(), p.getTitle(), p.getCategory(), p.getThumbnail())));
        return result;
    }

    public Product get(String id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Product not found: " + id));
    }

    public List<Product> similar(String id) {
        Product p = get(id);
        return repository.findTop12ByCategoryAndIdNotOrderByRatingDesc(p.getCategory(), id).stream()
                .peek(x -> { x.setReviews(List.of()); x.setDescription(null); })
                .toList();
    }

    /** Used by cart, order and wishlist to look up many products at once. Preserves request order. */
    public List<Product> batch(List<String> ids) {
        if (ids.isEmpty()) return List.of();
        Query query = new Query(Criteria.where("_id").in(ids));
        query.fields().exclude("reviews", "description", "specifications");
        Map<String, Product> byId = new HashMap<>();
        mongo.find(query, Product.class).forEach(p -> byId.put(p.getId(), p));
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }

    // ------------------------------------------------------------------ reviews

    public Product addReview(String productId, String userId, String userName, ReviewRequest req) {
        Product p = get(productId);
        p.getReviews().removeIf(r -> userId.equals(r.getUserId()));
        p.getReviews().add(0, new Review(userId, userName, req.rating(), req.title(), req.comment(), Instant.now().toString()));

        // Blend the new rating into the aggregate the way a marketplace would.
        double total = p.getRating() * p.getRatingCount() + req.rating();
        p.setRatingCount(p.getRatingCount() + 1);
        p.setReviewCount(p.getReviewCount() + 1);
        p.setRating(Math.round(total / p.getRatingCount() * 10) / 10.0);
        p.setAssured(p.getRating() >= 4.0);
        return repository.save(p);
    }

    // ------------------------------------------------------------------ seller / admin

    public Product create(ProductRequest req) {
        Product p = new Product();
        p.setId("FKS" + Long.toString(System.currentTimeMillis(), 36).toUpperCase());
        apply(p, req);
        p.setRating(0);
        p.setRatingCount(0);
        p.setReviewCount(0);
        p.setCreatedAt(Instant.now());
        return repository.save(p);
    }

    public Product update(String id, ProductRequest req) {
        Product p = get(id);
        apply(p, req);
        return repository.save(p);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) throw ApiException.notFound("Product not found: " + id);
        repository.deleteById(id);
    }

    public Product updateStock(String id, int stock) {
        Product p = get(id);
        p.setStock(stock);
        return repository.save(p);
    }

    // ------------------------------------------------------------------ inventory (called by order-service)

    /**
     * Reserves stock for every line or none of them. Each decrement is a single atomic Mongo update
     * guarded by "stock >= quantity", so two concurrent orders can never oversell.
     */
    public void reserve(List<StockLine> lines) {
        List<StockLine> done = new ArrayList<>();
        for (StockLine line : lines) {
            Query q = new Query(Criteria.where("_id").is(line.productId()).and("stock").gte(line.quantity()));
            long modified = mongo.updateFirst(q, new Update().inc("stock", -line.quantity()), Product.class).getModifiedCount();
            if (modified == 0) {
                release(done);
                Product p = repository.findById(line.productId()).orElse(null);
                if (p == null) throw ApiException.notFound("Product no longer available: " + line.productId());
                throw ApiException.conflict(p.getStock() == 0
                        ? p.getTitle() + " is out of stock"
                        : "Only " + p.getStock() + " unit(s) of " + p.getTitle() + " left in stock");
            }
            done.add(line);
        }
    }

    public void release(List<StockLine> lines) {
        for (StockLine line : lines) {
            mongo.updateFirst(new Query(Criteria.where("_id").is(line.productId())),
                    new Update().inc("stock", line.quantity()), Product.class);
        }
    }

    // ------------------------------------------------------------------ helpers

    private Criteria baseCriteria(String q, String category) {
        List<Criteria> parts = new ArrayList<>();
        if (StringUtils.hasText(category)) parts.add(Criteria.where("category").is(category));
        if (StringUtils.hasText(q)) {
            // Every word must match at least one searchable field.
            for (String word : q.trim().toLowerCase().split("\\s+")) {
                Pattern pattern = Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE);
                parts.add(new Criteria().orOperator(
                        Criteria.where("title").regex(pattern),
                        Criteria.where("brand").regex(pattern),
                        Criteria.where("subCategory").regex(pattern),
                        Criteria.where("categoryName").regex(pattern),
                        Criteria.where("tags").regex(pattern)));
            }
        }
        if (parts.isEmpty()) return new Criteria();
        if (parts.size() == 1) return parts.get(0);
        return new Criteria().andOperator(parts.toArray(new Criteria[0]));
    }

    private List<Facet> groupCount(Criteria base, String field) {
        Aggregation agg = newAggregation(match(base), group(field).count().as("count"),
                sort(Sort.by(Sort.Direction.DESC, "count").and(Sort.by("_id"))));
        return mongo.aggregate(agg, Product.class, Document.class).getMappedResults().stream()
                .filter(d -> d.get("_id") != null)
                .map(d -> new Facet(d.get("_id").toString(), ((Number) d.get("count")).longValue()))
                .toList();
    }

    private Sort sortFor(String sort) {
        if (sort == null) sort = "relevance";
        Sort primary = switch (sort) {
            case "popularity" -> Sort.by(Sort.Direction.DESC, "ratingCount");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "discount" -> Sort.by(Sort.Direction.DESC, "discount");
            default -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "ratingCount"));
        };
        // Tie-break on id so pagination is stable.
        return primary.and(Sort.by("_id"));
    }

    private void apply(Product p, ProductRequest req) {
        p.setTitle(req.title());
        p.setBrand(req.brand());
        p.setDescription(req.description());
        p.setCategory(req.category());
        p.setCategoryName(req.categoryName());
        p.setSubCategory(req.subCategory());
        p.setMrp(req.mrp());
        p.setPrice(req.price());
        p.recalculateDiscount();
        p.setStock(req.stock());
        p.setThumbnail(req.thumbnail());
        p.setImages(req.images() == null || req.images().isEmpty() ? List.of(req.thumbnail()) : req.images());
        if (req.highlights() != null) p.setHighlights(req.highlights());
        if (req.specifications() != null) p.setSpecifications(req.specifications());
        p.setWarranty(req.warranty());
        p.setReturnPolicy(req.returnPolicy());
    }
}
