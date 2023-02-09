package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.viewmodel.MessageType.BATTERY_UPDATE;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.backend.INetworkClient;
import com.example.wheele_commander.backend.NetworkClient;
import com.example.wheele_commander.backend.IMessageSubscriber;

public class BatteryViewModel extends ViewModel implements IMessageSubscriber {
    public static final int MAXIMUM_MILEAGE = 12000; // represents 12km
    public final MutableLiveData<Integer> batteryCharge;
    public final MutableLiveData<Integer> estimatedMileage;
    private INetworkClient networkClient;

    public BatteryViewModel() {
        batteryCharge = new MutableLiveData<>(1000);
        estimatedMileage = new MutableLiveData<>(MAXIMUM_MILEAGE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
            networkClient = binder.getService();
            networkClient.subscribe(BatteryViewModel.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public MutableLiveData<Integer> getBatteryCharge() {
        return batteryCharge;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == BATTERY_UPDATE.ordinal()) {
            batteryCharge.setValue(msg.arg1);
            estimatedMileage.setValue(MAXIMUM_MILEAGE / batteryCharge.getValue() * 1000);
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
}
