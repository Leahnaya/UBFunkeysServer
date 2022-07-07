package com.icedberries.UBFunkeysServer.config;

import com.icedberries.UBFunkeysServer.UBFunkeysServerApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(UBFunkeysServerApplication.class);
	}
}
