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
    ArrayList<String> args;
    @JsonIgnore
    StepResult parentStep = null;
    ArrayList<StepResult> steps = new ArrayList<>();
    Map<String, Number> stats = new HashMap<>();

    public StepResult(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public void end(TestStatus status) {
        this.endTime = Calendar.getInstance().getTimeInMillis();
        this.duration = endTime - startTime;
        this.status = status;
    }

    public StepResult addNewSubStep(final String name) {
        StepResult newSubStep = new StepResult(name);
        newSubStep.parentStep = this;
        steps.add(newSubStep);
        return newSubStep;
    }

    /* Setters */
    public void setFqn(String fqn) { this.fqn = fqn; }

    /* Getters */
    public String getId() { return id; }

    public String getName() { return name; }

    public String getFqn() { return fqn; }

    public long getStartTime() { return startTime; }

    public long getEndTime() { return endTime; }

    public List<StepResult> getSteps() { return steps; }

    public StepResult getParentStep() { return parentStep; }

    public Map<String, Number> getStats() { return stats; }
}
