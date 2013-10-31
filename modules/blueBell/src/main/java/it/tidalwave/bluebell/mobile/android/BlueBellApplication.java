/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile.android;

import it.tidalwave.sony.CameraDevice;
import android.app.Application;
import lombok.Getter;
import lombok.Setter;

/**
 * A Application class for the sample application.
 */
public class BlueBellApplication extends Application
  {
    @Getter @Setter
    private CameraDevice cameraDevice;
  }
