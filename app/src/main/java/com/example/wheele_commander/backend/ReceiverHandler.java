package com.example.wheele_commander.backend;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.wheele_commander.deserializer.Data;
import com.example.wheele_commander.deserializer.Warning;
import com.example.wheele_commander.viewmodel.AbstractViewModel;
import com.example.wheele_commander.viewmodel.MessageType;

public class ReceiverHandler extends Handler {
    private final AbstractViewModel movementStatisticsViewModel;
    private final AbstractViewModel batteryViewModel;
    private final AbstractViewModel warningViewModel;

    public ReceiverHandler(
            Looper looper,
            AbstractViewModel movementStatisticsViewModel,
            AbstractViewModel batteryViewModel,
            AbstractViewModel warningViewModel) {
        super(looper);
        this.movementStatisticsViewModel = movementStatisticsViewModel;
        this.batteryViewModel = batteryViewModel;
        this.warningViewModel = warningViewModel;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == 0) { // DATA
            Data data = (Data) msg.obj;

            if (data.getSpeed() != null) {
                Message velocityMessage = new Message();
                velocityMessage.what = MessageType.VELOCITY_UPDATE.ordinal();
                velocityMessage.arg1 = data.getSpeed();
                movementStatisticsViewModel.handleMessage(velocityMessage);
            }
            if (data.getBattery() != null) {
                Message batteryMessage = new Message();
                batteryMessage.what = MessageType.BATTERY_UPDATE.ordinal();
                batteryMessage.arg1 = data.getBattery();
                batteryViewModel.handleMessage(batteryMessage);
            }
        } else if (msg.what == 1) { // WARNING
            Message warningMessage = new Message();
            warningMessage.what = MessageType.WARNING_MESSAGE.ordinal();
            warningMessage.arg1 = ((Warning) msg.obj).getCode();
            warningViewModel.handleMessage(warningMessage);
        }
    }
}
