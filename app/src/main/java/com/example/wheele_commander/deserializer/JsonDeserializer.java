package com.example.wheele_commander.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonDeserializer {
    private static JsonDeserializer instance;
    private static ObjectMapper objectMapper;

    private JsonDeserializer() {
        objectMapper = new ObjectMapper();
    }

    public static JsonDeserializer getInstance() {
        if (instance == null)
            instance = new JsonDeserializer();

        return instance;
    }

    public <T> T deserialize(byte[] jsonBytes, Class<T> type) {
        try {
            return objectMapper.readValue(jsonBytes, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
