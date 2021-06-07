package io.cloudbeat.common.client;

import io.cloudbeat.common.model.RunStatus;
import io.cloudbeat.common.model.StatusModel;
import io.cloudbeat.common.model.TestStatus;

import java.util.Map;

public class CbApiClient {
    public void postInstanceStatus(String runId, String instanceId, RunStatus status) {

    }

    public void startRun(String runId, String projectId, RunStatus status) {
        //Calendar.getInstance().getTime()
    }

    public void startInstance(String id, String runId, RunStatus status, Map<String, String> attributes) {
    }

    public void endInstance(String id, String runId, TestStatus status) {

    }

    public void endRun(String runId) {
    }
}
