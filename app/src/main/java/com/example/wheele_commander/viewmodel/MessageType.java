package com.example.wheele_commander.viewmodel;

/**
 * Type of supported nominal for communication with ViewModels which implement {@link IMessageSubscriber}.
 * <p>
 * Usage example:
 * <pre>{@code msg.what = MessageType.BATTERY_UPDATE.ordinal();}</pre>
 *
 * @author Konrad Pawlikowski
 */
public class MessageType {
    /**
     * nominal to indicate a message contains data on joystick inputs.
     * <p>
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = joystick_angle;
     * msg.arg2 = joystick_power;
     * }</pre>
     */
    public static final int JOYSTICK_MOVEMENT = 0;

    /**
     * nominal to indicate a message contains data on the battery of the connected hardware.
     * <p>
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = battery_charge;
     * }</pre>
     */
    public static final int BATTERY_UPDATE = 1;

    /**
     * nominal to indicate a message contains data on the velocity of the connected hardware.
     * <p>
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = forward_velocity;
     * }</pre>
     */
    public static final int VELOCITY_UPDATE = 2;

    public static final int WARNING_MESSAGE = 3;
}