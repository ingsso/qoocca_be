package com.qoocca.teachers.api.user.service;

import com.qoocca.teachers.common.redis.RedisDao;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SmsService {
    private final RedisDao redisDao;
    private final UserRepository userRepository;

    public void sendVerificationCode(String phone) {

        String cleanPhone = phone.replaceAll("[^0-9]", "");

        if (cleanPhone.length() != 11) {
            throw new CustomException(ErrorCode.SMS_INVALID_PHONE);
        }

        String verificationCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        redisDao.setValues("SMS:" + cleanPhone, verificationCode, Duration.ofMinutes(3));

        System.out.println("휴대폰: " + cleanPhone + " / 인증번호: " + verificationCode);
    }

    public Map<String, Object> verifyCode(String phone, String code) {
        String savedCode = (String) redisDao.getValues("SMS:" + phone);

        if (savedCode != null && savedCode.equals(code)) {
            redisDao.deleteValues("SMS:" + phone);
            redisDao.setValues("SMS_VERIFIED:" + phone, "true", Duration.ofMinutes(5));

            boolean isExistingUser = userRepository.findByPhoneNumber(phone).isPresent();

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("isExistingUser", isExistingUser);
            return result;
        } else {
            throw new CustomException(ErrorCode.SMS_CODE_INVALID);
        }
    }

    public void checkIsVerified(String phone) {
        String verified = (String) redisDao.getValues("SMS_VERIFIED:" + phone);
        if (!"true".equals(verified)) {
            throw new CustomException(ErrorCode.SMS_NOT_VERIFIED);
        }
    }

    public void deleteVerifiedState(String phone) {
        redisDao.deleteValues("SMS_VERIFIED:" + phone);
    }
}
