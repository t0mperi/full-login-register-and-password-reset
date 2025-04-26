package com.example.loginregisterreset.repository;

import com.example.loginregisterreset.entity.PasswordResetToken;
import com.example.loginregisterreset.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    PasswordResetToken findByUserId(Long userId);

    void deleteByUser(User user);
}
