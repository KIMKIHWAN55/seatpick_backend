package com.seatpick.seatpick.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String password; // 👈 구글 로그인은 비번이 없으므로 nullable 허용

    private String provider; // "google" 또는 "local" (일반가입)
    private String providerId; // 구글에서 주는 고유 ID (sub 값)

    // 회원가입용 빌더
    @Builder
    public User(String email, String name, String password, String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
    }

    // 소셜 로그인 시, 이름이 바뀌었으면 업데이트 해주는 메서드
    public User update(String name) {
        this.name = name;
        return this;
    }
}