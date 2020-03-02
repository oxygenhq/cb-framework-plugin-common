package io.cloudbeat.common.model;

public abstract class TestResultEntityWithId extends TestResultBase {
    public long id;
    public FailureModel failure;
}
