package io.cloudbeat.common.reporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cloudbeat.common.reporter.serializer.EpochTimeSerializer;
import io.cloudbeat.common.reporter.serializer.TestStatusSerializer;

import java.util.*;

public class StepResult {
    String id;
    String name;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long startTime;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long endTime;
    long duration;
    @JsonSerialize(using = TestStatusSerializer.class)
    TestStatus status;
    String fqn;
    List<String> args;
    FailureResult failure;
    @JsonIgnore
    StepResult parentStep = null;
    ArrayList<StepResult> steps = new ArrayList<>();
    Map<String, Number> stats = new HashMap<>();
    ArrayList<LogMessage> logs = new ArrayList<>();

    public StepResult(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public void end() {
        end(null, null);
    }

    public void end(TestStatus status) {
        end(status, null);
    }

    public void end(Throwable throwable) {
        end(null, throwable);
    }

    public void end(TestStatus status, Throwable throwable) {
        this.endTime = Calendar.getInstance().getTimeInMillis();
        this.duration = endTime - startTime;
        if (throwable != null)
            this.failure = new FailureResult(throwable);
        // calculate status automatically or force the provided status
        this.status = status != null ? status : calculateStepStatus();
    }

    public StepResult addNewSubStep(final String name) {
        StepResult newSubStep = new StepResult(name);
        newSubStep.parentStep = this;
        steps.add(newSubStep);
        return newSubStep;
    }

    public void addLogMessage(final LogMessage logMessage) {
        this.logs.add(logMessage);
    }

    private TestStatus calculateStepStatus() {
        if (failure != null)
            return TestStatus.FAILED;
        boolean hasFailedSubStep = steps.stream().anyMatch(x -> x.status == TestStatus.FAILED);
        return hasFailedSubStep ? TestStatus.FAILED : TestStatus.PASSED;
    }

    /* Setters */
    public void setFqn(String fqn) { this.fqn = fqn; }
    public void setArgs(List<String > args) { this.args = args; }
    public void setStats(Map<String, Number> stats) { this.stats = stats; }

    /* Getters */
    public String getId() { return id; }

    public String getName() { return name; }

    public List<String> getArgs() { return args; }

    public String getFqn() { return fqn; }

    public long getStartTime() { return startTime; }

    public long getEndTime() { return endTime; }

    public long getDuration() { return duration; }

    public List<StepResult> getSteps() { return steps; }

    public StepResult getParentStep() { return parentStep; }

    public Map<String, Number> getStats() { return stats; }

    public FailureResult getFailure() { return failure; }

    public List<LogMessage> getLogs() { return logs; }
}
