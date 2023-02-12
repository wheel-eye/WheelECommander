package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.backend.NetworkClient;

/**
 * stores velocity related information of the connected hardware and handles {@code VELOCITY_UPDATE} messages.
 * <p>
 * Velocity related information refers to (total) distance, velocity, and acceleration.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see com.example.wheele_commander.viewmodel.MessageType
 */
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

    /**
     * returns the current acceleration of the connected hardware.
     *
     * @return Returns the acceleration of the hardware battery where each unit represents a
     * <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a> per square second
     * (dm/s^2).
     */
    public LiveData<Integer> getAcceleration() {
        return acceleration;
    }

    /**
     * returns the current velocity of the connected hardware.
     *
     * @return Returns the velocity of the hardware where each unit represents
     * a <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a> per second
     * (dm/s).
     */
    public LiveData<Integer> getVelocity() {
        return velocity;
    }

    /**
     * returns the total distance travelled of the connected hardware during this session.
     *
     * @return Returns the distance travelled by the hardware where each unit represents
     * a <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>.
     */
    public LiveData<Integer> getDistanceTravelled() {
        return distanceTravelled;
    }

    /**
     * Takes a {@code VELOCITY_UPDATE} message and updates the current {@code distanceTravelled},
     * {@code velocity} and {@code acceleration}.
     *
     * @param msg refer to {@link MessageType}.{@code VELOCITY_UPDATE} for clear specifications
     *            on the message structure.
     * @throws IllegalArgumentException  Thrown when given a message that isn't a
     *                                   {@code VELOCITY_UPDATE}.
     * @throws IndexOutOfBoundsException Thrown when given an unknown message nominal.
     */
    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MessageType.VELOCITY_UPDATE.ordinal()) {
            long currentReadingMillis = SystemClock.uptimeMillis();
            int newVelocity = msg.arg1; // may need to scale depending on units used by hardware
            int elapsedTime = (int) (currentReadingMillis - lastReadingMillis);

            distanceTravelled.postValue(distanceTravelled.getValue() +
                    (newVelocity + velocity.getValue()) * elapsedTime / 2 / 1000
            ); // distanceTravelled and velocity are never null
            acceleration.postValue(
                    (newVelocity - velocity.getValue()) / elapsedTime / 1000
            ); // 'can' result in division by 0 ArithmeticError
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
}
