package com.seatpick.seatpick.security;

import com.seatpick.seatpick.domain.entity.User;
import com.seatpick.seatpick.dto.TokenInfo;
import com.seatpick.seatpick.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. 구글에서 유저 정보 꺼내기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = oAuth2User.getName(); // 구글의 고유 ID (sub)

        log.info("구글 로그인 성공! 이메일: {}", email);

        // 2. DB에 저장 (없으면 가입, 있으면 업데이트)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .provider("google")
                        .providerId(providerId)
                        .build()));

        // 3. 토큰 생성 (JWT)
        // 주의: 토큰을 만들 때 필요한 Authentication 객체를 새로 만들어야 함 (우리 DB 기준)
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 4. 프론트엔드로 토큰 보내기 (Redirect)
        // http://localhost:3000/oauth/callback?accessToken=...&refreshToken=... 로 이동
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/callback")
                .queryParam("accessToken", tokenInfo.getAccessToken())
                .queryParam("refreshToken", tokenInfo.getRefreshToken())
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}