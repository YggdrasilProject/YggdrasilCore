package ru.linachan.yggdrasil.shell.helpers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ShellCommand {

    String command();
    String description();
}
