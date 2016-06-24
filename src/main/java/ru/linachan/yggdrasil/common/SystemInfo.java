package ru.linachan.yggdrasil.common;

import java.io.File;

public class SystemInfo {

    public static File getWorkingDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    public static String getUser() {
        return System.getProperty("user.name");
    }

    public static File getUserHomeDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    public static String getUserLanguage() {
        return System.getProperty("user.language");
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static OSType getOSType() {
        String osName = System.getProperty("os.name");

        if (osName.contains("win"))
            return OSType.WINDOWS;

        if (osName.contains("mac"))
            return OSType.OSX;

        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))
            return OSType.UNIX;

        return OSType.OTHER;
    }

    public static OSArch getOSArch() {
        String osArch = System.getProperty("os.arch");

        if (osArch.contains("64"))
            return OSArch.X64;

        if (osArch.contains("86"))
            return OSArch.X86;

        return OSArch.OTHER;
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public enum OSType {
        WINDOWS, UNIX, OSX, OTHER, ALL
    }

    public enum OSArch {
        X64, X86, OTHER, ALL
    }
}
