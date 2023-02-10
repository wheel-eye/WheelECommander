package com.example.wheele_commander.viewmodel;

/**
 * Type of supported nominal for communication with ViewModels which implement {@link IMessageSubscriber}.
 *
 * Usage example:
 * <pre>{@code msg.what = MessageType.BATTERY_UPDATE.ordinal();}</pre>
 *
 * @author Konrad Pawlikowski
 * @version 1.0
 * @since 06/02/2023
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
     *
     * @since 1.0
     */
    JOYSTICK_MOVEMENT,
    /**
     * nominal to indicate a message contains data on the battery of the connected hardware.
     *
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = battery_charge;
     * }</pre>
     *
     * @since 1.0
     */
    BATTERY_UPDATE,
    /**
     * nominal to indicate a message contains data on the velocity of the connected hardware.
     *
     * the corresponding message should have
     * <pre>{@code
     * msg.arg1 = forward_velocity;
     * }</pre>
     *
     * @since 1.0
     */
    VELOCITY_UPDATE,
    // WARNING_MESSAGE should be replaced with a separate WarningType.
    WARNING_MESSAGE
}