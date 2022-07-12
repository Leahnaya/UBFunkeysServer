package com.icedberries.UBFunkeysServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.icedberries", "javagrinko.spring"},
		exclude = SecurityAutoConfiguration.class)
public class UBFunkeysServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UBFunkeysServerApplication.class, args);
	}

}
