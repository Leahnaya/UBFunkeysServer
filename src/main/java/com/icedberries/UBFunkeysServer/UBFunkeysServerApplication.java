package com.icedberries.UBFunkeysServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.icedberries")
public class UBFunkeysServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UBFunkeysServerApplication.class, args);
	}

}
