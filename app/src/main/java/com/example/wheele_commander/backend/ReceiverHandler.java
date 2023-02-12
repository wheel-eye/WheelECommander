package com.example.wheele_commander.backend;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.deserializer.Data;
import com.example.wheele_commander.deserializer.Warning;
import com.example.wheele_commander.viewmodel.MessageType;

public class ReceiverHandler extends Handler {
    private final MutableLiveData<Message> movementMessageData;
    private final MutableLiveData<Message> batteryMessageData;
    private final MutableLiveData<Message> warningMessageData;

    public ReceiverHandler(
            Looper looper,
            MutableLiveData<Message> movementMessageData,
            MutableLiveData<Message> batteryMessageData,
            MutableLiveData<Message> warningMessageData) {
        super(looper);
        this.movementMessageData = movementMessageData;
        this.batteryMessageData = batteryMessageData;
        this.warningMessageData = warningMessageData;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == 0) { // DATA
            Data data = (Data) msg.obj;

            if (data.getSpeed() != null) {
                Message velocityMessage = new Message();
                velocityMessage.what = MessageType.VELOCITY_UPDATE.ordinal();
                velocityMessage.arg1 = data.getSpeed();
                movementMessageData.postValue(velocityMessage);
            }
            if (data.getBattery() != null) {
                Message batteryMessage = new Message();
                batteryMessage.what = MessageType.BATTERY_UPDATE.ordinal();
                batteryMessage.arg1 = data.getBattery();
                batteryMessageData.postValue(batteryMessage);
            }
        } else if (msg.what == 1) { // WARNING
            Message warningMessage = new Message();
            warningMessage.what = MessageType.WARNING_MESSAGE.ordinal();
            warningMessage.arg1 = ((Warning) msg.obj).getCode();
            warningMessageData.postValue(warningMessage);
        }
    }
}
