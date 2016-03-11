package ru.linachan.yggdrasil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YggdrasilConfig {

    private Map<String, String> configData;

    private static final Pattern sectionPattern = Pattern.compile(
            "^\\s*\\[(?<section>[^\\]]+)\\]\\s*$"
    );
    private static final Pattern configPattern = Pattern.compile(
            "^\\s*(?<key>[^#\\s]+)\\s*=\\s*(?<value>([^#]+)|('[^']+')|(\"[^\"]+\"))\\s*$"
    );

    private YggdrasilConfig(Map<String, String> config) {
        configData = config;
    }

    public static YggdrasilConfig readConfig(File configFile) throws IOException {
        Map<String, String> config = new HashMap<>();

        BufferedReader configReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));

        String configLine;
        String sectionName = "DEFAULT";

        while ((configLine = configReader.readLine()) != null) {
            Matcher sectionMatcher = sectionPattern.matcher(configLine);
            Matcher configMatcher = configPattern.matcher(configLine);

            if (sectionMatcher.matches()) {
                sectionName = sectionMatcher.group("section");
            } else if (configMatcher.matches()) {
                String keyName = configMatcher.group("key");
                String keyValue = configMatcher.group("value");

                config.put(sectionName.toLowerCase() + "." + keyName.toLowerCase(), keyValue);
            }
        }

        return new YggdrasilConfig(config);
    }

    public String getString(String key, String defaultValue) {
        return (configData.containsKey(key.toLowerCase())) ? configData.get(key.toLowerCase()) : defaultValue;
    }

    public Integer getInt(String key, Integer defaultValue) {
        return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
    }

    public Long getLong(String key, Long defaultValue) {
        return Long.parseLong(getString(key, String.valueOf(defaultValue)));
    }

    public Float getFloat(String key, Float defaultValue) {
        return Float.parseFloat(getString(key, String.valueOf(defaultValue)));
    }

    public Double getDouble(String key, Double defaultValue) {
        return Double.parseDouble(getString(key, String.valueOf(defaultValue)));
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
    }

    public <T> List<T> getList(String key, Class<T> valueType) {
        List<T> result = new ArrayList<>();
        String configValue = getString(key, null);

        if (configValue != null) {
            for (String valuePart: configValue.split("\\s*,\\s*")) {
                result.add(valueType.cast(valuePart));
            }
        }

        return result;
    }
}