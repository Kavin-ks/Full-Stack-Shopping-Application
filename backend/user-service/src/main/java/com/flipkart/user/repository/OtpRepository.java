package com.flipkart.user.repository;

import com.flipkart.user.model.OtpCode;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OtpRepository extends MongoRepository<OtpCode, String> {
}
