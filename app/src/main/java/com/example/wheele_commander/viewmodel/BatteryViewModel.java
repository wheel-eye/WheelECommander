package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.model.MessageType.BATTERY_UPDATE;

import android.app.Application;

import android.content.ComponentName;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.os.Message;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wheele_commander.backend.NetworkClient;

import com.example.wheele_commander.model.MessageType;
import com.example.wheele_commander.model.Wheelchair;

import java.util.Locale;

/**
 * stores information on the battery of the connected hardware and handles {@code BATTERY_UPDATE} messages.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see MessageType#BATTERY_UPDATE
 */
public final class BatteryViewModel extends AbstractViewModel {
    private static final String TAG = "BatteryViewModel";
    private final Wheelchair wheelchair;
    private int initialBattery;

    public BatteryViewModel(@NonNull Application application, @NonNull Wheelchair wheelchair) {
        super(application, wheelchair);
        this.wheelchair = wheelchair;
        this.initialBattery = -1;
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
                networkClient = binder.getService();

                // TODO: evil, but better than passing reference to NetworkClient via setter
                networkClient.getBatteryMessage().observeForever(messageObserver);
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
        networkClient.getBatteryMessage().removeObserver(messageObserver);
    }

    /**
     * Takes a {@code BATTERY_UPDATE} message and updates the current {@code batteryCharge} and
     * {@code estimatedMileage}.
     *
     * @param msg refer to {@link MessageType#BATTERY_UPDATE} for clear specifications
     *            on the message structure.
     * @throws IllegalArgumentException  Thrown when given a message that isn't a
     *                                   {@code BATTERY_UPDATE}.
     * @throws IndexOutOfBoundsException Thrown when given an unknown message nominal.
     */
    @Override
    public void handleMessage(Message msg) {
        if (msg.what == BATTERY_UPDATE.nominal()) {
            initialBattery = Math.max(msg.arg1, initialBattery);
            int newBatteryCharge = msg.arg1;
            wheelchair.setBatteryCharge(newBatteryCharge);

            wheelchair.setEstimatedMileage(
                    newBatteryCharge == 0 ?
                            0 :
                            (wheelchair.getDistanceTravelled().getValue() < 0.1f ?
                                    Wheelchair.MAXIMUM_MILEAGE :
                                    wheelchair.getDistanceTravelled().getValue() *
                                            ((float) newBatteryCharge) /
                                            ((float) (initialBattery-newBatteryCharge) )
                            )
            );
        } else {
            String errorMessage = String.format(
                    Locale.UK,
                    "Expected msg.what = %s (%d), got %s (%d)",
                    BATTERY_UPDATE.name(),
                    BATTERY_UPDATE.nominal(),
                    MessageType.getByNominal(msg.what).name(),
                    msg.what);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
