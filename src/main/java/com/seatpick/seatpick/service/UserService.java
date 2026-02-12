package com.seatpick.seatpick.service;

import com.seatpick.seatpick.domain.entity.User;
import com.seatpick.seatpick.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 사장님으로 등업 (Update)
    @Transactional
    public void upgradeToOwner(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 엔티티의 메서드 호출 (더티 체킹으로 자동 저장됨)
        user.upgradeToOwner();
    }
}