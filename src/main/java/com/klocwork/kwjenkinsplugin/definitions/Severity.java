package com.klocwork.kwjenkinsplugin.definitions;

public enum Severity {
    CRITICAL(1, "critical"),
    ERROR(2, "error"),
    WARNING(3, "warning"),
    REVIEW(4, "review");

    private final int id;
    private final String name;

    Severity(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
