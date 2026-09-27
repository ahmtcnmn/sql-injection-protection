package com.ahmtcnmn.manager.service.ApiKey;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class GenerateApiKey {

    private static final SecureRandom secureRandom = new SecureRandom();

    public String generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
