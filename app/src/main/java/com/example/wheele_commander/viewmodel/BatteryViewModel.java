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

import com.example.wheele_commander.backend.CommunicationService;

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
    private static final float MAXIMUM_MILEAGE = 12f; // represents 12km
    private final MutableLiveData<Integer> batteryCharge;
    private final MutableLiveData<Float> estimatedMileage;

    private float distanceTravelled;
    private int initialBattery;

    public BatteryViewModel(@NonNull Application application) {
        super(application);
        batteryCharge = new MutableLiveData<>(40);
        estimatedMileage = new MutableLiveData<>(MAXIMUM_MILEAGE);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                CommunicationService.CommunicationServiceBinder binder = (CommunicationService.CommunicationServiceBinder) iBinder;
                communicationService = binder.getService();

                // TODO: evil, but better than passing reference to NetworkClient via setter
                communicationService.getBatteryMessageData().observeForever(messageObserver);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: Service disconnected");
            }
        };
        initialBattery = Integer.MIN_VALUE;
        distanceTravelled = 0f;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        communicationService.getBatteryMessageData().removeObserver(messageObserver);
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
    public LiveData<Float> getEstimatedMileage() {
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
        if (msg.what != BATTERY_UPDATE) {
            String errorMessage = String.format(Locale.UK,
                    "Expected code %d, got %d%n", BATTERY_UPDATE, msg.what);
            throw new IllegalArgumentException(errorMessage);
        }

        if (initialBattery == Integer.MIN_VALUE)
            initialBattery = msg.arg1;

        int newBatteryCharge = msg.arg1;
        batteryCharge.postValue(newBatteryCharge);

        float estimatedMileageValue = 0f;
        if (newBatteryCharge != 0) {
            if (distanceTravelled <= 0.01f)
                estimatedMileageValue = MAXIMUM_MILEAGE * newBatteryCharge / 100f;
            else
                estimatedMileageValue = distanceTravelled * ((float) newBatteryCharge)
                        / ((float) (initialBattery - newBatteryCharge));
        }
        estimatedMileage.postValue(estimatedMileageValue);
    }

    public void setDistanceTravelled(float distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }
}
