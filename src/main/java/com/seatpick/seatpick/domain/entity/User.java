package com.seatpick.seatpick.domain.entity;

import com.seatpick.seatpick.domain.type.UserRole;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    // 회원가입용 빌더
    @Builder
    public User(String email, String name, String password, String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
    }

    public boolean isOwner() {
        return this.role == UserRole.OWNER;
    }

    // 소셜 로그인 시 이름 업데이트
    public User update(String name) {
        this.name = name;
        return this;
    }

    // 필요 시 사장님으로 등업해주는 메서드
    public void upgradeToOwner() {
        this.role = UserRole.OWNER;
    }
}