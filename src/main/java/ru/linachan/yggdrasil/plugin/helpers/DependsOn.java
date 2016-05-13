package ru.linachan.yggdrasil.plugin.helpers;

import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Dependencies.class)
public @interface DependsOn {
    Class<? extends YggdrasilPlugin> value();
}
