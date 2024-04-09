package me.mateus.milkshake.core.utils;

public final class Casing {
    public static String[] fromCamelCase(String camelCaseString) {
        return camelCaseString.split("(?=[A-Z])");
    }

    public static String toSnakeCase(String[] words) {
        return String.join("_", words).toLowerCase();
    }

    public static String squash(String[] words) {
        return String.join("", words).toLowerCase();
    }
}
