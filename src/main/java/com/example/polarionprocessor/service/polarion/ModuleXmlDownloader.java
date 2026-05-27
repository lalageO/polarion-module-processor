package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.service.shared.ModuleProcessException;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 根据 Polarion 项目、模块目录和模块名下载 module.xml。
 */
@Service
public class ModuleXmlDownloader {

    private final PolarionProperties properties;

    public ModuleXmlDownloader(PolarionProperties properties) {
        this.properties = properties;
    }

    /**
     * 下载 module.xml 原文。
     */
    public String download(String projectId, String moduleFolder, String moduleName) {
        String downloadUrl = buildDownloadUrl(projectId, moduleFolder, moduleName);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            applyAuth(connection);

            int status = connection.getResponseCode();
            String content = readResponse(status >= 400 ? connection.getErrorStream() : connection.getInputStream());
            if (status >= 400) {
                throw new ModuleProcessException("MODULE_XML_DOWNLOAD_FAILED",
                        "download module.xml failed, httpStatus=" + status);
            }
            validateModuleXml(content);
            return content;
        } catch (ModuleProcessException e) {
            throw e;
        } catch (IOException e) {
            throw new ModuleProcessException("MODULE_XML_DOWNLOAD_FAILED", "download module.xml failed: " + e.getMessage(), e);
        }
    }

    /**
     * 构造 svnwebclient 的 fileContent.jsp 下载地址。
     */
    public String buildDownloadUrl(String projectId, String moduleFolder, String moduleName) {
        String rawPath = projectId
                + "/modules/"
                + moduleFolder
                + "/"
                + moduleName
                + "/"
                + properties.getModuleFileName();
        String encodedPath = urlEncode(rawPath);
        String baseUrl = properties.getBaseUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/polarion/svnwebclient/fileContent.jsp?url=" + encodedPath;
    }

    /**
     * 基于配置添加下载认证信息。
     */
    protected void applyAuth(HttpURLConnection connection) {
        PolarionProperties.Auth auth = properties.getAuth();
        if (auth == null || !TextUtils.hasText(auth.getType())) {
            return;
        }
        String type = auth.getType().trim().toUpperCase(Locale.ROOT);
        if ("COOKIE".equals(type) && TextUtils.hasText(auth.getCookie())) {
            connection.setRequestProperty("Cookie", auth.getCookie());
        }
    }

    private String urlEncode(String rawPath) {
        try {
            return URLEncoder.encode(rawPath, "UTF-8").replace("+", "%20");
        } catch (IOException e) {
            throw new ModuleProcessException("MODULE_XML_URL_ENCODE_FAILED", "module path encode failed", e);
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } finally {
            reader.close();
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
}
