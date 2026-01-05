package com.example.qoocca_be.user.entity;

public enum OauthProvider {
  KAKAO, NAVER;

  public static OauthProvider fromString(String providerStr) {
    for (OauthProvider p : OauthProvider.values()) {
      if (p.name().equalsIgnoreCase(providerStr)) {
        return p;
      }
    }
    throw new IllegalArgumentException("지원하지 않는 소셜 공급자입니다: " + providerStr);
  }
}
