package io.cloudbeat.common.config;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CbConfig {
    public static final String CB_API_KEY = "CB_API_KEY";
    public static final String CB_API_ENDPOINT_URL = "CB_API_ENDPOINT_URL";
    public static final String CB_PROJECT_ID = "CB_PROJECT_ID";
    public static final String CB_RUN_ID = "CB_RUN_ID";
    public static final String CB_INSTANCE_ID = "CB_INSTANCE_ID";
    public static final String CB_CAPS_PREFIX = "CB_CAPS.";
    public static final String CB_META_PREFIX = "CB_META.";
    public static final String CB_ENV_PREFIX = "CB_ENV.";
    public static final String CB_OPT_PREFIX = "CB_OPT.";

    final Properties props;
    String apiToken;
    String apiEndpointUrl;
    String runId;
    String instanceId;
    String projectId;
    Map<String, String> metadata;
    Map<String, String> capabilities;
    Map<String, String> envVars;
    Map<String, String> options;

    public CbConfig(Properties props) {
        this.props = props;
        loadConfigFromProps();
    }

    private void loadConfigFromProps() {
        apiToken = props.getProperty(CB_API_KEY);
        apiEndpointUrl = props.getProperty(CB_API_ENDPOINT_URL);
        projectId = props.getProperty(CB_PROJECT_ID);
        runId = props.getProperty(CB_RUN_ID);
        instanceId = props.getProperty(CB_INSTANCE_ID);
        // load capabilities
        loadMapFromPrefixedProps(CB_CAPS_PREFIX, capabilities);
        // load metadata
        loadMapFromPrefixedProps(CB_META_PREFIX, metadata);
        // load options
        loadMapFromPrefixedProps(CB_OPT_PREFIX, options);
        // load environment variables
        loadMapFromPrefixedProps(CB_ENV_PREFIX, envVars);
    }

    private void loadMapFromPrefixedProps(String prefix, Map<String, String> map) {
        final Set<String> propertyNames = props.stringPropertyNames();
        propertyNames.stream()
            .filter(name -> name.startsWith(prefix))
            .forEach(name -> {
                final String noPrefixPropName = name.substring(prefix.length());
                final String propVal = props.getProperty(name);
                this.capabilities.put(noPrefixPropName, propVal);
            });
    }

    public Properties getProperties() {
        return props;
    }

    public Map<String, String> getCapabilities() { return capabilities; }

    public Map<String, String> getMetadata() { return metadata; }

    public Map<String, String> getOptions() { return options; }

    public Map<String, String> getEnvironmentVariables() { return envVars; }

    public String getApiToken() { return apiToken; }

    public String getProjectId() { return projectId; }

    public String getInstanceId() { return instanceId; }

    public String getRunId() { return runId; }
}
