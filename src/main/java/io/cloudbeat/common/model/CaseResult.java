package io.cloudbeat.common.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

public class CaseResult {
    String id;
    String name;
    long startTime;
    long endTime;
    Optional<String> fqn;
    ArrayList<String> args;
    TestStatus status;
    FailureResult failure;
    ArrayList<StepResult> steps = new ArrayList<>();

    public CaseResult(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public void setFqn(String fqn) {
        this.fqn = Optional.of(fqn);
    }

    public Optional<String> getFqn() {
        return fqn;
    }

    public void end(TestStatus status) {
        this.endTime = Calendar.getInstance().getTimeInMillis();
        this.status = status;
    }

    public void setFailure(Throwable exception) {
        failure = new FailureResult(exception, fqn.orElse(null));
    }
}
