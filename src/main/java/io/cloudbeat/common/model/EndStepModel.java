package io.cloudbeat.common.model;

import io.cloudbeat.common.reporter.model.FailureResult;

public class EndStepModel {
    public EndStepModel(String stepName, String testName, boolean isSuccess) {
        this.testName = testName;
        this.stepName = stepName;
        this.isSuccess = isSuccess;
    }

    public EndStepModel(String stepName, String testName, boolean isSuccess, FailureResult failureModel) {
        this.testName = testName;
        this.stepName = stepName;
        this.isSuccess = isSuccess;
        this.failureModel = failureModel;
    }

    public String testName;
    public String stepName;
    public long loadEvent;
    public long domContentLoadedEvent;
    public boolean isSuccess;
    public String pageRef;
    public FailureResult failureModel;
}
