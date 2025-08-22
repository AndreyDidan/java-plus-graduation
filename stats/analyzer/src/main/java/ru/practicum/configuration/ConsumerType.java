package ru.practicum.configuration;

public enum ConsumerType {
    USER_ACTION("userAction"),
    EVENT_SIMILARITY("eventSimilarity");

    private final String key;

    ConsumerType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
