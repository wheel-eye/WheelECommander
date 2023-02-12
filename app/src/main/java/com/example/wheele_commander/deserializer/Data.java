package com.example.wheele_commander.deserializer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {
    private Integer battery;
    private Integer speed;

    @JsonProperty("battery")
    public Integer getBattery() {
        return battery;
    }

    @JsonProperty("speed")
    public Integer getSpeed() {
        return speed;
    }
}
