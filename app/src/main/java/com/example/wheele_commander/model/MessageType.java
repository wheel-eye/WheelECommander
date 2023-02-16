package com.example.wheele_commander.model;

import android.os.Build;

import com.example.wheele_commander.viewmodel.IMessageSubscriber;

import java.util.Locale;

/**
 * Type of supported nominal for communication with ViewModels which implement {@link IMessageSubscriber}.
 *
 * Usage example:
 * <pre>{@code msg.what = MessageType.BATTERY_UPDATE.ordinal();}</pre>
 *
 * @author Konrad Pawlikowski
 */
public enum MessageType {
    /**
     * nominal to indicate a message contains data on joystick inputs.
     *
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = joystick_angle;
     * msg.arg2 = joystick_power;
     * }</pre>
     */
    JOYSTICK_MOVEMENT,
    /**
     * nominal to indicate a message contains data on the battery of the connected hardware.
     *
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = battery_charge;
     * }</pre>
     */
    BATTERY_UPDATE,
    /**
     * nominal to indicate a message contains data on the velocity of the connected hardware.
     *
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = forward_velocity;
     * }</pre>
     */
    VELOCITY_UPDATE;

    /**
     * returns the nominal value of the caller.
     *
     * @return nominal representing the caller.
     */
    public int nominal(){
        return this.ordinal();
    }

    /**
     * returns the MessageType corresponding to a nominal value.
     *
     * @param nominal the representing value of a MessageType.
     * @return the MessageType corresponding to the given nominal.
     *
     * @throws IndexOutOfBoundsException Thrown when given an unknown message nominal.
     */
    public static MessageType getByNominal(int nominal){
        try{
            return MessageType.values()[nominal];
        } catch(IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException(String.format(
                    Locale.UK,
                    "No MessageType corresponds with this nominal (%d), maybe you meant to get a WarningMessage instead?",
                    nominal));
        }
    }
}