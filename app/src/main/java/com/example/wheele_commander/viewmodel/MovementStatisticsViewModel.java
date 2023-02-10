package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.viewmodel.MessageType.VELOCITY_UPDATE;

import android.content.ComponentName;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.backend.INetworkClient;
import com.example.wheele_commander.backend.NetworkClient;

public class MovementStatisticsViewModel extends ViewModel implements IMessageSubscriber {
    private INetworkClient networkClient;
    public final MutableLiveData<Integer> velocity;
    public final MutableLiveData<Integer> acceleration;
    public final MutableLiveData<Integer> distanceTravelled;
    private Long lastReadingMillis;

    public MovementStatisticsViewModel() {
        velocity = new MutableLiveData<>(0);
        acceleration = new MutableLiveData<>(0);
        distanceTravelled = new MutableLiveData<>(0);
        lastReadingMillis = SystemClock.uptimeMillis();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
            networkClient = binder.getService();
//            networkClient.subscribe(MovementStatisticsViewModel.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCleared() {
        super.onCleared();
//        unbindService(serviceConnection);
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public MutableLiveData<Integer> getVelocity() {
        return velocity;
    }

    public MutableLiveData<Integer> getDistanceTravelled() {
        return distanceTravelled;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == VELOCITY_UPDATE.ordinal()) {
            long currentReadingMillis = SystemClock.uptimeMillis();
            int newVelocity = msg.arg1; // may need to scale depending on units used by hardware
            int elapsedTime = (int) (currentReadingMillis - lastReadingMillis);

            distanceTravelled.setValue(distanceTravelled.getValue()+
                    (newVelocity + velocity.getValue()) * elapsedTime / 2 / 1000
            ); // distanceTravelled and velocity are never null
            acceleration.setValue(
                    (newVelocity - velocity.getValue()) / elapsedTime / 1000
            ); // 'can' result in division by 0 ArithmeticError
            velocity.setValue(newVelocity);
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
}
