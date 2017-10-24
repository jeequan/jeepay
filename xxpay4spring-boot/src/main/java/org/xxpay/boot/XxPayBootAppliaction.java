package org.xxpay.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"org.xxpay"})
public class XxPayBootAppliaction {
    public static void main(String[] args) {
        SpringApplication.run(XxPayBootAppliaction.class, args);
    }
}
