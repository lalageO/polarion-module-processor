package com.example.polarionprocessor.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {

    private FileUtils() {
    }

    public static void ensureDirectory(Path directory) throws IOException {
        Files.createDirectories(directory);
    }

    public static void writeUtf8(Path file, String content) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            ensureDirectory(parent);
        }
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    public static String relativePath(Path base, Path child) {
        return base.relativize(child).toString().replace('\\', '/');
    }
}
