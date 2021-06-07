package io.cloudbeat.common.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

public class StepResult {
    String id;
    String name;
    long startTime;
    long endTime;
    TestStatus status;
    Optional<String> fqn;
    ArrayList<String> args;
    StepResult parentStep = null;
    ArrayList<StepResult> substeps = new ArrayList<>();

    public StepResult(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public StepResult getParentStep() { return parentStep; }

    public void end(TestStatus status) {
        this.endTime = Calendar.getInstance().getTimeInMillis();
        this.status = status;
    }

    public StepResult addNewSubStep(final String name) {
        StepResult newSubStep = new StepResult(name);
        newSubStep.parentStep = this;
        substeps.add(newSubStep);
        return newSubStep;
    }
}
