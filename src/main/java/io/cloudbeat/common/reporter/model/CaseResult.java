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

    public void end(TestStatus status) {
        this.endTime = Calendar.getInstance().getTimeInMillis();
        this.duration = endTime - startTime;
        this.status = status;
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
