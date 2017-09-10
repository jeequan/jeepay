package org.xxpay.dubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"org.xxpay"})
public class XxPayDubboServiceAppliaction {
    public static void main(String[] args) {
        SpringApplication.run(XxPayDubboServiceAppliaction.class, args);
    }
}
