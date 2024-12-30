package com.jeequan.jeepay.core.entity;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiModel {
    String value() default "";
    String description() default "";
}
