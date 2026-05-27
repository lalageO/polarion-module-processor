package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.service.shared.ModuleProcessException;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 通过公司 SVN 拉取 Polarion module.xml。
 */
@Service
public class ModuleXmlDownloader {

    private final PolarionProperties properties;

    public ModuleXmlDownloader(PolarionProperties properties) {
        this.properties = properties;
    }

    /**
     * 使用配置中的 baseUrl 拉取 module.xml，主要保留给旧测试和回退调用。
     */
    public String download(String projectId, String moduleFolder, String moduleName) {
        return download(properties.getBaseUrl(), projectId, moduleFolder, moduleName);
    }

    /**
     * 通过 svn co --depth=empty 和 svn up module.xml 拉取 module.xml 原文。
     */
    public String download(String baseUrl, String projectId, String moduleFolder, String moduleName) {
        String svnModuleUrl = buildSvnModuleUrl(baseUrl, projectId, moduleFolder, moduleName);
        Path tempRoot = null;
        try {
            tempRoot = Files.createTempDirectory("polarion-module-svn-");
            Path checkoutDir = tempRoot.resolve(TextUtils.sanitizePathPart(moduleName));
            PolarionProperties.Svn svn = svnConfig();
            runCommand(buildCheckoutCommand(svnModuleUrl, checkoutDir), tempRoot, timeout(svn.getCheckoutTimeoutSeconds(), 60), "SVN_CHECKOUT_FAILED");
            runCommand(buildUpdateCommand(), checkoutDir, timeout(svn.getUpdateTimeoutSeconds(), 60), "SVN_UPDATE_FAILED");

            Path moduleXml = checkoutDir.resolve(properties.getModuleFileName());
            String xmlContent = new String(Files.readAllBytes(moduleXml), StandardCharsets.UTF_8);
            validateModuleXml(xmlContent);
            return xmlContent;
        } catch (ModuleProcessException e) {
            throw e;
        } catch (IOException e) {
            throw new ModuleProcessException("MODULE_XML_SVN_FAILED", "SVN download module.xml failed: " + e.getMessage(), e);
        } finally {
            cleanupTempDir(tempRoot);
        }
    }

    /**
     * 构造 SVN 模块目录 URL，例如 http://alm.freetech.com/repo/FDP_Demo/modules/10 Stakeholder Requirement/R171e/。
     */
    public String buildSvnModuleUrl(String baseUrl, String projectId, String moduleFolder, String moduleName) {
        String normalizedBaseUrl = normalizeBaseUrl(TextUtils.hasText(baseUrl) ? baseUrl : properties.getBaseUrl());
        return normalizedBaseUrl
                + "/repo/"
                + projectId
                + "/modules/"
                + moduleFolder
                + "/"
                + moduleName
                + "/";
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private List<String> buildCheckoutCommand(String svnModuleUrl, Path checkoutDir) {
        List<String> command = new ArrayList<String>();
        command.add(svnExecutable());
        command.add("co");
        command.add(svnModuleUrl);
        command.add(checkoutDir.toString());
        command.add("--depth=empty");
        addSvnAuth(command);
        command.add("--non-interactive");
        return command;
    }

    private List<String> buildUpdateCommand() {
        List<String> command = new ArrayList<String>();
        command.add(svnExecutable());
        command.add("up");
        command.add(properties.getModuleFileName());
        addSvnAuth(command);
        command.add("--non-interactive");
        return command;
    }

    private String svnExecutable() {
        PolarionProperties.Svn svn = svnConfig();
        return svn == null || !TextUtils.hasText(svn.getExecutable()) ? "svn" : svn.getExecutable();
    }

    private void addSvnAuth(List<String> command) {
        PolarionProperties.Svn svn = svnConfig();
        if (svn == null) {
            return;
        }
        if (TextUtils.hasText(svn.getUsername())) {
            command.add("--username");
            command.add(svn.getUsername());
        }
        if (TextUtils.hasText(svn.getPassword())) {
            command.add("--password");
            command.add(svn.getPassword());
        }
    }

    private PolarionProperties.Svn svnConfig() {
        return properties.getSvn() == null ? new PolarionProperties.Svn() : properties.getSvn();
    }

    private void runCommand(List<String> command, Path workDir, int timeoutSeconds, String errorCode) {
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
            if (process.exitValue() != 0) {
                throw new ModuleProcessException(errorCode,
                        "SVN command failed, exitCode=" + process.exitValue() + ", output=" + abbreviate(collector.getOutput()));
            }
        } catch (ModuleProcessException e) {
            throw e;
        } catch (IOException e) {
            throw new ModuleProcessException(errorCode, "SVN command failed to start: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ModuleProcessException(errorCode, "SVN command was interrupted", e);
        }
    }

    private int timeout(Integer configuredValue, int defaultValue) {
        return configuredValue == null || configuredValue <= 0 ? defaultValue : configuredValue;
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
    }

    private void cleanupTempDir(Path tempRoot) {
        if (!Boolean.TRUE.equals(svnConfig().getCleanupTempDir()) || tempRoot == null || !Files.exists(tempRoot)) {
            return;
        }
        try {
            Stream<Path> stream = Files.walk(tempRoot);
            try {
                stream.sorted(new Comparator<Path>() {
                    @Override
                    public int compare(Path left, Path right) {
                        return Integer.compare(right.getNameCount(), left.getNameCount());
                    }
                }).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // 清理临时目录失败不影响主流程结果。
                    }
                });
            } finally {
                stream.close();
            }
        } catch (IOException ignored) {
            // 清理临时目录失败不影响主流程结果。
        }
    }

    /**
     * 避免登录页或错误页被误当作 module.xml 继续处理。
     */
    public void validateModuleXml(String xmlContent) {
        if (!TextUtils.hasText(xmlContent)
                || !xmlContent.contains("<module")
                || (!xmlContent.contains("id=\"homePageContent\"") && !xmlContent.contains("id='homePageContent'"))) {
            throw new ModuleProcessException("MODULE_XML_INVALID", "downloaded content is not a valid module.xml");
        }
    }

    /**
     * 后台收集 SVN 命令输出，避免命令输出较多时阻塞进程。
     */
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
