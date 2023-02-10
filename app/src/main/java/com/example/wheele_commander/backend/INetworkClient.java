package com.example.wheele_commander.backend;

import android.os.Message;

import com.example.wheele_commander.viewmodel.IMessageSubscriber;
import com.example.wheele_commander.viewmodel.MessageType;

import java.util.List;

public interface INetworkClient {
    void sendMessage(Message msg);

    void subscribe(IMessageSubscriber viewModel,
                   List<MessageType> subscribedTypes);
}
