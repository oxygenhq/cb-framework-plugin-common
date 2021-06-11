package io.cloudbeat.common.reporter.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cloudbeat.common.reporter.serializer.EpochTimeSerializer;
import io.cloudbeat.common.reporter.serializer.TestStatusSerializer;

import java.util.*;

public class CaseResult {
    String id;
    String name;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long startTime;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long endTime;
    long duration;
    String fqn;
    ArrayList<String> args;
    @JsonSerialize(using = TestStatusSerializer.class)
    TestStatus status;
    FailureResult failure;
    ArrayList<StepResult> steps = new ArrayList<>();
    ArrayList<StepResult> logs = new ArrayList<>();

    public CaseResult(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public void end() {
        end(null, null);
    }

    public void end(Throwable throwable) {
        end(null, throwable);
    }

    public void end(TestStatus status) {
        end(status, null);
    }

    public void end(TestStatus status, Throwable throwable) {
        this.endTime = Calendar.getInstance().getTimeInMillis();
        this.duration = endTime - startTime;
        this.failure = new FailureResult(throwable);
        this.status = status != null ? status : calculateCaseStatus();
    }

    public StepResult addNewStep(String name) {
        StepResult newStep;
        steps.add(newStep = new StepResult(name));
        return newStep;
    }

    private TestStatus calculateCaseStatus() {
        // if there is a direct failure attached to the case,
        // mark it as failed, regardless its children status
        if (failure != null)
            return TestStatus.FAILED;
        // determine case status by its children's status
        boolean hasFailedStep = steps.stream().anyMatch(x -> x.status == TestStatus.FAILED);
        return hasFailedStep ? TestStatus.FAILED : TestStatus.PASSED;
    }

    /* Setters */
    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public void setFailure(Throwable exception) {
        failure = new FailureResult(exception, fqn);
    }

    /* Getters */
    public String getId() { return id; }

    public String getName() { return name; }

    public String getFqn() {
        return fqn;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public List<String> getArgs() {
        return args;
    }

    public TestStatus getStatus() {
        return status;
    }

    public FailureResult getFailure() {
        return failure;
    }

    public List<StepResult> getSteps() {
        return steps;
    }
}
