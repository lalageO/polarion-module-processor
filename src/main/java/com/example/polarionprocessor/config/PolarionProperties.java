package com.example.polarionprocessor.config;

import com.example.polarionprocessor.model.polarion.PolarionCustomFieldRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Polarion 正式业务链路相关配置。
 */
@ConfigurationProperties(prefix = "polarion")
public class PolarionProperties {

    /** Polarion 站点基础地址，例如 http://alm.freetech.com。 */
    private String baseUrl = "http://alm.freetech.com";

    /** 模块文件名，默认 module.xml。 */
    private String moduleFileName = "module.xml";

    /** 正式接口未传 projectId 时使用的默认项目 id。 */
    private String defaultProjectId = "FDP_Demo";

    /** 正式接口未传 moduleFolder 时使用的默认模块目录。 */
    private String defaultModuleFolder = "10 Stakeholder Requirement";

    /** 正式接口未传 workItemType 时使用的默认 Work Item 类型。 */
    private String defaultWorkItemType = "stakeholderrequirement";

    /** 下载 module.xml 时使用的认证配置。 */
    private Auth auth = new Auth();

    /** 通过 SVN 拉取 module.xml 时使用的命令配置。 */
    private Svn svn = new Svn();

    /** 创建 Work Item 时使用的 API 配置。 */
    private WorkItemApi workItemApi = new WorkItemApi();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModuleFileName() {
        return moduleFileName;
    }

    public void setModuleFileName(String moduleFileName) {
        this.moduleFileName = moduleFileName;
    }

    public String getDefaultProjectId() {
        return defaultProjectId;
    }

    public void setDefaultProjectId(String defaultProjectId) {
        this.defaultProjectId = defaultProjectId;
    }

    public String getDefaultModuleFolder() {
        return defaultModuleFolder;
    }

    public void setDefaultModuleFolder(String defaultModuleFolder) {
        this.defaultModuleFolder = defaultModuleFolder;
    }

    public String getDefaultWorkItemType() {
        return defaultWorkItemType;
    }

    public void setDefaultWorkItemType(String defaultWorkItemType) {
        this.defaultWorkItemType = defaultWorkItemType;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Svn getSvn() {
        return svn;
    }

    public void setSvn(Svn svn) {
        this.svn = svn;
    }

    public WorkItemApi getWorkItemApi() {
        return workItemApi;
    }

    public void setWorkItemApi(WorkItemApi workItemApi) {
        this.workItemApi = workItemApi;
    }

    /**
     * 下载 module.xml 的认证参数。
     */
    public static class Auth {

        /** 认证类型，第二版先支持 NONE 和 COOKIE。 */
        private String type = "NONE";

        /** COOKIE 认证时使用的 Cookie 请求头内容。 */
        private String cookie = "";

        /** 预留 BASIC 用户名。 */
        private String username = "";

        /** 预留 BASIC 密码。 */
        private String password = "";

        /** 预留 BEARER token。 */
        private String token = "";

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCookie() {
            return cookie;
        }

        public void setCookie(String cookie) {
            this.cookie = cookie;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * SVN 命令行拉取 module.xml 的参数。
     */
    public static class Svn {

        /** SVN 可执行文件名或完整路径。 */
        private String executable = "svn";

        /** SVN 用户名。 */
        private String username = "polarion";

        /** SVN 密码。生产环境应通过外部配置提供。 */
        private String password = "";

        /** checkout 命令超时时间。 */
        private Integer checkoutTimeoutSeconds = 60;

        /** update module.xml 命令超时时间。 */
        private Integer updateTimeoutSeconds = 60;

        /** commit module.xml 命令超时时间。 */
        private Integer commitTimeoutSeconds = 60;

        /** 默认 SVN 提交信息。 */
        private String defaultCommitMessage = "update module.xml";

        /** 拉取完成后是否删除临时 checkout 目录。 */
        private Boolean cleanupTempDir = true;

        /** 提交完成后是否删除 output/{jobId}/svn-workspace。默认保留便于排查。 */
        private Boolean cleanupCommitWorkspace = false;

        public String getExecutable() {
            return executable;
        }

        public void setExecutable(String executable) {
            this.executable = executable;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getCheckoutTimeoutSeconds() {
            return checkoutTimeoutSeconds;
        }

        public void setCheckoutTimeoutSeconds(Integer checkoutTimeoutSeconds) {
            this.checkoutTimeoutSeconds = checkoutTimeoutSeconds;
        }

        public Integer getUpdateTimeoutSeconds() {
            return updateTimeoutSeconds;
        }

        public void setUpdateTimeoutSeconds(Integer updateTimeoutSeconds) {
            this.updateTimeoutSeconds = updateTimeoutSeconds;
        }

        public Integer getCommitTimeoutSeconds() {
            return commitTimeoutSeconds;
        }

        public void setCommitTimeoutSeconds(Integer commitTimeoutSeconds) {
            this.commitTimeoutSeconds = commitTimeoutSeconds;
        }

        public String getDefaultCommitMessage() {
            return defaultCommitMessage;
        }

        public void setDefaultCommitMessage(String defaultCommitMessage) {
            this.defaultCommitMessage = defaultCommitMessage;
        }

        public Boolean getCleanupTempDir() {
            return cleanupTempDir;
        }

        public void setCleanupTempDir(Boolean cleanupTempDir) {
            this.cleanupTempDir = cleanupTempDir;
        }

        public Boolean getCleanupCommitWorkspace() {
            return cleanupCommitWorkspace;
        }

        public void setCleanupCommitWorkspace(Boolean cleanupCommitWorkspace) {
            this.cleanupCommitWorkspace = cleanupCommitWorkspace;
        }
    }

    /**
     * 创建 Polarion Work Item 的 API 配置。
     */
    public static class WorkItemApi {

        /** 是否启用真实 Work Item 创建 API。 */
        private Boolean enabled = true;

        /** 创建 Work Item 的 HTTP 地址。 */
        private String createUrl = "http://10.179.60.154:30000/workitem/ws/create";

        /** 请求体 polarionId 的默认值。 */
        private String defaultPolarionId = "FDP_Demo";

        /** 请求体 type 的默认值。 */
        private String defaultType = "stakeholderrequirement";

        /** 请求体 authorId 的默认值。 */
        private String defaultAuthorId = "yiming.yuan";

        /** HTTP 连接超时时间。 */
        private Integer connectTimeoutMs = 5000;

        /** HTTP 读取超时时间。 */
        private Integer readTimeoutMs = 30000;

        /** 默认 customFields，先于请求字段加入。 */
        private List<PolarionCustomFieldRequest> defaultCustomFields =
                new ArrayList<PolarionCustomFieldRequest>();

        /** 创建 API 的认证类型，当前仅预留。 */
        private String authType = "NONE";

        /** 创建 API 的 token，当前仅预留。 */
        private String token = "";

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getCreateUrl() {
            return createUrl;
        }

        public void setCreateUrl(String createUrl) {
            this.createUrl = createUrl;
        }

        public String getDefaultPolarionId() {
            return defaultPolarionId;
        }

        public void setDefaultPolarionId(String defaultPolarionId) {
            this.defaultPolarionId = defaultPolarionId;
        }

        public String getDefaultType() {
            return defaultType;
        }

        public void setDefaultType(String defaultType) {
            this.defaultType = defaultType;
        }

        public String getDefaultAuthorId() {
            return defaultAuthorId;
        }

        public void setDefaultAuthorId(String defaultAuthorId) {
            this.defaultAuthorId = defaultAuthorId;
        }

        public Integer getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(Integer connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public Integer getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(Integer readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public List<PolarionCustomFieldRequest> getDefaultCustomFields() {
            return defaultCustomFields;
        }

        public void setDefaultCustomFields(List<PolarionCustomFieldRequest> defaultCustomFields) {
            this.defaultCustomFields = defaultCustomFields == null
                    ? new ArrayList<PolarionCustomFieldRequest>()
                    : defaultCustomFields;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
