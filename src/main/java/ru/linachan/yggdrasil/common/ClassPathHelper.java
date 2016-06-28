package ru.linachan.yggdrasil.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathHelper {

    private static final Class<?>[] parameters = new Class<?>[] { URL.class };

    public static void addFile(String jarFilePath) throws IOException {
        File jarFile = new File(jarFilePath);
        addFile(jarFile);
    }

    public static void addFile(File jarFile) throws IOException {
        addURL(jarFile.toURI().toURL());
    }

    public static void addURL(URL jarFileURL) throws IOException {
        URLClassLoader systemClassLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> classLoaderClass = URLClassLoader.class;
        try {
            Method method = classLoaderClass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(systemClassLoader, jarFileURL);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new IOException(String.format(
                "Error, could not add URL to system classloader: [%s] %s",
                throwable.getClass().getSimpleName(), throwable.getMessage()
            ));
        }
    }
}
