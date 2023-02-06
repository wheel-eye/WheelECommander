package com.example.wheele_commander.deserializer;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Deserializer {
    Deserializer instance;

    private Deserializer(){
    }

    public Deserializer getInstance(){
        if (instance == null) {
            instance = new Deserializer();
        }
        return instance;
    }

    public static Data getData(byte[] stream){
        try {
            return new ObjectMapper().readValue(stream, Data.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static warning getWarning(byte[] stream){
        try {
            return new ObjectMapper().readValue(stream, warning.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
