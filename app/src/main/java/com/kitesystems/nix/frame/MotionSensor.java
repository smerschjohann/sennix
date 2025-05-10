package com.kitesystems.nix.frame;

import android.util.Log;

public class MotionSensor {
    private static boolean LIBRARY_LOADED;
    private static final String LIBRARY_NAME = "gpio_jni";

    public native int readMotionSensor();

    public native boolean readMotionSensorPower();

    public native void setMotionSensorPower(boolean b);

    public native int setWakeOnMotion(boolean b);

    public synchronized boolean hasMotionDetected() {
        if(LIBRARY_LOADED) {
            if(!readMotionSensorPower()) {
                setMotionSensorPower(true);
            }
            return readMotionSensor() > 0;
        }
        return false;
    }

    static {
        LIBRARY_LOADED = false;
        try {
            System.loadLibrary(LIBRARY_NAME);
            LIBRARY_LOADED = true;
        } catch (UnsatisfiedLinkError e) {
            Log.d("MotionSensor", String.format("native library %s could not be loaded: %s", LIBRARY_NAME, e.getMessage()));
        }
    }
}
