package ru.linachan.yggdrasil.plugin.helpers;

import ru.linachan.yggdrasil.common.SystemInfo;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SupportedOS.class)
public @interface OSSupport {

    SystemInfo.OSType value() default SystemInfo.OSType.ALL;
    SystemInfo.OSArch arch() default SystemInfo.OSArch.ALL;
}
