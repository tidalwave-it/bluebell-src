/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile;

import it.tidalwave.sony.CameraDevice;
import android.app.Application;

/**
 * A Application class for the sample application.
 */
public class SampleApplication extends Application {

    private CameraDevice mTargetDevice;

    /**
     * Sets a target server object to transmit to SampleCameraActivity.
     * 
     * @param device
     */
    public void setTargetServerDevice(CameraDevice device) {
        mTargetDevice = device;
    }

    /**
     * Returns a target server object to get from SampleDeviceSearchActivity.
     * 
     * @param device
     */
    public CameraDevice getTargetServerDevice() {
        return mTargetDevice;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
