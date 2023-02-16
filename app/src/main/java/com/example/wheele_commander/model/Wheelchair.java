package com.example.wheele_commander.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public final class Wheelchair {
    private boolean isParentalLockOn;
    private int maxVelocity;
    private final MutableLiveData<Float> velocity;
    private final MutableLiveData<Float> distanceTravelled;
    private final MutableLiveData<Integer> batteryCharge;
    private final MutableLiveData<Float> estimatedMileage;

    /**
     * recalls the mean maximum mileage of the device on smooth terrain with no incline, as measured during testing.
     * <p>
     * each unit represents a
     * <a href="https://physics.nist.gov/cuu/Units/prefixes.html">decimeter</a>
     * so that a view may display results with 1 decimal point of precision.
     */
    public static final float MAXIMUM_MILEAGE = 1200f;


    public Wheelchair() {
        this.velocity = new MutableLiveData<>(0f);
        this.distanceTravelled = new MutableLiveData<>(0f);
        this.batteryCharge = new MutableLiveData<>(1000);
        this.estimatedMileage = new MutableLiveData<>(MAXIMUM_MILEAGE);

        // Y.S. - subject to change
        setParentalLockOn(false);
    }

    /**
     * returns the current velocity of the connected hardware.
     *
     * @return Returns the velocity of the hardware where each unit represents
     * a kilometer per hour (km/h).
     */
    public LiveData<Float> getVelocity(){
        return this.velocity;
    }
    /**
     * sets the current velocity of the connected hardware.
     *
     * @param velocity the velocity of the hardware where each unit represents
     * a kilometer per hour (km/h).
     */
    public void setVelocity(float velocity){
        this.velocity.setValue(velocity);
    }

    /**
     * returns the total distance travelled of the connected hardware during this session.
     *
     * @return Returns the distance travelled by the hardware where each unit represents
     * a kilometer (km).
     */
    public LiveData<Float> getDistanceTravelled(){
        return this.distanceTravelled;
    }
    /**
     * sets the total distance travelled of the connected hardware during this session.
     *
     * @param distanceTravelled the total distance travelled of the hardware where each unit
     *                          represents a kilometer (km).
     */
    public void setDistanceTravelled(float distanceTravelled){
        this.distanceTravelled.setValue(distanceTravelled);
    }

    /**
     * returns the battery charge of the connected hardware.
     *
     * @return Returns the battery charge of the hardware where each unit represents
     * 0.1% of total battery charge.
     */
    public LiveData<Integer> getBatteryCharge(){
        return this.batteryCharge;
    }
    /**
     * sets the current battery charge of the connected hardware.
     *
     * @param batteryCharge the total battery charge, assumed to be bounded by [1000,0] where
     *                      a unit represents 0.1% battery charge.
     */
    public void setBatteryCharge(int batteryCharge){
        this.batteryCharge.setValue(batteryCharge);
    }

    /**
     * returns the estimated mileage of the connected hardware.
     *
     * @return Returns the estimated mileage of the hardware where each unit represents
     * a kilometer (km).
     */
    public LiveData<Float> getEstimatedMileage(){
        return this.estimatedMileage;
    }
    /**
     * sets the current estimated mileage of the connected hardware.
     *
     * @param estimatedMileage the estimated mileage of the hardware where each unit represents
     *                         a kilometer (km).
     */
    public void setEstimatedMileage(float estimatedMileage){
        this.estimatedMileage.setValue(estimatedMileage);
    }


    public boolean isParentalLockOn() {
        return isParentalLockOn;
    }

    public void setParentalLockOn(boolean parentalLockOn) {
        isParentalLockOn = parentalLockOn;
    }

    public int getMaxVelocity() {
        return this.maxVelocity;
    }

    public void setMaxVelocity(int maxVelocity) {
        this.maxVelocity = maxVelocity;
    }
}
