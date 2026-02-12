package com.seatpick.seatpick.controller;

import com.seatpick.seatpick.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 👈 여기 변경
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/upgrade")
    public ResponseEntity<String> upgradeToOwner(Authentication authentication) { // 👈 파라미터 변경

        // 1. Authentication 객체에서 이름(Principal)을 꺼냅니다.
        // JwtTokenProvider구현에 따라 다르지만, 보통 여기가 providerId(sub)가 됩니다.
        String providerId = authentication.getName();

        System.out.println("현재 로그인한 사용자 ID: " + providerId); // 로그로 확인해보세요

        // 2. 서비스 호출
        userService.upgradeToOwner(providerId);

        return ResponseEntity.ok("사장님으로 전환되었습니다! 🎉 이제 공간을 등록해보세요.");
    }
}