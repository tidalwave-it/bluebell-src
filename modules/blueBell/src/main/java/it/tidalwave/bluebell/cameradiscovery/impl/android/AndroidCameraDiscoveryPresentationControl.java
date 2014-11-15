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
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.bluebell.cameradiscovery.CameraDiscoveryPresentation;
import it.tidalwave.bluebell.cameradiscovery.DefaultCameraDiscoveryPresentationControl;
import it.tidalwave.bluebell.cameraview.impl.android.CameraPresentationActivity;
import it.tidalwave.bluebell.mobile.android.BlueBellApplication;
import static android.content.Context.WIFI_SERVICE;

/***********************************************************************************************************************
 *
 * The Android specialisation of {@link DefaultCameraDiscoveryViewControl} that contains Android-specific code.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class AndroidCameraDiscoveryPresentationControl extends DefaultCameraDiscoveryPresentationControl
  {
    @Nonnull
    private final Context context;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private final BroadcastReceiver wiFiReceiver = new BroadcastReceiver()
      {
        @Override
        public void onReceive (final Context context, final Intent intent) 
          {
            checkWifiStatusChange();
          }
      };
    
    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param presentation      the controlled presentation
     * @param context           the Android {@link Context}
     * 
     ******************************************************************************************************************/
    public AndroidCameraDiscoveryPresentationControl (final @Nonnull CameraDiscoveryPresentation presentation,
                                                      final @Nonnull Context context)
      {
        super(presentation);
        this.context = context;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc} 
     * 
     ******************************************************************************************************************/
    @Override
    public void start()
      {
        super.start(); 
        registerWiFiReceiver();
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc} 
     * 
     ******************************************************************************************************************/
    @Override
    public void stop() 
      {
        unregisterWiFiReceiver();
        super.stop(); 
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
            presentation.notifySelectedDeviceNotSupported();
          }
        else
          {
            presentation.notifySelectedDeviceName(device.getFriendlyName());
            final BlueBellApplication application = (BlueBellApplication)((Activity)context).getApplication();
            application.setCameraDevice(device);
            // We can't pass the device as an Intent extra because CameraDevice is not Serializable (it contains
            // references to other objects). 
            // TODO: check whether CameraDevice can be refactored so it's Serializable
            final Intent intent = new Intent(context, CameraPresentationActivity.class);
            context.startActivity(intent);
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc} 
     *
     ******************************************************************************************************************/
    @Override
    protected void checkWifiStatusChange()
      {
        final WifiManager wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE); 
        final String ssid = wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED ?
                "" : wifiManager.getConnectionInfo().getSSID();
        setWifiSsid(ssid);
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private void registerWiFiReceiver() 
      {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(wiFiReceiver, intentFilter); 
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void unregisterWiFiReceiver() 
      {
        context.unregisterReceiver(wiFiReceiver);
      }
  }
