package com.example.first.security.oauth2;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }
    @Override
    public String getProvider() {
        return "google";
    }
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    @Override
    public String getName() {
        return (String) attributes.get("given_name");
    }
    @Override
    public String getProfileImage() {
        return (String) attributes.get("picture");
    }

    public String getSub() {
        return (String) attributes.get("sub");
    }
}
