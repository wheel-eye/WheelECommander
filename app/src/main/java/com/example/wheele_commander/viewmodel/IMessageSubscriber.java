package com.example.wheele_commander.viewmodel;

import android.os.Message;

/**
 * requires Implementer to be able to handle Message objects.
 *
 * @author Konrad Pawlikowski
 * @version 1.0
 * @since 06/02/2023
 */
public interface IMessageSubscriber {
    /**
     * Takes a Message object and performs a procedure related to the contents of the message.
     *
     * @since 1.0
     */
    void handleMessage(Message message);
}