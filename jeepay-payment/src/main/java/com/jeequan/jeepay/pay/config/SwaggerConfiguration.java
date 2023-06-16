package com.jeequan.jeepay.pay.config;

import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * knife4j 自定义文档配置
 * API访问地址： http://localhost:9216/doc.html
 * @author yr
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfiguration {

    private final OpenApiExtensionResolver openApiExtensionResolver;

    @Autowired
    public SwaggerConfiguration(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    @Bean(value = "defaultApi")
    public Docket defaultApi() {
        String groupName = "支付网关";
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder().version("1.0").build()) //描述字段支持Markdown语法
                .groupName(groupName)
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .build()
                .extensions(openApiExtensionResolver.buildExtensions(groupName));
        return docket;
    }
}
