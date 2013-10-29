/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile;

import android.app.Application;

/**
 * A Application class for the sample application.
 */
public class SampleApplication extends Application {

    private ServerDevice mTargetDevice;

    /**
     * Sets a target server object to transmit to SampleCameraActivity.
     * 
     * @param device
     */
    public void setTargetServerDevice(ServerDevice device) {
        mTargetDevice = device;
    }

    /**
     * Returns a target server object to get from SampleDeviceSearchActivity.
     * 
     * @param device
     */
    public ServerDevice getTargetServerDevice() {
        return mTargetDevice;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
