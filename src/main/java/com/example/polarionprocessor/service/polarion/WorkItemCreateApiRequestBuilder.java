package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.model.polarion.PolarionCustomFieldRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateApiRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateRequest;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 将内部 WorkItemCreateRequest 映射为公司内部 createItem 接口请求体。
 */
@Service
public class WorkItemCreateApiRequestBuilder {

    private static final Set<String> RESERVED_FIELD_KEYS = new HashSet<String>(Arrays.asList(
            "project",
            "projectid",
            "polarionid",
            "type",
            "title",
            "description",
            "cdescription",
            "authorid",
            "isnewpdp",
            "onlycreate",
            "customfields"));

    private static final Map<String, String> KNOWN_SIMPLE_FIELD_TYPES = buildKnownSimpleFieldTypes();

    private final PolarionProperties properties;

    public WorkItemCreateApiRequestBuilder(PolarionProperties properties) {
        this.properties = properties;
    }

    public WorkItemCreateApiRequest build(WorkItemCreateRequest request) {
        WorkItemCreateRequest safeRequest = request == null ? new WorkItemCreateRequest() : request;
        PolarionProperties.WorkItemApi api = properties.getWorkItemApi();

        WorkItemCreateApiRequest apiRequest = new WorkItemCreateApiRequest();
        apiRequest.setPolarionId(firstText(
                safeRequest.getProjectId(),
                api == null ? null : api.getDefaultPolarionId(),
                properties.getDefaultProjectId()));
        apiRequest.setType(normalizeWorkItemType(firstText(
                safeRequest.getType(),
                api == null ? null : api.getDefaultType(),
                properties.getDefaultWorkItemType())));
        apiRequest.setTitle(safeRequest.getTitle());
        apiRequest.setAuthorId(firstText(safeRequest.getAuthorId(), api == null ? null : api.getDefaultAuthorId()));
        apiRequest.setIsNewPdp(Boolean.FALSE);
        apiRequest.setOnlyCreate(Boolean.TRUE);
        apiRequest.setCdescription(safeRequest.getDescription());
        apiRequest.setCustomFields(buildCustomFields(safeRequest));
        return apiRequest;
    }

    public List<PolarionCustomFieldRequest> buildCustomFields(WorkItemCreateRequest request) {
        WorkItemCreateRequest safeRequest = request == null ? new WorkItemCreateRequest() : request;
        PolarionProperties.WorkItemApi api = properties.getWorkItemApi();
        LinkedHashMap<String, PolarionCustomFieldRequest> merged =
                new LinkedHashMap<String, PolarionCustomFieldRequest>();

        if (api != null) {
            appendCustomFields(merged, api.getDefaultCustomFields());
        }
        appendFields(merged, safeRequest.getFields());
        appendCustomFields(merged, safeRequest.getCustomFields());
        return new ArrayList<PolarionCustomFieldRequest>(merged.values());
    }

    private void appendCustomFields(LinkedHashMap<String, PolarionCustomFieldRequest> merged,
                                    List<PolarionCustomFieldRequest> customFields) {
        if (customFields == null) {
            return;
        }
        for (PolarionCustomFieldRequest field : customFields) {
            appendCustomField(merged, field);
        }
    }

    private void appendFields(LinkedHashMap<String, PolarionCustomFieldRequest> merged,
                              Map<String, Object> fields) {
        if (fields == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!TextUtils.hasText(entry.getKey()) || isReservedFieldKey(entry.getKey())) {
                continue;
            }
            appendCustomField(merged, toCustomField(entry.getKey(), entry.getValue()));
        }
    }

    private void appendCustomField(LinkedHashMap<String, PolarionCustomFieldRequest> merged,
                                   PolarionCustomFieldRequest field) {
        if (field == null || !TextUtils.hasText(field.getId())) {
            return;
        }
        if (!TextUtils.hasText(field.getType())) {
            return;
        }
        merged.put(field.getId().trim(), copy(field));
    }

    private PolarionCustomFieldRequest toCustomField(String key, Object rawValue) {
        if (rawValue instanceof PolarionCustomFieldRequest) {
            PolarionCustomFieldRequest source = (PolarionCustomFieldRequest) rawValue;
            PolarionCustomFieldRequest target = copy(source);
            if (!TextUtils.hasText(target.getId())) {
                target.setId(key);
            }
            return target;
        }
        if (rawValue instanceof Map) {
            return mapToCustomField(key, (Map<?, ?>) rawValue);
        }
        String type = KNOWN_SIMPLE_FIELD_TYPES.get(key.toLowerCase(Locale.ROOT));
        if (!TextUtils.hasText(type)) {
            return null;
        }
        return new PolarionCustomFieldRequest(key, Boolean.FALSE, type, rawValue);
    }

    private PolarionCustomFieldRequest mapToCustomField(String key, Map<?, ?> valueMap) {
        String id = stringValue(valueMap.containsKey("id") ? valueMap.get("id") : key);
        String type = stringValue(valueMap.get("type"));
        if (!TextUtils.hasText(id) || !TextUtils.hasText(type)) {
            return null;
        }
        Object multiValue = valueMap.get("multi");
        Object fieldValue = valueMap.containsKey("value") ? valueMap.get("value") : null;
        return new PolarionCustomFieldRequest(
                id,
                toBoolean(multiValue),
                type,
                fieldValue);
    }

    private PolarionCustomFieldRequest copy(PolarionCustomFieldRequest source) {
        PolarionCustomFieldRequest copy = new PolarionCustomFieldRequest();
        copy.setId(source.getId() == null ? null : source.getId().trim());
        copy.setMulti(source.getMulti());
        copy.setType(source.getType());
        copy.setValue(source.getValue());
        return copy;
    }

    private boolean isReservedFieldKey(String key) {
        return RESERVED_FIELD_KEYS.contains(key.toLowerCase(Locale.ROOT));
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return Boolean.FALSE;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.valueOf(String.valueOf(value));
    }

    private String normalizeWorkItemType(String type) {
        return TextUtils.hasText(type) ? type.trim().toLowerCase(Locale.ROOT) : type;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (TextUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Map<String, String> buildKnownSimpleFieldTypes() {
        Map<String, String> types = new HashMap<String, String>();
        types.put("verificationcriteria", "text/html");
        types.put("requirementsource", "EnumOptionId");
        types.put("reqtype", "EnumOptionId");
        types.put("status", "EnumOptionId");
        types.put("asil", "EnumOptionId");
        types.put("cal", "EnumOptionId");
        types.put("targetversion", "EnumOptionId");
        return types;
    }
}
