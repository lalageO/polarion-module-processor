package com.example.polarionprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
    private String defaultWorkItemType = "stakeholderRequirement";

    /** 下载 module.xml 时使用的认证配置。 */
    private Auth auth = new Auth();

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
     * 创建 Polarion Work Item 的 API 配置。
     */
    public static class WorkItemApi {

        /** 是否启用真实 Work Item 创建 API。 */
        private Boolean enabled = false;

        /** 创建 Work Item 的 HTTP 地址。 */
        private String createUrl = "";

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
