package com.example.wheele_commander.viewmodel;

import android.os.Message;

/**
 * requires Implementer to be able to handle Message objects.
 *
 * @author Konrad Pawlikowski
 */
public interface IMessageSubscriber {
    /**
     * Takes a Message object and performs a procedure related to the contents of the message.
     */
    void handleMessage(Message message);
}