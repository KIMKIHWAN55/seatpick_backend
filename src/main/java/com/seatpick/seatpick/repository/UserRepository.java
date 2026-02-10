package com.seatpick.seatpick.repository;

import com.seatpick.seatpick.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // 로그인할 때 이메일로 찾기 위함
    Optional<User> findByProviderId(String providerId);
}