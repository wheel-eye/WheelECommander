package com.example.wheele_commander.backend_thread;

import android.os.Message;

public interface INetworkClient {
    void sendMessage(Message msg);

    void subscribe(IMessageSubscriber viewModel);
}
