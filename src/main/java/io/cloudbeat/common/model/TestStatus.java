package io.cloudbeat.common.model;

public enum TestStatus {
    PASSED("passed"),
    FAILED("failed"),
    IGNORED("ignored"),
    BROKEN("broken");

    private final String value;

    TestStatus(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value();
    }
}
