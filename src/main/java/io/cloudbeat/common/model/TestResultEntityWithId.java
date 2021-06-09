package io.cloudbeat.common.model;

import io.cloudbeat.common.reporter.model.FailureResult;

public abstract class TestResultEntityWithId extends TestResultBase {
    public long id;
    public FailureResult failure;
}
