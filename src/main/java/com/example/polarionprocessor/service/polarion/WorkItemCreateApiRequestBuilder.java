package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.model.polarion.PolarionCustomFieldRequest;
import com.example.polarionprocessor.model.polarion.PolarionEnumOptionRequest;
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
            "status",
            "wkid",
            "type",
            "title",
            "description",
            "cdescription",
            "cDescription".toLowerCase(Locale.ROOT),
            "authorname",
            "authorid",
            "assigneeids",
            "duedate",
            "startdate",
            "parentwkid",
            "isnewpdp",
            "onlycreate",
            "commentcontent",
            "removedlink",
            "initialestimate",
            "timespent",
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
        apiRequest.setStatus(safeRequest.getStatus());
        apiRequest.setWkId(safeRequest.getWkId());
        apiRequest.setPolarionId(firstText(
                safeRequest.getProjectId(),
                api == null ? null : api.getDefaultPolarionId(),
                properties.getDefaultProjectId()));
        apiRequest.setType(normalizeWorkItemType(firstText(
                safeRequest.getType(),
                api == null ? null : api.getDefaultType(),
                properties.getDefaultWorkItemType())));
        apiRequest.setTitle(safeRequest.getTitle());
        apiRequest.setAuthorName(safeRequest.getAuthorName());
        apiRequest.setAuthorId(firstText(safeRequest.getAuthorId(), api == null ? null : api.getDefaultAuthorId()));
        apiRequest.setAssigneeIds(copyStringList(safeRequest.getAssigneeIds()));
        apiRequest.setDueDate(safeRequest.getDueDate());
        apiRequest.setStartDate(safeRequest.getStartDate());
        apiRequest.setParentWkId(safeRequest.getParentWkId());
        apiRequest.setIsNewPdp(safeRequest.getIsNewPdp() == null ? Boolean.FALSE : safeRequest.getIsNewPdp());
        apiRequest.setOnlyCreate(safeRequest.getOnlyCreate() == null ? Boolean.TRUE : safeRequest.getOnlyCreate());
        apiRequest.setCommentContent(safeRequest.getCommentContent());
        apiRequest.setRemovedLink(safeRequest.getRemovedLink() == null ? Boolean.FALSE : safeRequest.getRemovedLink());
        apiRequest.setCdescription(safeRequest.getDescription());
        apiRequest.setInitialEstimate(safeRequest.getInitialEstimate());
        apiRequest.setTimeSpent(safeRequest.getTimeSpent());
        applyTopLevelFields(apiRequest, safeRequest.getFields());
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
            appendCustomFields(merged, projectCustomFields(api, safeRequest.getProjectId()));
        }
        appendFields(merged, safeRequest.getFields());
        appendCustomFields(merged, safeRequest.getCustomFields());
        return materializeCustomFields(merged);
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
        String fieldId = canonicalCustomFieldId(field.getId());
        PolarionCustomFieldRequest existing = merged.get(fieldId);
        PolarionCustomFieldRequest candidate = mergeCustomField(existing, field);
        candidate.setId(fieldId);
        if (!TextUtils.hasText(candidate.getType())) {
            return;
        }
        merged.put(fieldId, candidate);
    }

    private List<PolarionCustomFieldRequest> projectCustomFields(PolarionProperties.WorkItemApi api, String projectId) {
        if (api == null || api.getProjectCustomFields() == null || !TextUtils.hasText(projectId)) {
            return null;
        }
        List<PolarionCustomFieldRequest> fields = api.getProjectCustomFields().get(projectId);
        if (fields != null) {
            return fields;
        }
        for (Map.Entry<String, List<PolarionCustomFieldRequest>> entry : api.getProjectCustomFields().entrySet()) {
            if (entry.getKey() != null && projectId.trim().equalsIgnoreCase(entry.getKey().trim())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private PolarionCustomFieldRequest toCustomField(String key, Object rawValue) {
        String fieldId = canonicalCustomFieldId(key);
        if (rawValue instanceof PolarionCustomFieldRequest) {
            PolarionCustomFieldRequest source = (PolarionCustomFieldRequest) rawValue;
            PolarionCustomFieldRequest target = copy(source);
            if (!TextUtils.hasText(target.getId())) {
                target.setId(fieldId);
            }
            return target;
        }
        if (rawValue instanceof Map) {
            return mapToCustomField(fieldId, (Map<?, ?>) rawValue);
        }
        String type = KNOWN_SIMPLE_FIELD_TYPES.get(fieldId.toLowerCase(Locale.ROOT));
        if (!TextUtils.hasText(type)) {
            return null;
        }
        return new PolarionCustomFieldRequest(fieldId, Boolean.FALSE, type, rawValue);
    }

    private PolarionCustomFieldRequest mapToCustomField(String key, Map<?, ?> valueMap) {
        String id = canonicalCustomFieldId(stringValue(valueMap.containsKey("id") ? valueMap.get("id") : key));
        if (!TextUtils.hasText(id)) {
            return null;
        }
        String canonicalKey = canonicalCustomFieldId(key);
        String type = firstText(
                stringValue(valueMap.get("type")),
                KNOWN_SIMPLE_FIELD_TYPES.get(id.toLowerCase(Locale.ROOT)),
                TextUtils.hasText(canonicalKey)
                        ? KNOWN_SIMPLE_FIELD_TYPES.get(canonicalKey.toLowerCase(Locale.ROOT))
                        : null);
        Object multiValue = valueMap.get("multi");
        Object fieldValue = valueMap.containsKey("value") ? valueMap.get("value") : null;
        PolarionCustomFieldRequest field = new PolarionCustomFieldRequest(
                id,
                toBoolean(multiValue),
                type,
                fieldValue);
        field.setEnumId(stringValue(valueMap.get("enumId")));
        field.setName(stringValue(valueMap.get("name")));
        field.setRequired(valueMap.containsKey("required") ? toBoolean(valueMap.get("required")) : null);
        field.setEnumOptions(toEnumOptions(valueMap.get("enumOptions")));
        return field;
    }

    private PolarionCustomFieldRequest copy(PolarionCustomFieldRequest source) {
        PolarionCustomFieldRequest copy = new PolarionCustomFieldRequest();
        copy.setId(canonicalCustomFieldId(source.getId()));
        copy.setEnumId(source.getEnumId());
        copy.setMulti(source.getMulti());
        copy.setName(source.getName());
        copy.setRequired(source.getRequired());
        copy.setType(source.getType());
        copy.setEnumOptions(copyEnumOptions(source.getEnumOptions()));
        copy.setValue(source.getValue());
        return copy;
    }

    private PolarionCustomFieldRequest mergeCustomField(PolarionCustomFieldRequest existing,
                                                        PolarionCustomFieldRequest incoming) {
        if (existing == null) {
            return copy(incoming);
        }
        PolarionCustomFieldRequest source = copy(incoming);
        PolarionCustomFieldRequest merged = copy(existing);
        merged.setEnumId(firstText(source.getEnumId(), merged.getEnumId()));
        merged.setMulti(source.getMulti() == null ? merged.getMulti() : source.getMulti());
        merged.setName(firstText(source.getName(), merged.getName()));
        merged.setRequired(source.getRequired() == null ? merged.getRequired() : source.getRequired());
        merged.setType(firstText(source.getType(), merged.getType()));
        if (source.getEnumOptions() != null && !source.getEnumOptions().isEmpty()) {
            merged.setEnumOptions(copyEnumOptions(source.getEnumOptions()));
        }
        if (hasCustomFieldValue(source)) {
            merged.setValue(source.getValue());
        }
        return merged;
    }

    private List<PolarionCustomFieldRequest> materializeCustomFields(LinkedHashMap<String, PolarionCustomFieldRequest> merged) {
        List<PolarionCustomFieldRequest> customFields = new ArrayList<PolarionCustomFieldRequest>();
        for (PolarionCustomFieldRequest field : merged.values()) {
            if (hasCustomFieldValue(field)) {
                customFields.add(copy(field));
            }
        }
        return customFields;
    }

    private boolean hasCustomFieldValue(PolarionCustomFieldRequest field) {
        if (field == null) {
            return false;
        }
        Object value = field.getValue();
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return TextUtils.hasText((String) value);
        }
        if (value instanceof Iterable<?>) {
            return ((Iterable<?>) value).iterator().hasNext();
        }
        return true;
    }

    private String canonicalCustomFieldId(String id) {
        if (!TextUtils.hasText(id)) {
            return id;
        }
        String trimmed = id.trim();
        if ("requirementsouce".equalsIgnoreCase(trimmed)) {
            return "requirementsource";
        }
        return trimmed;
    }

    private void applyTopLevelFields(WorkItemCreateApiRequest apiRequest, Map<String, Object> fields) {
        if (fields == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!TextUtils.hasText(entry.getKey())) {
                continue;
            }
            applyTopLevelField(apiRequest, entry.getKey(), entry.getValue());
        }
    }

    private void applyTopLevelField(WorkItemCreateApiRequest apiRequest, String key, Object value) {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        if ("project".equals(normalizedKey) || "projectid".equals(normalizedKey) || "polarionid".equals(normalizedKey)) {
            apiRequest.setPolarionId(stringValue(value));
        } else if ("status".equals(normalizedKey)) {
            apiRequest.setStatus(stringValue(value));
        } else if ("wkid".equals(normalizedKey)) {
            apiRequest.setWkId(stringValue(value));
        } else if ("type".equals(normalizedKey)) {
            apiRequest.setType(normalizeWorkItemType(stringValue(value)));
        } else if ("title".equals(normalizedKey)) {
            apiRequest.setTitle(stringValue(value));
        } else if ("description".equals(normalizedKey)
                || "cdescription".equals(normalizedKey)) {
            apiRequest.setCdescription(stringValue(value));
        } else if ("authorname".equals(normalizedKey)) {
            apiRequest.setAuthorName(stringValue(value));
        } else if ("authorid".equals(normalizedKey)) {
            apiRequest.setAuthorId(stringValue(value));
        } else if ("assigneeids".equals(normalizedKey)) {
            apiRequest.setAssigneeIds(toStringList(value));
        } else if ("duedate".equals(normalizedKey)) {
            apiRequest.setDueDate(stringValue(value));
        } else if ("startdate".equals(normalizedKey)) {
            apiRequest.setStartDate(stringValue(value));
        } else if ("parentwkid".equals(normalizedKey)) {
            apiRequest.setParentWkId(stringValue(value));
        } else if ("isnewpdp".equals(normalizedKey)) {
            apiRequest.setIsNewPdp(toBoolean(value));
        } else if ("onlycreate".equals(normalizedKey)) {
            apiRequest.setOnlyCreate(toBoolean(value));
        } else if ("commentcontent".equals(normalizedKey)) {
            apiRequest.setCommentContent(stringValue(value));
        } else if ("removedlink".equals(normalizedKey)) {
            apiRequest.setRemovedLink(toBoolean(value));
        } else if ("initialestimate".equals(normalizedKey)) {
            apiRequest.setInitialEstimate(stringValue(value));
        } else if ("timespent".equals(normalizedKey)) {
            apiRequest.setTimeSpent(stringValue(value));
        }
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

    private List<String> copyStringList(List<String> values) {
        return values == null ? null : new ArrayList<String>(values);
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return null;
        }
        List<String> values = new ArrayList<String>();
        if (value instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) value) {
                if (item != null) {
                    values.add(String.valueOf(item));
                }
            }
        } else {
            values.add(String.valueOf(value));
        }
        return values;
    }

    private List<PolarionEnumOptionRequest> toEnumOptions(Object value) {
        if (!(value instanceof Iterable<?>)) {
            return null;
        }
        List<PolarionEnumOptionRequest> enumOptions = new ArrayList<PolarionEnumOptionRequest>();
        for (Object item : (Iterable<?>) value) {
            PolarionEnumOptionRequest option = toEnumOption(item);
            if (option != null) {
                enumOptions.add(option);
            }
        }
        return enumOptions.isEmpty() ? null : enumOptions;
    }

    private PolarionEnumOptionRequest toEnumOption(Object item) {
        if (item instanceof PolarionEnumOptionRequest) {
            PolarionEnumOptionRequest source = (PolarionEnumOptionRequest) item;
            return new PolarionEnumOptionRequest(source.getId(), source.getName());
        }
        if (!(item instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> itemMap = (Map<?, ?>) item;
        return new PolarionEnumOptionRequest(
                stringValue(itemMap.get("id")),
                stringValue(itemMap.get("name")));
    }

    private List<PolarionEnumOptionRequest> copyEnumOptions(List<PolarionEnumOptionRequest> source) {
        if (source == null) {
            return null;
        }
        List<PolarionEnumOptionRequest> copy = new ArrayList<PolarionEnumOptionRequest>();
        for (PolarionEnumOptionRequest option : source) {
            if (option != null) {
                copy.add(new PolarionEnumOptionRequest(option.getId(), option.getName()));
            }
        }
        return copy;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Map<String, String> buildKnownSimpleFieldTypes() {
        Map<String, String> types = new HashMap<String, String>();
        types.put("verificationcriteria", "text/html");
        types.put("requirementsource", "EnumOptionId");
        types.put("reqtype", "EnumOptionId");
        types.put("asil", "EnumOptionId");
        types.put("cal", "EnumOptionId");
        types.put("targetversion", "EnumOptionId");
        return types;
    }
}
