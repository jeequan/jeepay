package com.jeequan.jeepay.pay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: zx
 * @date: 2024/12/27 23:02
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
            .info(new Info() // 基本信息配置
                .title("Jeepay支付网关Api接口文档") // 标题
                .version("1.0") // 版本
                // 设置OpenAPI文档的联系信息，包括联系人姓名为"patrick"，邮箱为"patrick@gmail.com"。
                .contact(new Contact().name("jeequan"))
                // 设置OpenAPI文档的许可证信息，包括许可证名称为"Apache 2.0"，许可证URL为"http://springdoc.org"。
                .license(new License().name("Apache 2.0").url("https://www.jeequan.com"))
            );
    }

}
