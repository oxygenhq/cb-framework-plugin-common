package io.cloudbeat.common.model;

import java.util.UUID;

public class Run {
    String id;

    public Run() {
        this.id = UUID.randomUUID().toString();
    }

    public Run(final String id) {
        this.id = id;
    }
}
