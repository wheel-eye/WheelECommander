package com.example.wheele_commander.backend_thread;

public class Wheelchair {
    private boolean isParentalLockOn;
    private int maxVelocity;

    public Wheelchair() {
        // Y.S. - subject to change
        setParentalLockOn(false);
        setMaxVelocity(100);
    }

    public boolean isParentalLockOn() {
        return isParentalLockOn;
    }

    public void setParentalLockOn(boolean parentalLockOn) {
        isParentalLockOn = parentalLockOn;
    }

    public int getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(int maxVelocity) {
        this.maxVelocity = maxVelocity;
    }
}
