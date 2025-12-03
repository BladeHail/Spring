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

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                AuthProvider.valueOf(registrationId.toUpperCase()), providerId);

        String targetUsername;
        String fallbackUsername = registrationId + "_" + providerId; // 예: naver_123456

        if (email != null && !email.isEmpty()) {
            Optional<User> userByEmail = userRepository.findByUsername(email);

            if (userByEmail.isPresent()) {
                User existUser = userByEmail.get();

                if (userOptional.isPresent() && existUser.getId().equals(userOptional.get().getId())) {
                    targetUsername = email;
                } else {
                    targetUsername = fallbackUsername;
                    log.info("아이디 중복 방지: 일반 회원이 존재하여 {} 대신 {}를 사용합니다.", email, targetUsername);
                }
            } else {
                targetUsername = email;
            }
        } else {
            targetUsername = fallbackUsername;
        }

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.updateOAuthInfo(targetUsername, profileImage);
            userRepository.save(user);
        } else {
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