package com.qoocca.teachers.api.user.service;

import com.qoocca.teachers.common.redis.RedisDao;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {
    private final RedisDao redisDao;
    private final UserRepository userRepository;

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9]", "");
    }

    public void sendVerificationCode(String phone) {
        String cleanPhone = normalizePhone(phone);

        if (cleanPhone.length() != 11) {
            throw new CustomException(ErrorCode.SMS_INVALID_PHONE);
        }

        String verificationCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        redisDao.setValues("SMS:" + cleanPhone, verificationCode, Duration.ofMinutes(3));

        log.info("SMS verification code generated for phone={}", cleanPhone);
    }

    public Map<String, Object> verifyCode(String phone, String code) {
        String cleanPhone = normalizePhone(phone);
        String savedCode = (String) redisDao.getValues("SMS:" + cleanPhone);

        if (savedCode != null && savedCode.equals(code)) {
            redisDao.deleteValues("SMS:" + cleanPhone);
            redisDao.setValues("SMS_VERIFIED:" + cleanPhone, "true", Duration.ofMinutes(5));

            boolean isExistingUser = userRepository.findByPhoneNumber(cleanPhone).isPresent();

            Map<String, Object> result = new HashMap<>();
            result.put("isExistingUser", isExistingUser);
            return result;
        } else {
            throw new CustomException(ErrorCode.SMS_CODE_INVALID);
        }
    }

    public void checkIsVerified(String phone) {
        String cleanPhone = normalizePhone(phone);
        String verified = (String) redisDao.getValues("SMS_VERIFIED:" + cleanPhone);
        if (!"true".equals(verified)) {
            throw new CustomException(ErrorCode.SMS_NOT_VERIFIED);
        }
    }

    public void deleteVerifiedState(String phone) {
        String cleanPhone = normalizePhone(phone);
        redisDao.deleteValues("SMS_VERIFIED:" + cleanPhone);
    }
}
