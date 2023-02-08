package com.example.wheele_commander.deserializer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SensorWarning {
    private Warning warning;

    @JsonProperty("warning")
    public Warning getWarning() {
        return warning;
    }

    public void setData(Warning warning) {
        this.warning = warning;
    }
}
