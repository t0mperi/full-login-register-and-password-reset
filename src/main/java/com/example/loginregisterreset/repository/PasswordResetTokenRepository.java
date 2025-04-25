package com.example.loginregisterreset.repository;

import com.example.loginregisterreset.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    // Optional: Method to find token by user ID if needed later
    // PasswordResetToken findByUserId(Long userId);
}
