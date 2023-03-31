package com.example.wheele_commander.deserializer;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonDeserializer {
    private static final String TAG = "JsonDeserializer";
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
            String json = new String(jsonBytes, StandardCharsets.UTF_8);
            Log.d(TAG, "Failed to deserialize " + json);
        }
        return null;
    }
}
