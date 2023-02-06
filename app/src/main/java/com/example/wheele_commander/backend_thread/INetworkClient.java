package com.example.wheele_commander.backend_thread;

import android.os.Message;

import androidx.lifecycle.ViewModel;

/*
Y.S. - This interface needs further revision... Certain methods maybe redundant
 */
public interface INetworkClient {

    void sendMessage(Message msg);

    void subscribe(ViewModel viewModel);
}
