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

import java.util.ArrayList;

/**
 * knife4j 自定义文档配置
 * API访问地址： http://localhost:9216/doc.html
 * @author yr
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfiguration {

    @Autowired(required = false)
    private OpenApiExtensionResolver openApiExtensionResolver;

    /**
     * 功能描述:  API访问地址： http://localhost:9216/doc.html
     *
     * @Return: springfox.documentation.spring.web.plugins.Docket
     * @Author: terrfly
     * @Date: 2023/6/20 15:04
     */
    @Bean(value = "knife4jDockerBean")
    public Docket knife4jDockerBean() {
        return new Docket(DocumentationType.SWAGGER_2)  //指定使用Swagger2规范
                .apiInfo(new ApiInfoBuilder().version("1.0").build()) //描述字段支持Markdown语法
                .groupName("支付网关") //分组名称
                .select() // 配置： 如何扫描
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)) // 只扫描： ApiOperation 注解文档。 也支持配置包名、 路径等扫描模式。
                .build().extensions(openApiExtensionResolver == null ? new ArrayList<>() : openApiExtensionResolver.buildExtensions("支付网关"));
    }


}
