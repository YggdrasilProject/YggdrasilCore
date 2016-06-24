package ru.linachan.yggdrasil.plugin.helpers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedOS{
    OSSupport[] value() default {};
}
