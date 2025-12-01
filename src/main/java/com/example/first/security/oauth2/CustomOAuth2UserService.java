package com.example.first.security.oauth2;

import com.example.first.entity.AuthProvider;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("=== OAuth2 로그인 시작 ===");
        log.info("Provider: {}", userRequest.getClientRegistration().getRegistrationId());
        log.info("Attributes: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;

        if ("kakao".equals(registrationId)) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else if ("google".equals(registrationId)) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if ("naver".equals(registrationId)) {
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
        }

        if (oAuth2UserInfo == null) {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 Provider입니다.");
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();
        String profileImage = oAuth2UserInfo.getProfileImage();

        log.info("OAuth2 사용자 정보 - email: {}, name: {}, providerId: {}", email, name, providerId);

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                AuthProvider.valueOf(registrationId.toUpperCase()), providerId);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.updateOAuthInfo(name, profileImage);
            userRepository.save(user);
            log.info("기존 OAuth2 사용자 업데이트: {} (provider: {})", email, registrationId);
        } else {
            // OAuth 사용자를 위한 랜덤 비밀번호 생성
            String randomPassword = generateRandomPassword();
            String encodedPassword = passwordEncoder.encode(randomPassword);

            user = User.builder()
                    .username(name != null ? name : "user_" + providerId)
                    .email(email)
                    .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                    .providerId(providerId)
                    .profileImage(profileImage)
                    .password(encodedPassword)
                    .build();
            userRepository.save(user);
            log.info("신규 OAuth2 사용자 생성: {} (provider: {})", email, registrationId);
            log.info("랜덤 비밀번호 생성 완료");
        }

        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }

    /**
     * OAuth 사용자를 위한 랜덤 비밀번호 생성
     */
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
    }
}