#!/usr/bin/env python3
"""
Builds the product seed ("stock") file used by product-service.

Source catalogue: https://dummyjson.com/products (free demo data).
- Converts USD prices to INR and derives MRP / selling price / discount like Flipkart.
- Maps categories onto Flipkart's top-level navigation.
- Downloads every product image into frontend/public/images/products so the app works offline.

Usage:  python3 backend/tools/build_seed.py <path-to-dummyjson-products.json>
"""
import json, os, sys, subprocess, urllib.parse, concurrent.futures, math

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SEED_OUT = os.path.join(ROOT, "backend", "product-service", "src", "main", "resources", "data", "products.json")
IMG_DIR = os.path.join(ROOT, "frontend", "public", "images", "products")

CATEGORY_MAP = {
    "smartphones": ("mobiles", "Mobiles"),
    "laptops": ("electronics", "Electronics"),
    "tablets": ("electronics", "Electronics"),
    "mobile-accessories": ("electronics", "Electronics"),
    "mens-shirts": ("fashion", "Fashion"),
    "mens-shoes": ("fashion", "Fashion"),
    "mens-watches": ("fashion", "Fashion"),
    "womens-dresses": ("fashion", "Fashion"),
    "womens-shoes": ("fashion", "Fashion"),
    "womens-watches": ("fashion", "Fashion"),
    "womens-bags": ("fashion", "Fashion"),
    "womens-jewellery": ("fashion", "Fashion"),
    "tops": ("fashion", "Fashion"),
    "sunglasses": ("fashion", "Fashion"),
    "furniture": ("home", "Home & Furniture"),
    "home-decoration": ("home", "Home & Furniture"),
    "kitchen-accessories": ("home", "Home & Furniture"),
    "groceries": ("grocery", "Grocery"),
    "beauty": ("beauty", "Beauty, Toys & More"),
    "fragrances": ("beauty", "Beauty, Toys & More"),
    "skin-care": ("beauty", "Beauty, Toys & More"),
    "sports-accessories": ("beauty", "Beauty, Toys & More"),
    "motorcycle": ("two-wheelers", "Two Wheelers"),
}

SUB_NAMES = {
    "smartphones": "Smartphones", "laptops": "Laptops", "tablets": "Tablets",
    "mobile-accessories": "Mobile Accessories", "mens-shirts": "Men's Shirts",
    "mens-shoes": "Men's Footwear", "mens-watches": "Men's Watches",
    "womens-dresses": "Women's Dresses", "womens-shoes": "Women's Footwear",
    "womens-watches": "Women's Watches", "womens-bags": "Handbags",
    "womens-jewellery": "Jewellery", "tops": "Women's Tops", "sunglasses": "Sunglasses",
    "furniture": "Furniture", "home-decoration": "Home Decor",
    "kitchen-accessories": "Kitchen & Dining", "groceries": "Grocery Staples",
    "beauty": "Makeup", "fragrances": "Fragrances", "skin-care": "Skin Care",
    "sports-accessories": "Sports & Fitness", "motorcycle": "Motorcycles",
}

USD_TO_INR = 84


def price_ending(value):
    """Round to a retail-looking price ending in 9 (e.g. 1,499 / 12,999)."""
    if value < 100:
        return max(49, int(math.ceil(value / 10.0) * 10) - 1)
    if value < 1000:
        return int(math.ceil(value / 50.0) * 50) - 1
    if value < 20000:
        return int(math.ceil(value / 100.0) * 100) - 1
    return int(math.ceil(value / 1000.0) * 1000) - 1


def price_floor(value, mrp):
    """Selling price: rounded down to a retail ending, always below MRP."""
    step = 10 if value < 1000 else (100 if value < 20000 else 1000)
    price = max(29, int(value // step) * step - 1)
    return price if price < mrp else mrp - step


def download(job):
    url, dest = job
    if os.path.exists(dest) and os.path.getsize(dest) > 0:
        return True
    safe = urllib.parse.quote(url, safe=":/")
    for _ in range(4):
        r = subprocess.run(["curl", "-sSfL", "--max-time", "30", "-o", dest, safe], capture_output=True)
        if r.returncode == 0 and os.path.getsize(dest) > 0:
            return True
    print("FAILED", url, file=sys.stderr)
    return False


def main(src):
    raw = json.load(open(src))["products"]
    products, jobs = [], []
    for p in raw:
        if p["category"] not in CATEGORY_MAP:
            continue
        cat, cat_name = CATEGORY_MAP[p["category"]]
        pid = "FK" + str(p["id"]).zfill(4)
        os.makedirs(os.path.join(IMG_DIR, pid), exist_ok=True)

        images = []
        for i, url in enumerate(p["images"][:5]):
            ext = os.path.splitext(url)[1] or ".webp"
            name = f"{i + 1}{ext}"
            jobs.append((url, os.path.join(IMG_DIR, pid, name)))
            images.append(f"/images/products/{pid}/{name}")
        thumb_ext = os.path.splitext(p["thumbnail"])[1] or ".webp"
        jobs.append((p["thumbnail"], os.path.join(IMG_DIR, pid, "thumb" + thumb_ext)))

        discount = int(min(75, max(5, round(p["discountPercentage"] * 2.4))))
        mrp = price_ending(p["price"] * USD_TO_INR)
        price = price_floor(mrp * (100 - discount) / 100.0, mrp)
        discount = int(round((mrp - price) * 100.0 / mrp))

        rating = round(max(3.1, p["rating"]), 1)
        rating_count = 800 + (p["id"] * 7919) % 48000
        review_count = max(len(p.get("reviews", [])), rating_count // 9)

        dims = p.get("dimensions", {})
        specs = {
            "Brand": p.get("brand") or "Generic",
            "Model Number": p.get("sku", ""),
            "Weight": f'{p.get("weight", 1)} kg',
            "Dimensions": f'{dims.get("width", 0)} x {dims.get("height", 0)} x {dims.get("depth", 0)} cm',
            "Warranty": p.get("warrantyInformation", "No warranty"),
            "Minimum Order Quantity": str(p.get("minimumOrderQuantity", 1)),
        }
        highlights = [
            p.get("warrantyInformation", "Brand warranty"),
            p.get("returnPolicy", "7 days return policy"),
            p.get("shippingInformation", "Ships in 2 days"),
            f'Weight: {p.get("weight", 1)} kg',
        ]

        products.append({
            "id": pid,
            "title": p["title"],
            "brand": p.get("brand") or "Generic",
            "description": p["description"],
            "category": cat,
            "categoryName": cat_name,
            "subCategory": SUB_NAMES[p["category"]],
            "mrp": mrp,
            "price": price,
            "discount": discount,
            "rating": rating,
            "ratingCount": rating_count,
            "reviewCount": review_count,
            "stock": int(p.get("stock", 10)),
            "thumbnail": f"/images/products/{pid}/thumb{thumb_ext}",
            "images": images,
            "highlights": highlights,
            "specifications": specs,
            "warranty": p.get("warrantyInformation", ""),
            "returnPolicy": p.get("returnPolicy", "7 days return policy"),
            "tags": p.get("tags", []),
            "assured": rating >= 4.0,
            "reviews": [
                {"userName": r["reviewerName"], "rating": r["rating"], "comment": r["comment"], "date": r["date"]}
                for r in p.get("reviews", [])
            ],
        })

    os.makedirs(os.path.dirname(SEED_OUT), exist_ok=True)
    with open(SEED_OUT, "w") as f:
        json.dump(products, f, indent=1, ensure_ascii=False)
    print(f"Wrote {len(products)} products to {SEED_OUT}")

    with concurrent.futures.ThreadPoolExecutor(max_workers=12) as ex:
        ok = sum(ex.map(download, jobs))
    print(f"Downloaded {ok}/{len(jobs)} images into {IMG_DIR}")


if __name__ == "__main__":
    main(sys.argv[1])
