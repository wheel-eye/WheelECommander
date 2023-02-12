package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.viewmodel.MessageType.BATTERY_UPDATE;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.backend.NetworkClient;

import java.util.Locale;

/**
 * stores information on the battery of the connected hardware and handles {@code BATTERY_UPDATE} messages.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see com.example.wheele_commander.viewmodel.MessageType
 */
public class BatteryViewModel extends AbstractViewModel {
    private static final String TAG = "BatteryViewModel";
    /**
     * recalls the mean maximum mileage of the device on smooth terrain with no incline, as measured during testing.
     * <p>
     * each unit represents a
     * <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>
     * so that a view may display results with 1 decimal point of precision.
     */
    private static final int MAXIMUM_MILEAGE = 12000; // represents 12km
    private final MutableLiveData<Integer> batteryCharge;
    private final MutableLiveData<Integer> estimatedMileage;

    public BatteryViewModel(@NonNull Application application) {
        super(application);
        batteryCharge = new MutableLiveData<>(100);
        estimatedMileage = new MutableLiveData<>(MAXIMUM_MILEAGE);
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
     * returns the current charge of the connected hardware.
     *
     * @return Returns the charge of the hardware battery where each unit represents 0.1%
     * of battery charge.
     */
    public LiveData<Integer> getBatteryCharge() {
        return batteryCharge;
    }

    /**
     * returns the estimated mileage of the connected hardware.
     *
     * @return Returns the estimated mileage of the hardware where each unit represents
     * a <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>.
     */
    public LiveData<Integer> getEstimatedMileage() {
        return estimatedMileage;
    }

    /**
     * Takes a {@code BATTERY_UPDATE} message and updates the current {@code batteryCharge} and
     * {@code estimatedMileage}.
     *
     * @param msg refer to {@link MessageType}.{@code BATTERY_UPDATE} for clear specifications
     *            on the message structure.
     * @throws IllegalArgumentException  Thrown when given a message that isn't a
     *                                   {@code BATTERY_UPDATE}.
     * @throws IndexOutOfBoundsException Thrown when given an unknown message nominal.
     */
    @Override
    public void handleMessage(Message msg) {
        if (msg.what == BATTERY_UPDATE.ordinal()) {
            int newBatteryCharge = msg.arg1;
            batteryCharge.postValue(newBatteryCharge);
            int newEstimatedMileage = newBatteryCharge == 0 ? 0 : MAXIMUM_MILEAGE / newBatteryCharge * 100;
            estimatedMileage.postValue(newEstimatedMileage);
        } else {
            String errorMessage = String.format(
                    Locale.UK,
                    "Expected msg.what = %s (%d), got %s (%d)",
                    BATTERY_UPDATE.name(),
                    BATTERY_UPDATE.ordinal(),
                    MessageType.values()[msg.what].name(),
                    msg.what);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
