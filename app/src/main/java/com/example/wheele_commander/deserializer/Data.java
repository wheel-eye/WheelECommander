package com.example.wheele_commander.deserializer;

public class Data {
    private int battery;
    private int speed;

    @JsonProperty("battery")
    public int getBattery() {
        return battery;
    }

    @JsonProperty("speed")
    public int getSpeed() {
        return speed;
    }
}
