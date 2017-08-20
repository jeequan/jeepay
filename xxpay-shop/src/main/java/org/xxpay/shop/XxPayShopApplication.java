package org.xxpay.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class XxPayShopApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(XxPayShopApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        application.listeners();
        return application.sources(applicationClass);
    }

    private static Class<XxPayShopApplication> applicationClass = XxPayShopApplication.class;

}