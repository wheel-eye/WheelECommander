package com.example.wheele_commander.model;

import static com.example.wheele_commander.model.MessageType.BATTERY_UPDATE;

import android.os.Message;

import androidx.lifecycle.MutableLiveData;

import backend_thread.NetworkClient;

public final class BatteryViewModel implements IMessageSubscriber{
    public static final int MAXIMUM_MILEAGE = 12000; // represents 12km
    public final MutableLiveData<Integer> batteryCharge = new MutableLiveData<>(1000);
    public final MutableLiveData<Integer> estimatedMileage = new MutableLiveData<>(MAXIMUM_MILEAGE);

    BatteryViewModel(){
        NetworkClient.subscribe(this); // how do I know the Network client?
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == BATTERY_UPDATE.ordinal()){
            batteryCharge.setValue(msg.arg1);
            estimatedMileage.setValue(MAXIMUM_MILEAGE/batteryCharge.getValue()*1000);
        } else {
            try {
                String errorMessage = "Expected msg.what =" + BATTERY_UPDATE.name() +
                        "(" + BATTERY_UPDATE.ordinal() + "), got "
                        + MessageType.values()[msg.what] + "(" + msg.what + ")";
                throw new IllegalArgumentException(errorMessage);
            } catch (IndexOutOfBoundsException e){
                throw new IndexOutOfBoundsException("Message does not exist");
            }
        }
    }
}
