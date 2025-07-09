package dev.rebelcraft.cli;

public class FileNameUtil {
    /**
     * Escapes a name for safe use in file and directory names.
     * Replaces any character that is not a letter, digit, dot, or dash with an underscore.
     */
    public static String escapeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
