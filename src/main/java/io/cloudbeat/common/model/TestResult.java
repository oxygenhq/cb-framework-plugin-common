package io.cloudbeat.common.model;

import java.util.*;

public class TestResult {
    String instanceId;
    String runId;
    long startTime;
    long endTime;
    Map<String, String> options;
    Map<String, String> capabilities;
    Map<String, String> metadata;
    Map<String, String> environmentVariables;

    FailureResult failure;
    ArrayList<SuiteResult> suites = new ArrayList<>();
    TestStatus status;

    public TestResult(final String runId, final String instanceId) {
        this.runId = runId;
        this.instanceId = instanceId;
        startTime = Calendar.getInstance().getTime().getTime();
    }

    public void end(TestStatus status) {
        endTime = Calendar.getInstance().getTime().getTime();
        this.status = status;
    }

    public SuiteResult addNewSuite(final String name) {
        SuiteResult newSuite = new SuiteResult(name);
        suites.add(newSuite);
        return newSuite;
    }

    public Optional<SuiteResult> getLastSuite() {
        if (suites.size() > 0)
            return Optional.of(suites.get(suites.size() - 1));
        return Optional.empty();
    }

    public Optional<SuiteResult> getLastSuite(String fqn) {
        for (int i = suites.size(); i-- > 0; ) {
            final SuiteResult suite = suites.get(i);
            final Optional<String> suiteFqn = suite.getFqn();
            if (suiteFqn.isPresent() && suiteFqn.get().equals(fqn))
                return Optional.of(suite);
        }
        return Optional.empty();
    }
}
