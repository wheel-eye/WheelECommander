package com.example.wheele_commander.model;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public final class Wheelchair {
    private boolean isParentalLockOn;
    private int maxVelocity;
    private final MutableLiveData<Float> velocity;
    private final MutableLiveData<Float> distanceTravelled;
    private final MutableLiveData<Integer> batteryCharge;
    private final MutableLiveData<Float> estimatedMileage;

    // K.P - this section is EXPERIMENTAL, see document (demo report 1, misc section) for full explanation of implementation
    // requires further analysis
    // Would like to introduce Guava for immutable array
    // these waste coefficients are 100% wrong, need experimentation to determine
    public static final float[] wasteCoefficients = new float[]{1,0.99f,0.98f,0.965f,0.95f};
    public static final float[] wasteCutoffs = new float[]{10,20,40,60,Float.POSITIVE_INFINITY};

    public final long[] globalPowerUseBin = new long[]{0,0,0,0,0}; // taking no chances with a long
    public long globalMeasure = 0;
    public int[] localPowerUseBin1 = new int[]{0,0,0,0,0};
    public int localMeasure1 = 0;
    public int[] localPowerUseBin2 = new int[]{0,0,0,0,0};
    public int localMeasure2 = 0;
    public boolean OneIsActiveBin = true;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int delay = 60000; // 1000 milliseconds == 1 second



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

        // K.P - EXPERIMENTAL
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                OneIsActiveBin = false;
                localPowerUseBin1 = new int[]{0,0,0,0,0};
                localMeasure1 = 0;
                handler.removeCallbacks(this);
                handler.postDelayed(this, delay);
            }
        }, delay);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                OneIsActiveBin = true;
                localPowerUseBin2 = new int[]{0,0,0,0,0};
                localMeasure2 = 0;
                handler.removeCallbacks(this);
                handler.postDelayed(this, delay);
            }
        }, delay+3000);

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
        for (int i = 0; i < wasteCutoffs.length; i++) {
            if (velocity < wasteCutoffs[i]){
                globalPowerUseBin[i]+=1;
                localPowerUseBin1[i]+=1;
                localPowerUseBin2[i]+=1;
                break;
            }
        }
        globalMeasure+=1;
        localMeasure1+=1;
        localMeasure2+=1;
    }

    public void clearLocalBin1(){
        localPowerUseBin1 = new int[]{0,0,0,0,0};
    }
    public void clearLocalBin2(){
        localPowerUseBin2 = new int[]{0,0,0,0,0};
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
