package me.mateus.milkshake.core.utils;

public final class Casing {
    public static String[] fromSnakeCase(String snakeCaseString) {
        return snakeCaseString.split("_");
    }

    public static String[] fromCamelCase(String camelCaseString) {
        return camelCaseString.split("(?=[A-Z])");
    }

    public static String toSnakeCase(String[] words) {
        return String.join("_", words).toLowerCase();
    }

    public static String toCamelCase(String[] words) {
        if (words.length == 0)
            return "";
        String result = words[0];
        for (int idx = 1; idx < words.length; idx++) {
            char leadingChar = Character.toUpperCase(words[idx].charAt(0));
            result += leadingChar + words[idx].substring(1);
        }
        return result;
    }

    public static String squash(String[] words) {
        return String.join("", words).toLowerCase();
    }
}
