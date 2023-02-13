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
    private final MutableLiveData<Float> velocity;
    private final MutableLiveData<Integer> acceleration;
    private final MutableLiveData<Float> distanceTravelled;
    private Long lastReadingMillis;

    public MovementStatisticsViewModel(@NonNull Application application) {
        super(application);
        velocity = new MutableLiveData<>(0f);
        acceleration = new MutableLiveData<>(0);
        distanceTravelled = new MutableLiveData<>(0f);
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
    public LiveData<Float> getVelocity() {
        return velocity;
    }

    /**
     * returns the total distance travelled of the connected hardware during this session.
     *
     * @return Returns the distance travelled by the hardware where each unit represents
     * a <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>.
     */
    public LiveData<Float> getDistanceTravelled() {
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
        if (msg.what != MessageType.VELOCITY_UPDATE.ordinal()) {
            throw new IllegalArgumentException(String.format("Expected msg.what = %s (%d), got %s (%d)",
                    MessageType.VELOCITY_UPDATE.name(), MessageType.VELOCITY_UPDATE.ordinal(),
                    MessageType.values()[msg.what], msg.what));
        }

        float newVelocity = msg.arg1 / 10f;
        long elapsedTime = SystemClock.uptimeMillis() - lastReadingMillis;

        // elapsedTime is converted from milliseconds to hours
        distanceTravelled.postValue(distanceTravelled.getValue() + (elapsedTime * (velocity.getValue() + newVelocity)) / 7200000f);
        velocity.postValue(newVelocity);
        lastReadingMillis = SystemClock.uptimeMillis();
    }
}
