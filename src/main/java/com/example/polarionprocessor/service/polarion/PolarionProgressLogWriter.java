package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 面向前端用户展示的任务进度日志，区别于服务端控制台日志。
 */
@Service
public class PolarionProgressLogWriter {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String fileName() {
        return "progress.log";
    }

    public synchronized void append(Path file, String message) throws IOException {
        if (file == null || !TextUtils.hasText(message)) {
            return;
        }
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        String line = "[" + TIME_FORMAT.format(LocalDateTime.now()) + "] " + message + System.lineSeparator();
        Files.write(file,
                line.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }
}
