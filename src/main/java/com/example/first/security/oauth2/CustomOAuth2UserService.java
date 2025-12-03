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

        // Provider별 사용자 정보 매핑
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

        // username 생성 로직: 이메일 우선 사용
        String targetUsername;
        if (email != null && !email.isEmpty()) {
            targetUsername = email;
        } else {
            targetUsername = registrationId + "_" + providerId;
        }

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                AuthProvider.valueOf(registrationId.toUpperCase()), providerId);

        User user;
        if (userOptional.isPresent()) {
            // 이미 가입된 회원이라면 정보 업데이트
            user = userOptional.get();
            user.updateOAuthInfo(targetUsername, profileImage);
            userRepository.save(user);
        } else {
            // [수정] 신규 회원일 경우 랜덤 비밀번호 생성 및 암호화
            String uuid = UUID.randomUUID().toString().substring(0, 16);
            String encodedPassword = passwordEncoder.encode(uuid);

            user = User.builder()
                    .username(targetUsername)
                    .email(email)
                    .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                    .providerId(providerId)
                    .profileImage(profileImage)
                    .password(encodedPassword)
                    .build();
            userRepository.save(user);
        }

        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }
}