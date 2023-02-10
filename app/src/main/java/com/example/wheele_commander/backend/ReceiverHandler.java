package com.example.wheele_commander.backend;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.wheele_commander.deserializer.SensorData;
import com.example.wheele_commander.deserializer.SensorWarning;
import com.example.wheele_commander.viewmodel.IMessageSubscriber;
import com.example.wheele_commander.viewmodel.MessageType;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ReceiverHandler extends Handler {
    private final HashMap<MessageType, List<IMessageSubscriber>> subscribedViewModels;

    public ReceiverHandler(Looper looper,
                           HashMap<MessageType, List<IMessageSubscriber>> subscribedViewModels) {
        super(looper);
        this.subscribedViewModels = subscribedViewModels;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            // WARNING
            case 0:
                Message msgWarning = new Message();
                msgWarning.what = MessageType.WARNING_MESSAGE.ordinal();
                msgWarning.arg1 = ((SensorWarning) msg.obj).getWarning().code;
                for (IMessageSubscriber subscriber :
                        Objects.requireNonNull(
                                subscribedViewModels.get(MessageType.WARNING_MESSAGE))) {
                    subscriber.handleMessage(msgWarning);
                }
                break;
            // DATA
            case 1:
                Message msgData = new Message();
//                for (MessageType type : )

                msgData.what = MessageType.VELOCITY_UPDATE.ordinal();
                msgData.arg1 = ((SensorData) msg.obj).getData().getSpeed();
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
