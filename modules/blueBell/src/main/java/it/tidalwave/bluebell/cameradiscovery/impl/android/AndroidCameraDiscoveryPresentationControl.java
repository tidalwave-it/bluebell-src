/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
 * %%
 * Copyright (C) 2013 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.bluebell.cameradiscovery.impl.android;

import javax.annotation.Nonnull;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.bluebell.cameradiscovery.CameraDiscoveryPresentation;
import it.tidalwave.bluebell.cameradiscovery.DefaultCameraDiscoveryPresentationControl;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Intent;
import it.tidalwave.bluebell.cameraview.impl.android.CameraPresentationActivity;
import it.tidalwave.bluebell.mobile.android.BlueBellApplication;
import static android.content.Context.WIFI_SERVICE;

/***********************************************************************************************************************
 *
 * The Android specialisation of {@link DefaultCameraDiscoveryViewControl}, which contains Android-specific code.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class AndroidCameraDiscoveryPresentationControl extends DefaultCameraDiscoveryPresentationControl
  {
    @Nonnull
    private final Activity activity;
    
    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param presentation      the controlled presentation
     * @param activity          the controlled activity
     * 
     ******************************************************************************************************************/
    public AndroidCameraDiscoveryPresentationControl (final @Nonnull CameraDiscoveryPresentation presentation,
                                                      final @Nonnull CameraDiscoveryPresentationActivity activity)
      {
        super(presentation);
        this.activity = activity;
        // TODO: poll and notify state changes
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc} 
     * 
     ******************************************************************************************************************/
    @Override
    public void showCameraPresentation (final @Nonnull CameraDevice device)
      {
        if (!device.hasApiService("camera"))
          {
            presentation.notifyDeviceNotSupported();
          }
        else
          {
            presentation.notifyDeviceName(device.getFriendlyName());
            final BlueBellApplication application = (BlueBellApplication)activity.getApplication();
            application.setCameraDevice(device);
            // We can't pass the device as an Intent extra because CameraDevice is not Serializable (it contains
            // references to other objects). 
            // TODO: check whether CameraDevice can be refactored so it's Serializable
            final Intent intent = new Intent(activity, CameraPresentationActivity.class);
            activity.startActivity(intent);
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc} 
     *
     ******************************************************************************************************************/
    @Override
    protected void populateWiFi()
      {
        final WifiManager wifiManager = (WifiManager)activity.getSystemService(WIFI_SERVICE); 

        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
          {
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            final String htmlLabel = String.format("SSID: <b>%s</b>", wifiInfo.getSSID());
            presentation.populateWiFiState(htmlLabel);
          }
        else
          {
            presentation.populateWiFiState("WiFi disconnected");
            // R.string.msg_wifi_disconnect FIXME drop
          }
      }
  }
