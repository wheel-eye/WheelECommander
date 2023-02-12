package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.viewmodel.MessageType.BATTERY_UPDATE;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.backend.NetworkClient;

public class BatteryViewModel extends AbstractViewModel {
    private static final String TAG = "BatteryViewModel";
    private static final int MAXIMUM_MILEAGE = 12000; // represents 12km
    private final MutableLiveData<Integer> batteryCharge;
    private final MutableLiveData<Integer> estimatedMileage;

    public BatteryViewModel(@NonNull Application application) {
        super(application);
        batteryCharge = new MutableLiveData<>(1000);
        estimatedMileage = new MutableLiveData<>(MAXIMUM_MILEAGE);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
                networkClient = binder.getService();
                networkClient.setBatteryViewModel(BatteryViewModel.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: Service disconnected");
            }
        };
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == BATTERY_UPDATE.ordinal()) {
            batteryCharge.postValue(msg.arg1);
            estimatedMileage.postValue(MAXIMUM_MILEAGE / batteryCharge.getValue() * 1000);
        } else {
            try {
                String errorMessage = "Expected msg.what =" + BATTERY_UPDATE.name() +
                        "(" + BATTERY_UPDATE.ordinal() + "), got "
                        + MessageType.values()[msg.what] + "(" + msg.what + ")";
                throw new IllegalArgumentException(errorMessage);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Message does not exist");
            }
        }
    }

    public MutableLiveData<Integer> getBatteryCharge() {
        return batteryCharge;
    }

    public MutableLiveData<Integer> getEstimatedMileage() {
        return estimatedMileage;
    }
}
