package io.cloudbeat.common.reporter.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cloudbeat.common.reporter.serializer.EpochTimeSerializer;
import io.cloudbeat.common.reporter.serializer.TestStatusSerializer;

import java.util.*;

public class TestResult {
    String instanceId;
    String runId;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long startTime;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long endTime;
    long duration;
    Map<String, String> options;
    Map<String, String> capabilities;
    Map<String, String> attributes = new HashMap<>();
    Map<String, String> metaData;
    Map<String, String> environmentVariables;
    FailureResult failure;
    ArrayList<SuiteResult> suites = new ArrayList<>();
    @JsonSerialize(using = TestStatusSerializer.class)
    TestStatus status;

    public TestResult(final String runId, final String instanceId) {
        this(runId, instanceId, null, null, null, null);
    }

    public TestResult(
            final String runId,
            final String instanceId,
            final Map<String, String> capabilities,
            final Map<String, String> options,
            final Map<String, String> metaData,
            final Map<String, String> environmentVariables
    ) {
        this.runId = runId;
        this.instanceId = instanceId;
        this.capabilities = capabilities != null ? capabilities : new HashMap<>();
        this.options = options;
        this.metaData = metaData;
        this.environmentVariables = environmentVariables;
        startTime = Calendar.getInstance().getTime().getTime();
    }

    public void end(TestStatus status) {
        endTime = Calendar.getInstance().getTime().getTime();
        this.duration = startTime - endTime;
        this.status = status;
    }

    public SuiteResult addNewSuite(final String name) {
        SuiteResult newSuite = new SuiteResult(name);
        suites.add(newSuite);
        return newSuite;
    }

    public void addAttribute(final String name, final String value) {
        this.attributes.put(name, value);
    }

    public void addCapability(final String name, final String value) {
        this.capabilities.put(name, value);
    }

    public Optional<SuiteResult> lastSuite() {
        if (suites.size() > 0)
            return Optional.of(suites.get(suites.size() - 1));
        return Optional.empty();
    }

    public Optional<SuiteResult> lastSuite(String fqn) {
        for (int i = suites.size(); i-- > 0; ) {
            final SuiteResult suite = suites.get(i);
            final String suiteFqn = suite.getFqn();
            if (suiteFqn != null && suiteFqn.equals(fqn))
                return Optional.of(suite);
        }
        return Optional.empty();
    }

    /* Getters */

    public String getRunId() { return runId; }

    public String getInstanceId() { return instanceId; }

    public long getStartTime() { return startTime; }

    public long getEndTime() { return endTime; }

    public List<SuiteResult> getSuites() { return suites; }

    public Map<String, String> getOptions() {
        return options;
    }

    public Map<String, String> getCapabilities() {
        return capabilities;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public FailureResult getFailure() {
        return failure;
    }

    public TestStatus getStatus() {
        return status;
    }
}
