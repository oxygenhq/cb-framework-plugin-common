package io.cloudbeat.common.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CbConfig {
    public String runId;
    public String instanceId;
    public Map<String, String> metadata;
    public Map<String, String> capabilities;
    public Map<String, String> environmentVariables;
    public Map<String, String> options;

    public Map<String, Case> cases = new HashMap<>();

    public static class Case {
        public long id;
        public String name;
        public int order;
    }

    public static CbConfig load(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String json = new String(encoded, StandardCharsets.UTF_8);

        CbConfig config = new CbConfig();

        final ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(json, JsonNode.class);
        TypeReference<Map<String, Object>> mapTypeRef = new TypeReference<Map<String, Object>>() {};
        TypeReference<Map<String, String>> mapStringTypeRef = new TypeReference<Map<String, String>>() {};

        config.runId = rootNode.get("RunId").textValue();
        config.instanceId = rootNode.get("InstanceId").textValue();
        config.capabilities = mapper.readValue(rootNode.get("Capabilities").toString(), mapTypeRef);
        config.metadata = mapper.readValue(rootNode.get("Metadata").toString(), mapTypeRef);
        config.environmentVariables = mapper.readValue(rootNode.get("EnvironmentVariables").toString(), mapTypeRef);
        config.options = mapper.readValue(rootNode.get("Options").toString(), mapStringTypeRef);

        for (JsonNode caseNode : rootNode.get("Cases")) {
            CbConfig.Case caze = new CbConfig.Case();
            caze.id = caseNode.get("Id").asLong();
            caze.name = caseNode.get("Name").asText();
            caze.order = caseNode.get("Order").asInt();
            config.cases.put(caze.name, caze);
        }

        return config;
    }
}
