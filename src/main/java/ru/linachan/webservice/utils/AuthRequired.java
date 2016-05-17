package ru.linachan.webservice.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AuthRequired {
    String value() default "Yggdrasil WebService";
}
