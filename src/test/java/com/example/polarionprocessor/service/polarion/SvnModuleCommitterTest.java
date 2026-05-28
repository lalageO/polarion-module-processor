package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.model.polarion.SvnCommitResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvnModuleCommitterTest {

    @TempDir
    Path tempDir;

    @Test
    void svnStatusModifiedShouldCommit() throws Exception {
        FakeSvnCommandExecutor executor = new FakeSvnCommandExecutor("M       module.xml\n", "Committed revision 123456.\n");
        SvnModuleCommitter committer = buildCommitter(executor);
        Path processedModuleXml = processedModuleXml();
        Path relativeOutputDir = tempDir.getParent().relativize(tempDir);

        SvnCommitResult result = committer.commit(
                "job-1",
                relativeOutputDir,
                "http://alm.freetech.com",
                "FDP_Demo",
                "10 Stakeholder Requirement",
                "R171e",
                processedModuleXml,
                "update module.xml");

        assertEquals(Boolean.TRUE, result.getSuccess());
        assertEquals(SvnCommitResult.STATUS_COMMITTED, result.getStatus());
        assertEquals("123456", result.getRevision());
        assertEquals("M       module.xml", result.getStatusBeforeCommit());
        assertEquals(1, executor.getCommitCount());
        assertTrue(result.getWorkspaceDir().endsWith("svn-workspace"));
        assertTrue(Paths.get(executor.getCheckoutTarget()).isAbsolute());
    }

    @Test
    void svnStatusCleanShouldReturnNoChange() throws Exception {
        FakeSvnCommandExecutor executor = new FakeSvnCommandExecutor("", "Committed revision 123456.\n");
        SvnModuleCommitter committer = buildCommitter(executor);

        SvnCommitResult result = committer.commit(
                "job-1",
                tempDir,
                "http://alm.freetech.com",
                "FDP_Demo",
                "10 Stakeholder Requirement",
                "R171e",
                processedModuleXml(),
                "update module.xml");

        assertEquals(Boolean.TRUE, result.getSuccess());
        assertEquals(SvnCommitResult.STATUS_NO_CHANGE, result.getStatus());
        assertEquals(0, executor.getCommitCount());
    }

    @Test
    void svnStatusQuestionMarkShouldFail() throws Exception {
        FakeSvnCommandExecutor executor = new FakeSvnCommandExecutor("?       module.xml\n", "Committed revision 123456.\n");
        SvnModuleCommitter committer = buildCommitter(executor);

        SvnCommitResult result = committer.commit(
                "job-1",
                tempDir,
                "http://alm.freetech.com",
                "FDP_Demo",
                "10 Stakeholder Requirement",
                "R171e",
                processedModuleXml(),
                "update module.xml");

        assertEquals(Boolean.FALSE, result.getSuccess());
        assertEquals(SvnCommitResult.STATUS_COMMIT_FAILED, result.getStatus());
        assertEquals("?       module.xml", result.getStatusBeforeCommit());
        assertTrue(result.getErrorMessage().contains("Unexpected SVN status"));
        assertEquals(0, executor.getCommitCount());
    }

    private SvnModuleCommitter buildCommitter(FakeSvnCommandExecutor executor) {
        PolarionProperties properties = new PolarionProperties();
        properties.getSvn().setCleanupCommitWorkspace(false);
        ModuleXmlDownloader downloader = new ModuleXmlDownloader(properties, executor);
        return new SvnModuleCommitter(properties, downloader, executor);
    }

    private Path processedModuleXml() throws Exception {
        Path file = tempDir.resolve("module.xml");
        Files.write(file, "<module><field id=\"homePageContent\"><![CDATA[ok]]></field></module>".getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private static class FakeSvnCommandExecutor extends SvnCommandExecutor {

        private final String firstStatusOutput;
        private final String commitOutput;
        private final List<List<String>> commands = new ArrayList<List<String>>();
        private int statusCount;
        private int commitCount;
        private String checkoutTarget;

        FakeSvnCommandExecutor(String firstStatusOutput, String commitOutput) {
            this.firstStatusOutput = firstStatusOutput;
            this.commitOutput = commitOutput;
        }

        @Override
        public String run(List<String> command, Path workDir, int timeoutSeconds, String errorCode) {
            commands.add(new ArrayList<String>(command));
            String action = command.get(1);
            if ("checkout".equals(action)) {
                createCheckout(command.get(4));
                return "Checked out revision 1.\n";
            }
            if ("update".equals(action)) {
                createModuleXml(workDir);
                return "Updated to revision 1.\n";
            }
            if ("status".equals(action)) {
                statusCount++;
                return statusCount == 1 ? firstStatusOutput : "";
            }
            if ("commit".equals(action)) {
                commitCount++;
                return commitOutput;
            }
            return "";
        }

        int getCommitCount() {
            return commitCount;
        }

        String getCheckoutTarget() {
            return checkoutTarget;
        }

        @SuppressWarnings("unused")
        List<List<String>> getCommands() {
            return commands;
        }

        private void createCheckout(String workspaceDir) {
            try {
                this.checkoutTarget = workspaceDir;
                Files.createDirectories(Paths.get(workspaceDir));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private void createModuleXml(Path workspaceDir) {
            try {
                Files.createDirectories(workspaceDir);
                Files.write(workspaceDir.resolve("module.xml"), "<module/>".getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
