package io.cloudbeat.common.model;

import io.cloudbeat.common.reporter.model.FailureResult;

import java.util.List;
import java.util.Map;

public class ResultModel extends TestResultBase {
    public Map<String, String> options;
    public Map<String, String> capabilities;
    public Map<String, String> metadata;
    public Map<String, String> environmentVariables;
    public String instanceId;
    public int totalCases;
    public FailureResult failure;
    public List<SuiteModel> suites;
    public String runId;

    public ResultModel(final String runId, final String instanceId) {
        this.runId = runId;
        this.instanceId = instanceId;
    }
}
