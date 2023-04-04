package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.viewmodel.MessageType.BATTERY_UPDATE;

import android.os.Message;
import android.os.SystemClock;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.backend.CommunicationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * stores information on the battery of the connected hardware and handles {@code BATTERY_UPDATE} messages.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see com.example.wheele_commander.viewmodel.MessageType
 */
public class BatteryViewModel extends ViewModel implements IViewModel {
    private static final String TAG = "BatteryViewModel";
    /**
     * recalls the mean maximum mileage of the device on smooth terrain with no incline, as measured during testing.
     * <p>
     * each unit represents a
     * <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>
     * so that a view may display results with 1 decimal point of precision.
     */
    private static final float MAXIMUM_MILEAGE = 5f; // represents 5km
    private static final int DEFAULT_CHARGE = 0;
    private static final float DEFAULT_ESTIMATED_MILEAGE = 0f;

    private final MutableLiveData<Integer> batteryCharge;
    private final MutableLiveData<Float> estimatedMileage;
    private final List<Integer> batteryReadings;

    private float distanceTravelled;
    private long timerStartTime;

    public BatteryViewModel() {
        batteryCharge = new MutableLiveData<>(DEFAULT_CHARGE);
        estimatedMileage = new MutableLiveData<>(DEFAULT_ESTIMATED_MILEAGE);
        distanceTravelled = 0f;

        batteryReadings = new ArrayList<>();
        timerStartTime = SystemClock.uptimeMillis();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * returns the current charge of the connected hardware.
     *
     * @return Returns the charge of the hardware battery where each unit represents 0.1%
     * of battery charge.
     */
    public MutableLiveData<Integer> getBatteryCharge() {
        return batteryCharge;
    }

    /**
     * returns the estimated mileage of the connected hardware.
     *
     * @return Returns the estimated mileage of the hardware where each unit represents
     * a <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>.
     */
    public MutableLiveData<Float> getEstimatedMileage() {
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
    public void handleMessage(Message msg) {
        if (msg.what != BATTERY_UPDATE) {
            String errorMessage = String.format(Locale.UK,
                    "Expected code %d, got %d%n", BATTERY_UPDATE, msg.what);
            throw new IllegalArgumentException(errorMessage);
        }

        int receivedBattery = msg.arg1;

        if (receivedBattery <= 0) {
            batteryCharge.postValue(DEFAULT_CHARGE);
            estimatedMileage.postValue(DEFAULT_ESTIMATED_MILEAGE);
            batteryReadings.clear();
            timerStartTime = SystemClock.uptimeMillis();
            return;
        }

        // collect battery data for a couple of seconds
        if (SystemClock.uptimeMillis() - timerStartTime < 5000L) {
            batteryReadings.add(receivedBattery);
            return;
        }

        if (!batteryReadings.isEmpty()) {
            // filter outliers and calculate the new battery value
            List<Integer> filteredReadings = filterOutliers(batteryReadings);
            int newBattery = Math.floorDiv(Collections.max(filteredReadings), 10) * 10;
            int initialBattery = batteryCharge.getValue();

            if (initialBattery == 0 || hasRepeatedNumber(filteredReadings) || newBattery < initialBattery) {
                if (initialBattery != newBattery) {
                    // determine estimated mileage
                    float estimatedMileageValue = 0f;
                    if (newBattery != 0) {
                        if (initialBattery == 0 || distanceTravelled <= 0.01f) {
                            estimatedMileageValue = MAXIMUM_MILEAGE * newBattery / 100f;
                        } else {
                            estimatedMileageValue = distanceTravelled * ((float) newBattery)
                                    / ((float) Math.abs(initialBattery - newBattery));
                        }
                    }

                    estimatedMileage.postValue(estimatedMileageValue);
                }

                batteryCharge.postValue(newBattery);
            }
        }

        batteryReadings.clear();
        timerStartTime = SystemClock.uptimeMillis();
    }

    private List<Integer> filterOutliers(List<Integer> readings) {
        List<Integer> sortedReadings = new ArrayList<>(readings);
        Collections.sort(sortedReadings);
        int q1Index = (int) (sortedReadings.size() * 0.25);
        int q3Index = (int) (sortedReadings.size() * 0.75);
        int q1 = sortedReadings.get(q1Index);
        int q3 = sortedReadings.get(q3Index);
        int iqr = q3 - q1;
        int lowerBound = (int) Math.round(q1 - 0.5 * iqr);
        int upperBound = (int) Math.round(q3 + 0.5 * iqr);
        return readings.stream()
                .filter(r -> r >= lowerBound && r <= upperBound)
                .collect(Collectors.toList());
    }

    private static boolean hasRepeatedNumber(List<Integer> numbers) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int number : numbers) {
            int newCount = countMap.getOrDefault(number, 0) + 1;
            if (newCount >= 5)
                return true;
            countMap.put(number, newCount);
        }

        return false;
    }

    public void setDistanceTravelled(float distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }

    @Override
    public void registerCommunicationService(CommunicationService communicationService) {
        communicationService.getBatteryMessageData().observeForever(this::handleMessage);
    }
}
