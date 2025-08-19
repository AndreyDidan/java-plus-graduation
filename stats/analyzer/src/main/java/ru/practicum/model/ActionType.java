package ru.practicum.model;

import lombok.Getter;

@Getter
public enum ActionType {
    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    final double weight;

    ActionType(double weight) {
        this.weight = weight;
    }
}