package com.example.wheele_commander.deserializer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SensorData {
    private Data data;

    @JsonProperty("data")
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
