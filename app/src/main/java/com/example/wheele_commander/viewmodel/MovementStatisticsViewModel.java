package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.backend.NetworkClient;

public class MovementStatisticsViewModel extends AbstractViewModel {
    private static final String TAG = "MovementStatisticsViewM";
    private final MutableLiveData<Integer> velocity;
    private final MutableLiveData<Integer> acceleration;
    private final MutableLiveData<Integer> distanceTravelled;
    private Long lastReadingMillis;

    public MovementStatisticsViewModel(@NonNull Application application) {
        super(application);
        velocity = new MutableLiveData<>(0);
        acceleration = new MutableLiveData<>(0);
        distanceTravelled = new MutableLiveData<>(0);
        lastReadingMillis = SystemClock.uptimeMillis();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
                networkClient = binder.getService();
                networkClient.setMovementStatisticsViewModel(MovementStatisticsViewModel.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: Service disconnected");
            }
        };
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MessageType.VELOCITY_UPDATE.ordinal()) {
            long currentReadingMillis = SystemClock.uptimeMillis();
            int newVelocity = msg.arg1; // may need to scale depending on units used by hardware
            int elapsedTime = (int) (currentReadingMillis - lastReadingMillis);

            distanceTravelled.postValue(
                    (newVelocity + velocity.getValue()) * elapsedTime / 2000
            ); // velocity is never null
            acceleration.postValue(
                    (newVelocity - velocity.getValue()) / elapsedTime / 1000
            );
            velocity.postValue(newVelocity);
            lastReadingMillis = currentReadingMillis;
        } else {
            try {
                String errorMessage = "Expected msg.what =" + MessageType.VELOCITY_UPDATE.name() +
                        "(" + MessageType.VELOCITY_UPDATE.ordinal() + "), got "
                        + MessageType.values()[msg.what] + "(" + msg.what + ")";
                throw new IllegalArgumentException(errorMessage);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Message does not exist");
            }
        }
    }

    public MutableLiveData<Integer> getVelocity() {
        return velocity;
    }

    public MutableLiveData<Integer> getDistanceTravelled() {
        return distanceTravelled;
    }
}
