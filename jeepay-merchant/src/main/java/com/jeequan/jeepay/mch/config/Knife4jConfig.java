package com.jeequan.jeepay.mch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;

/**
 * @description:
 * @author: zx
 * @date: 2024/12/27 23:02
 */
public class Knife4jConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                // 接口文档标题
                .info(new Info().title("运营平台API")
                        // .description("运营平台")
                        // 接口文档版本
                        .version("v1.0")
                        .license(new License().name("Apache 2.0")
                        .url("https://www.jeequan.com")));
    }

}
