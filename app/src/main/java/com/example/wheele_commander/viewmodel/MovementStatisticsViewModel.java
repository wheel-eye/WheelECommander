package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wheele_commander.backend.NetworkClient;
import com.example.wheele_commander.model.MessageType;
import com.example.wheele_commander.model.Wheelchair;

/**
 * stores velocity related information of the connected hardware and handles {@code VELOCITY_UPDATE} messages.
 * <p>
 * Velocity related information refers to (total) distance, velocity, and acceleration.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see MessageType#VELOCITY_UPDATE
 */
public class MovementStatisticsViewModel extends AbstractViewModel {
    private static final String TAG = "MovementStatisticsViewModel";
    private long lastReadingMillis;

    public MovementStatisticsViewModel(@NonNull Application application, @NonNull Wheelchair wheelchair) {
        super(application, wheelchair);
        lastReadingMillis = SystemClock.uptimeMillis();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
                networkClient = binder.getService();

                // TODO: evil, but better than passing reference to NetworkClient via setter
                networkClient.getMovementMessage().observeForever(messageObserver);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: Service disconnected");
            }
        };
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        networkClient.getMovementMessage().removeObserver(messageObserver);
    }

    /**
     * Takes a {@code VELOCITY_UPDATE} message and updates the current {@code distanceTravelled},
     * {@code velocity} and {@code acceleration}.
     *
     * @param msg refer to {@link MessageType#VELOCITY_UPDATE} for clear specifications
     *            on the message structure.
     * @throws IllegalArgumentException  Thrown when given a message that isn't a
     *                                   {@code VELOCITY_UPDATE}.
     * @throws IndexOutOfBoundsException Thrown when given an unknown message nominal.
     */
    @Override
    public void handleMessage(Message msg) {
        if (msg.what != MessageType.VELOCITY_UPDATE.nominal()) {
            throw new IllegalArgumentException(String.format("Expected msg.what = %s (%d), got %s (%d)",
                    MessageType.VELOCITY_UPDATE.name(), MessageType.VELOCITY_UPDATE.ordinal(),
                    MessageType.values()[msg.what], msg.what));
        }

        float newVelocity = msg.arg1 / 10f;
        int elapsedTime = (int) (SystemClock.uptimeMillis() - lastReadingMillis);

        // elapsedTime is converted from milliseconds to hours
        wheelchair.setDistanceTravelled(wheelchair.getDistanceTravelled().getValue() +
                (elapsedTime * (wheelchair.getVelocity().getValue() + newVelocity)) / 7200000f);
        wheelchair.setVelocity(newVelocity);
        lastReadingMillis = SystemClock.uptimeMillis();
    }
}
