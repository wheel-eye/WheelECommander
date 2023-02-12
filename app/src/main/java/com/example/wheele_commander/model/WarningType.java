package com.example.wheele_commander.model;

public enum WarningType {
    EMERGENCY_STOP(1),
    DISABLE_COMPONENT(2),
    COMPONENT_CONNECTION_LOST(3),
    COLLISION_WARNING(4),
    BATTERY_LOW(5),
    BATTERY_CRITICAL(6),
    SPEED_WARNING(7);

    private final int code;

    WarningType(int value) {
        this.code = value;
    }

    public int getCode() {
        return code;
    }

    public static WarningType getWarningTypeFromCode(int code) {
        for (WarningType warningType : WarningType.values()) {
            if (warningType.getCode() == code)
                return warningType;
        }
        return null;
    }
}
