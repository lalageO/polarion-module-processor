package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.model.polarion.SvnCommitResult;
import com.example.polarionprocessor.service.shared.ModuleProcessException;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 将生成后的 module.xml 写回新的 SVN 工作副本并提交。
 */
@Service
public class SvnModuleCommitter {

    private static final String WORKSPACE_DIR_NAME = "svn-workspace";
    private static final Pattern COMMITTED_REVISION_PATTERN = Pattern.compile("Committed revision (\\d+)\\.");

    private final PolarionProperties properties;
    private final ModuleXmlDownloader moduleXmlDownloader;
    private final SvnCommandExecutor commandExecutor;

    public SvnModuleCommitter(PolarionProperties properties,
                              ModuleXmlDownloader moduleXmlDownloader,
                              SvnCommandExecutor commandExecutor) {
        this.properties = properties;
        this.moduleXmlDownloader = moduleXmlDownloader;
        this.commandExecutor = commandExecutor;
    }

    public SvnCommitResult commit(String jobId,
                                  Path outputDir,
                                  String baseUrl,
                                  String projectId,
                                  String moduleFolder,
                                  String moduleName,
                                  Path processedModuleXml,
                                  String commitMessage) {
        Path normalizedOutputDir = outputDir.toAbsolutePath().normalize();
        Path workspaceDir = normalizedOutputDir.resolve(WORKSPACE_DIR_NAME);
        Path normalizedProcessedModuleXml = processedModuleXml == null
                ? null
                : processedModuleXml.toAbsolutePath().normalize();
        SvnCommitResult result = SvnCommitResult.failed(null);
        result.setWorkspaceDir(toResponsePath(workspaceDir));
        try {
            if (normalizedProcessedModuleXml == null || !Files.exists(normalizedProcessedModuleXml)) {
                return fail(result, "Processed module.xml not found: " + processedModuleXml);
            }
            Files.createDirectories(normalizedOutputDir);
            String svnModuleUrl = moduleXmlDownloader.buildSvnModuleUrl(baseUrl, projectId, moduleFolder, moduleName);
            commandExecutor.run(buildCheckoutCommand(svnModuleUrl, workspaceDir),
                    normalizedOutputDir,
                    timeout(svnConfig().getCheckoutTimeoutSeconds(), 60),
                    "SVN_COMMIT_CHECKOUT_FAILED");
            commandExecutor.run(buildUpdateCommand(),
                    workspaceDir,
                    timeout(svnConfig().getUpdateTimeoutSeconds(), 60),
                    "SVN_COMMIT_UPDATE_FAILED");

            Path workspaceModuleXml = workspaceDir.resolve(properties.getModuleFileName());
            Files.copy(normalizedProcessedModuleXml, workspaceModuleXml, StandardCopyOption.REPLACE_EXISTING);

            String statusBeforeCommit = normalizeStatus(commandExecutor.run(buildStatusCommand(),
                    workspaceDir,
                    timeout(svnConfig().getUpdateTimeoutSeconds(), 60),
                    "SVN_STATUS_FAILED"));
            result.setStatusBeforeCommit(statusBeforeCommit);

            if (!TextUtils.hasText(statusBeforeCommit)) {
                result.setSuccess(true);
                result.setStatus(SvnCommitResult.STATUS_NO_CHANGE);
                return result;
            }
            if (!statusBeforeCommit.startsWith("M")) {
                return fail(result, "Unexpected SVN status before commit: " + statusBeforeCommit);
            }

            String commitOutput = commandExecutor.run(buildCommitCommand(firstText(commitMessage, svnConfig().getDefaultCommitMessage())),
                    workspaceDir,
                    timeout(svnConfig().getCommitTimeoutSeconds(), 60),
                    "SVN_COMMIT_FAILED");
            String revision = parseRevision(commitOutput);
            if (!TextUtils.hasText(revision)) {
                return fail(result, "SVN commit output did not contain committed revision: " + abbreviate(commitOutput));
            }
            result.setSuccess(true);
            result.setStatus(SvnCommitResult.STATUS_COMMITTED);
            result.setRevision(revision);
            result.setStatusAfterCommit(normalizeStatus(commandExecutor.run(buildStatusCommand(),
                    workspaceDir,
                    timeout(svnConfig().getUpdateTimeoutSeconds(), 60),
                    "SVN_STATUS_AFTER_COMMIT_FAILED")));
            return result;
        } catch (ModuleProcessException e) {
            return fail(result, e.getErrorCode() + ": " + e.getMessage());
        } catch (IOException e) {
            return fail(result, "SVN_COMMIT_FILE_FAILED: " + e.getMessage());
        } finally {
            cleanupWorkspace(workspaceDir);
        }
    }

    private SvnCommitResult fail(SvnCommitResult result, String errorMessage) {
        result.setSuccess(false);
        result.setStatus(SvnCommitResult.STATUS_COMMIT_FAILED);
        result.setErrorMessage(errorMessage);
        return result;
    }

    private List<String> buildCheckoutCommand(String svnModuleUrl, Path workspaceDir) {
        List<String> command = new ArrayList<String>();
        command.add(svnExecutable());
        command.add("checkout");
        command.add("--depth=empty");
        command.add(svnModuleUrl);
        command.add(workspaceDir.toString());
        addSvnAuth(command);
        command.add("--non-interactive");
        return command;
    }

    private List<String> buildUpdateCommand() {
        List<String> command = new ArrayList<String>();
        command.add(svnExecutable());
        command.add("update");
        command.add(properties.getModuleFileName());
        addSvnAuth(command);
        command.add("--non-interactive");
        return command;
    }

    private List<String> buildStatusCommand() {
        List<String> command = new ArrayList<String>();
        command.add(svnExecutable());
        command.add("status");
        command.add(properties.getModuleFileName());
        return command;
    }

    private List<String> buildCommitCommand(String commitMessage) {
        List<String> command = new ArrayList<String>();
        command.add(svnExecutable());
        command.add("commit");
        command.add("-m");
        command.add(firstText(commitMessage, "update module.xml"));
        command.add(properties.getModuleFileName());
        addSvnAuth(command);
        command.add("--non-interactive");
        return command;
    }

    private void addSvnAuth(List<String> command) {
        PolarionProperties.Svn svn = svnConfig();
        if (TextUtils.hasText(svn.getUsername())) {
            command.add("--username");
            command.add(svn.getUsername());
        }
        if (TextUtils.hasText(svn.getPassword())) {
            command.add("--password");
            command.add(svn.getPassword());
        }
    }

    private String svnExecutable() {
        PolarionProperties.Svn svn = svnConfig();
        return TextUtils.hasText(svn.getExecutable()) ? svn.getExecutable() : "svn";
    }

    private PolarionProperties.Svn svnConfig() {
        return properties.getSvn() == null ? new PolarionProperties.Svn() : properties.getSvn();
    }

    private int timeout(Integer configuredValue, int defaultValue) {
        return configuredValue == null || configuredValue <= 0 ? defaultValue : configuredValue;
    }

    private String parseRevision(String output) {
        Matcher matcher = COMMITTED_REVISION_PATTERN.matcher(output == null ? "" : output);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String normalizeStatus(String output) {
        return output == null ? "" : output.trim();
    }

    private String firstText(String first, String fallback) {
        return TextUtils.hasText(first) ? first.trim() : fallback;
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
    }

    private String toResponsePath(Path path) {
        return path == null ? null : path.toString().replace('\\', '/');
    }

    private void cleanupWorkspace(Path workspaceDir) {
        if (!Boolean.TRUE.equals(svnConfig().getCleanupCommitWorkspace())
                || workspaceDir == null
                || !Files.exists(workspaceDir)) {
            return;
        }
        try {
            Stream<Path> stream = Files.walk(workspaceDir);
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
                        // 提交工作区清理失败不影响任务结果。
                    }
                });
            } finally {
                stream.close();
            }
        } catch (IOException ignored) {
            // 提交工作区清理失败不影响任务结果。
        }
    }
}
