package com.icedberries.UBFunkeysServer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

@Configuration
public class Password {

    private static final Integer STRENGTH = 15;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(STRENGTH, new SecureRandom());
    }
}
