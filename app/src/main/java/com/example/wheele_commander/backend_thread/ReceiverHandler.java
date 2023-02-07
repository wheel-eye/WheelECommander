package com.example.wheele_commander.backend_thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;

public class ReceiverHandler extends Handler {
    private final List<IMessageSubscriber> subscribedViewModels;

    public ReceiverHandler(Looper looper, List<IMessageSubscriber> subscribedViewModels){
        super(looper);
        this.subscribedViewModels = subscribedViewModels;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {

        switch (msg.what) {
            case 0:
                /*
                for (IMessageSubscriber subscriber : subscribedViewModels.get(EventType)) {
                        subscriber.handleMessage(msg);
                }
                */
                break;
            case 1:
                // Handle message 2
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
