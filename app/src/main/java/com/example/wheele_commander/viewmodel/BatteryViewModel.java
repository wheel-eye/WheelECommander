package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.viewmodel.MessageType.BATTERY_UPDATE;

import android.content.ComponentName;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.os.Message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.backend.INetworkClient;
import com.example.wheele_commander.backend.NetworkClient;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.Throws;

/**
 * stores information on the battery of the connected hardware and handles {@code BATTERY_UPDATE} messages.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see com.example.wheele_commander.viewmodel.MessageType
 * @version 1.0
 * @since 08/02/2023
 */
public class BatteryViewModel extends ViewModel implements IMessageSubscriber {
    /**
     * recalls the mean maximum mileage of the device on smooth terrain with no incline, as measured during testing.
     *
     * each unit represents a
     * <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>
     * so that a view may display results with 1 decimal point of precision.
     *
     * @since 1.0
     */
    public static final int MAXIMUM_MILEAGE = 120000;
    private final MutableLiveData<Integer> batteryCharge;
    private final MutableLiveData<Integer> estimatedMileage;
    private static INetworkClient networkClient;

    public BatteryViewModel() {
        batteryCharge = new MutableLiveData<>(1000);
        estimatedMileage = new MutableLiveData<>(MAXIMUM_MILEAGE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
            networkClient = binder.getService();
//            networkClient.subscribe(BatteryViewModel.this);
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

    /**
     * returns the current charge of the connected hardware.
     *
     * @return Returns the charge of the hardware battery where each unit represents 0.1%
     * of battery charge.
     *
     * @see LiveData
     * @since 1.0
     */
    public LiveData<Integer> getBatteryCharge() {
        return batteryCharge;
    }

    /**
     * returns the estimated mileage of the connected hardware.
     *
     * @return Returns the estimated mileage of the hardware where each unit represents
     * a <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>.
     *
     * @see LiveData
     * @since 1.0
     */
    public LiveData<Integer> getEstimatedMileage() {
        return estimatedMileage;
    }

    /**
     * Takes a {@code BATTERY_UPDATE} message and updates the current {@code batteryCharge} and
     * {@code estimatedMileage}.
     *
     * @param msg refer to {@link MessageType}.{@code BATTERY_UPDATE} for clear specifications
     * on the message structure.
     *
     * @exception  IllegalArgumentException Thrown when given a message that isn't a
     * {@code BATTERY_UPDATE}.
     * @exception IndexOutOfBoundsException Thrown when given an unknown message nominal.
     *
     * @since 1.0
     */
    @Override
    public void handleMessage(@NotNull Message msg) {
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
