package com.example.wheele_commander.model;

import java.util.Locale;

public enum WarningType {
    EMERGENCY_STOP,
    DISABLE_COMPONENT,
    COMPONENT_CONNECTION_LOST,
    COLLISION_WARNING,
    BATTERY_LOW,
    BATTERY_CRITICAL,
    SPEED_WARNING;

    /**
     * returns the nominal value of the caller.
     *
     * @return nominal representing the caller.
     */
    public int nominal(){
        return this.ordinal();
    }

    /**
     * returns the WarningType corresponding to a nominal value.
     *
     * @param nominal the representing value of a WarningType.
     * @return the WarningType corresponding to the given nominal.
     *
     * @throws IndexOutOfBoundsException Thrown when given an unknown message nominal.
     */
    public static WarningType getByNominal(int nominal){
        try{
            return WarningType.values()[nominal];
        } catch(IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException(String.format(
                    Locale.UK,
                    "No WarningType corresponds with this nominal (%d), maybe you meant to get a MessageType instead?",
                    nominal));
        }
    }
}
