package com.flipkart.user.config;

import com.flipkart.user.model.User;
import com.flipkart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Creates the seller/admin account used to access the Seller Hub (log in with mobile 9999999999). */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    static final String ADMIN_MOBILE = "9999999999";

    private final UserRepository users;

    public AdminSeeder(UserRepository users) {
        this.users = users;
    }

    @Override
    public void run(String... args) {
        if (users.findByMobile(ADMIN_MOBILE).isPresent()) return;
        User admin = new User();
        admin.setName("Seller Admin");
        admin.setMobile(ADMIN_MOBILE);
        admin.setRole("ADMIN");
        users.save(admin);
        log.info("Created seller admin account (mobile {})", ADMIN_MOBILE);
    }
}
