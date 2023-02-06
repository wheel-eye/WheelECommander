package com.example.wheele_commander.model;

import static com.example.wheele_commander.model.MessageType.VELOCITY_UPDATE;

import android.os.Message;
import android.os.SystemClock;

import androidx.lifecycle.MutableLiveData;

import backend_thread.NetworkClient;

public final class MovementStatisticsViewModel implements IMessageSubscriber{
    public final MutableLiveData<Integer> velocity = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> acceleration = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> distanceTravelled = new MutableLiveData<>(0);
    private Long lastReadingMillis = SystemClock.uptimeMillis();

    MovementStatisticsViewModel(){
        NetworkClient.subscribe(this); // how do I know the Network client?
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == VELOCITY_UPDATE.ordinal()){
            long currentReadingMillis = SystemClock.uptimeMillis();
            int newVelocity = msg.arg1; // may need to scale depending on units used by hardware
            int elapsedTime = (int) (currentReadingMillis-this.lastReadingMillis);

            distanceTravelled.setValue(
                    (newVelocity+this.velocity.getValue())*elapsedTime/2000
            ); // velocity is never null
            acceleration.setValue(
                    (newVelocity-this.velocity.getValue())/elapsedTime/1000
            );
            this.velocity.setValue(newVelocity);
            this.lastReadingMillis = currentReadingMillis;
        } else {
            try {
                String errorMessage = "Expected msg.what =" + VELOCITY_UPDATE.name() +
                        "(" + VELOCITY_UPDATE.ordinal() + "), got "
                        + MessageType.values()[msg.what] + "(" + msg.what + ")";
                throw new IllegalArgumentException(errorMessage);
            } catch (IndexOutOfBoundsException e){
                throw new IndexOutOfBoundsException("Message does not exist");
            }
        }
    }
}
