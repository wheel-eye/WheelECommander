package com.example.wheele_commander.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Deserializer {
    private static Deserializer instance;
    private static ObjectMapper objectMapper;

    private Deserializer() {
        objectMapper = new ObjectMapper();
    }

    public Deserializer getInstance() {
        if (instance == null)
            instance = new Deserializer();

        return instance;
    }

    public static SensorData getData(byte[] stream) {
        try {
            return objectMapper.readValue(stream, SensorData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SensorWarning getWarning(byte[] stream) {
        try {
            return objectMapper.readValue(stream, SensorWarning.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
