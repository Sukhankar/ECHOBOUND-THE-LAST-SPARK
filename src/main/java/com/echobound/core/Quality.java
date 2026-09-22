package com.echobound.core;

public enum Quality {
    LOW("LOW"),
    BALANCED("BALANCED"),
    HIGH("HIGH");

    private final String label;

    Quality(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Quality next() {
        Quality[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
