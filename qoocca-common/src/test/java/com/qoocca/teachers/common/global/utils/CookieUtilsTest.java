package com.qoocca.teachers.common.global.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieUtilsTest {

    @Test
    void addRefreshTokenCookieAppliesConfiguredAttributes() {
        CookieUtils cookieUtils = new CookieUtils();
        ReflectionTestUtils.setField(cookieUtils, "encryptionKey", "12345678901234567890123456789012");
        ReflectionTestUtils.setField(cookieUtils, "refreshCookieSecure", true);
        ReflectionTestUtils.setField(cookieUtils, "refreshCookieSameSite", "None");
        ReflectionTestUtils.setField(cookieUtils, "refreshCookiePath", "/");
        ReflectionTestUtils.setField(cookieUtils, "refreshCookieDomain", "example.com");
        ReflectionTestUtils.setField(cookieUtils, "refreshCookieMaxAge", 1234L);

        MockHttpServletResponse response = new MockHttpServletResponse();
        cookieUtils.addRefreshTokenCookie(response, "refresh-token-value");

        String setCookie = response.getHeader("Set-Cookie");
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("refreshToken="));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("SameSite=None"));
        assertTrue(setCookie.contains("Domain=example.com"));
        assertTrue(setCookie.contains("Max-Age=1234"));
    }
}
