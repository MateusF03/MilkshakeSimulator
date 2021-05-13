package me.mateus.milkshake.core.utils;

public class StringComparator {

    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isPoint(String string) {
        String[] a = string.split("[,.x]");
        if (a.length != 2)
            return false;
        return isInteger(a[0]) && isInteger(a[1]);
    }
}
