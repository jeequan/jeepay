package org.xxpay.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@SpringBootApplication
@ComponentScan(basePackages={"org.xxpay"})
public class XxPayBootAppliaction {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
	   // Do any additional configuration here
	   return builder.build();
	}
	
    public static void main(String[] args) {
        SpringApplication.run(XxPayBootAppliaction.class, args);
    }
}
