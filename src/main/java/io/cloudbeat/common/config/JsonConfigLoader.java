package io.cloudbeat.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonConfigLoader {
    public static CbConfig load(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String json = new String(encoded, StandardCharsets.UTF_8);

        CbConfig config = new CbConfig();

        final ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(json, JsonNode.class);
        TypeReference<Map<String, Object>> mapTypeRef = new TypeReference<Map<String, Object>>() {};
        TypeReference<Map<String, String>> mapStringTypeRef = new TypeReference<Map<String, String>>() {};
        TypeReference<List<String>> listStringTypeRef = new TypeReference<List<String>>() {};

        config.runId = rootNode.get("RunId").textValue();
        config.instanceId = rootNode.get("InstanceId").textValue();
        config.instanceId = rootNode.get("InstanceId").textValue();
        config.capabilities = mapper.readValue(rootNode.get("Capabilities").toString(), mapTypeRef);
        config.metadata = mapper.readValue(rootNode.get("Metadata").toString(), mapTypeRef);
        config.envVars = mapper.readValue(rootNode.get("EnvironmentVariables").toString(), mapTypeRef);
        config.options = mapper.readValue(rootNode.get("Options").toString(), mapStringTypeRef);
        config.tags = mapper.readValue(rootNode.get("Tags").toString(), listStringTypeRef);
        // if cases list is specified, map it to a simple list of cases FQNs
        if (rootNode.has("Cases")) {
            config.cases = new ArrayList<>();
            for (JsonNode caseNode : rootNode.get("Cases")) {
                if (caseNode.get("Fqn") != null)
                    config.cases.add(caseNode.get("Fqn").asText());
                else if (caseNode.get("Details") != null) {
                    if (caseNode.get("Details").get("FullyQualifiedName") != null)
                        config.cases.add(caseNode.get("Details").get("FullyQualifiedName").asText());
                }
            }
        }

        return config;
    }
}
