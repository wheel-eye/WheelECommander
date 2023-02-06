package com.example.wheele_commander.backend_thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class ReceiverHandler extends Handler {
    public ReceiverHandler(Looper looper){
        super(looper);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case 1:
                // Handle message 1
                break;
            case 2:
                // Handle message 2
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
