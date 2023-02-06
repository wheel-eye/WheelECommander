package com.example.wheele_commander.model;

public enum MessageType{
    /*
    JOYSTICK_MOVEMENT:
    arg1 = angle
    arg2 = power
     */
    JOYSTICK_MOVEMENT,
    /*
    BATTERY_UPDATE:
    arg1 = charge
     */
    BATTERY_UPDATE,
    /*
    VELOCITY_UPDATE:
    arg1 = velocity
     */
    VELOCITY_UPDATE
}