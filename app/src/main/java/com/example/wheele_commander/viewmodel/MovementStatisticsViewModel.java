package com.example.wheele_commander.viewmodel;

import android.os.Message;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.backend.CommunicationService;

import java.util.Locale;

/**
 * stores velocity related information of the connected hardware and handles {@code VELOCITY_UPDATE} messages.
 * <p>
 * Velocity related information refers to (total) distance, velocity, and acceleration.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see com.example.wheele_commander.viewmodel.MessageType
 */
public class MovementStatisticsViewModel extends ViewModel implements IViewModel {
    private static final String TAG = "MovementStatisticsViewModel";
    private final MutableLiveData<Float> velocity;
    private final MutableLiveData<Float> distanceTravelled;
    private Long lastReadingMillis;

    public MovementStatisticsViewModel() {
        velocity = new MutableLiveData<>(0f);
        distanceTravelled = new MutableLiveData<>(0f);
        lastReadingMillis = SystemClock.uptimeMillis();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * returns the current velocity of the connected hardware.
     *
     * @return Returns the velocity of the hardware where each unit represents
     * a <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a> per second
     * (dm/s).
     */
    public MutableLiveData<Float> getVelocity() {
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
    public void handleMessage(Message msg) {
        if (msg.what != MessageType.VELOCITY_UPDATE) {
            String errorMessage = String.format(
                    Locale.UK,
                    "Expected code %d, got %d%n", MessageType.VELOCITY_UPDATE, msg.what);
            throw new IllegalArgumentException(errorMessage);
        }

        // hardware sends scaled-up velocity value by factor of 10 so floats don't have to be exchanged
        float newVelocity = msg.arg1 / 10f;
        long elapsedTime = SystemClock.uptimeMillis() - lastReadingMillis;

        // elapsedTime is converted from milliseconds to hours
        distanceTravelled.postValue(distanceTravelled.getValue() + (elapsedTime * (velocity.getValue() + newVelocity)) / 7200000f);
        velocity.postValue(newVelocity);
        lastReadingMillis = SystemClock.uptimeMillis();
    }

    @Override
    public void registerCommunicationService(CommunicationService communicationService) {
        communicationService.getMovementMessageData().observeForever(this::handleMessage);
    }
}
