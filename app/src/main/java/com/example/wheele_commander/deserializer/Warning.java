package com.example.wheele_commander.deserializer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Warning {
    private int code;

    @JsonProperty("code")
    public int getCode() {
        return code;
    }
}
