package com.jeequan.jeepay.core.entity;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiModelProperty {
    String value() default "";
    String description() default "";
}
