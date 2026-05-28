package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.service.shared.ModuleProcessException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SVN 命令执行器，统一使用 ProcessBuilder 参数数组，避免 shell 字符串。
 */
@Service
public class SvnCommandExecutor {

    public String run(List<String> command, Path workDir, int timeoutSeconds, String errorCode) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(workDir.toFile());
            builder.redirectErrorStream(true);
            Process process = builder.start();
            StreamCollector collector = new StreamCollector(process.getInputStream());
            Thread outputThread = new Thread(collector, "svn-output-reader");
            outputThread.setDaemon(true);
            outputThread.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new ModuleProcessException(errorCode, "SVN command timed out after " + timeoutSeconds + " seconds");
            }
            outputThread.join(1000);
            String output = collector.getOutput();
            if (process.exitValue() != 0) {
                throw new ModuleProcessException(errorCode,
                        "SVN command failed, exitCode=" + process.exitValue() + ", output=" + abbreviate(output));
            }
            return output;
        } catch (ModuleProcessException e) {
            throw e;
        } catch (IOException e) {
            throw new ModuleProcessException(errorCode, "SVN command failed to start: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ModuleProcessException(errorCode, "SVN command was interrupted", e);
        }
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
    }

    private static class StreamCollector implements Runnable {

        private final InputStream inputStream;
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        private StreamCollector(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096];
            int length;
            try {
                while ((length = inputStream.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException ignored) {
                // 命令结束或被销毁时可能触发流读取异常，调用方仍会根据 exitCode 判断结果。
            }
        }

        private String getOutput() {
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
