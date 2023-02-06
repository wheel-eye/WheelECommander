package com.example.wheele_commander.deserializer;


public class data {
    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getTimeSinceStart() {
        return timeSinceStart;
    }

    public void setTimeSinceStart(int timeSinceStart) {
        this.timeSinceStart = timeSinceStart;
    }

    private int battery;
    private int speed;
    private int timeSinceStart;
}
