package com.example.polarionprocessor.model.polarion;

/**
 * SVN 回写 module.xml 的结果。
 */
public class SvnCommitResult {

    public static final String STATUS_COMMITTED = "COMMITTED";
    public static final String STATUS_NO_CHANGE = "NO_CHANGE";
    public static final String STATUS_COMMIT_FAILED = "COMMIT_FAILED";

    private Boolean success;
    private String status;
    private String revision;
    private String workspaceDir;
    private String statusBeforeCommit;
    private String statusAfterCommit;
    private String errorMessage;

    public static SvnCommitResult committed(String revision) {
        SvnCommitResult result = new SvnCommitResult();
        result.setSuccess(true);
        result.setStatus(STATUS_COMMITTED);
        result.setRevision(revision);
        return result;
    }

    public static SvnCommitResult noChange() {
        SvnCommitResult result = new SvnCommitResult();
        result.setSuccess(true);
        result.setStatus(STATUS_NO_CHANGE);
        return result;
    }

    public static SvnCommitResult failed(String errorMessage) {
        SvnCommitResult result = new SvnCommitResult();
        result.setSuccess(false);
        result.setStatus(STATUS_COMMIT_FAILED);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(String workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public String getStatusBeforeCommit() {
        return statusBeforeCommit;
    }

    public void setStatusBeforeCommit(String statusBeforeCommit) {
        this.statusBeforeCommit = statusBeforeCommit;
    }

    public String getStatusAfterCommit() {
        return statusAfterCommit;
    }

    public void setStatusAfterCommit(String statusAfterCommit) {
        this.statusAfterCommit = statusAfterCommit;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
