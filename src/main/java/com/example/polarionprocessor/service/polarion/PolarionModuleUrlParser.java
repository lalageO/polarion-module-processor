package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.model.polarion.PolarionModuleLocation;
import com.example.polarionprocessor.service.shared.ModuleProcessException;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析 Polarion wiki 模块 URL。
 */
@Service
public class PolarionModuleUrlParser {

    private static final String PROJECT_MARKER = "project";
    private static final String WIKI_MARKER = "wiki";

    /**
     * 从形如 /polarion/#/project/{projectId}/wiki/{folder}/{moduleName} 的 URL 中提取模块位置。
     */
    public PolarionModuleLocation parse(String moduleUrl) {
        if (!TextUtils.hasText(moduleUrl)) {
            throw new ModuleProcessException("MODULE_URL_EMPTY", "moduleUrl is required");
        }
        URI uri = parseUri(moduleUrl.trim());
        String rawFragment = uri.getRawFragment();
        if (!TextUtils.hasText(rawFragment)) {
            throw new ModuleProcessException("MODULE_URL_INVALID", "Polarion module URL must contain hash route");
        }

        List<String> segments = decodeSegments(rawFragment);
        int projectIndex = segments.indexOf(PROJECT_MARKER);
        int wikiIndex = segments.indexOf(WIKI_MARKER);
        if (projectIndex < 0 || wikiIndex < 0 || wikiIndex <= projectIndex + 1 || wikiIndex + 2 > segments.size()) {
            throw new ModuleProcessException("MODULE_URL_INVALID", "Polarion module URL route is not supported");
        }

        PolarionModuleLocation location = new PolarionModuleLocation();
        location.setBaseUrl(buildBaseUrl(uri));
        location.setProjectId(segments.get(projectIndex + 1));
        location.setModuleName(segments.get(segments.size() - 1));
        location.setModuleFolder(joinPathSegments(segments.subList(wikiIndex + 1, segments.size() - 1)));
        validate(location);
        location.setModuleURI(buildModuleURI(location.getProjectId(), location.getModuleFolder(), location.getModuleName()));
        return location;
    }

    public String buildModuleURI(String projectId, String moduleFolder, String moduleName) {
        if (!TextUtils.hasText(projectId) || !TextUtils.hasText(moduleFolder) || !TextUtils.hasText(moduleName)) {
            return null;
        }
        return "subterra:data-service:objects:/default/"
                + projectId.trim()
                + "${Module}"
                + "{moduleFolder}"
                + directParentFolder(moduleFolder)
                + "#"
                + moduleName.trim();
    }

    private String directParentFolder(String moduleFolder) {
        String trimmed = moduleFolder.trim();
        int slashIndex = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
        return slashIndex < 0 ? trimmed : trimmed.substring(slashIndex + 1);
    }

    private URI parseUri(String moduleUrl) {
        try {
            return new URI(moduleUrl);
        } catch (URISyntaxException e) {
            throw new ModuleProcessException("MODULE_URL_INVALID", "Polarion module URL syntax is invalid", e);
        }
    }

    private List<String> decodeSegments(String rawFragment) {
        String normalized = rawFragment.startsWith("/") ? rawFragment.substring(1) : rawFragment;
        String[] rawSegments = normalized.split("/");
        List<String> segments = new ArrayList<String>();
        for (String rawSegment : rawSegments) {
            if (!TextUtils.hasText(rawSegment)) {
                continue;
            }
            segments.add(urlDecode(rawSegment));
        }
        return segments;
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ModuleProcessException("MODULE_URL_INVALID", "Polarion module URL decode failed", e);
        }
    }

    private String buildBaseUrl(URI uri) {
        if (!TextUtils.hasText(uri.getScheme()) || !TextUtils.hasText(uri.getRawAuthority())) {
            throw new ModuleProcessException("MODULE_URL_INVALID", "Polarion module URL must contain scheme and host");
        }
        return uri.getScheme() + "://" + uri.getRawAuthority();
    }

    private String joinPathSegments(List<String> segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(segment);
        }
        return builder.toString();
    }

    private void validate(PolarionModuleLocation location) {
        if (!TextUtils.hasText(location.getProjectId())
                || !TextUtils.hasText(location.getModuleFolder())
                || !TextUtils.hasText(location.getModuleName())) {
            throw new ModuleProcessException("MODULE_URL_INVALID", "Polarion module URL is missing project, folder or module name");
        }
    }
}
