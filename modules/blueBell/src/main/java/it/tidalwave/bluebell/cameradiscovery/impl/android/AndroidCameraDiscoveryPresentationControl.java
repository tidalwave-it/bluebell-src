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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import it.tidalwave.sony.CameraDescriptor;
import it.tidalwave.bluebell.cameradiscovery.CameraDiscoveryPresentation;
import it.tidalwave.bluebell.cameradiscovery.DefaultCameraDiscoveryPresentationControl;
import it.tidalwave.bluebell.cameraview.impl.android.CameraPresentationActivity;
import lombok.Getter;
import static android.content.Context.WIFI_SERVICE;

/***********************************************************************************************************************
 *
 * The Android specialisation of {@link DefaultCameraDiscoveryPresentationControl} that contains Android-specific code.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class AndroidCameraDiscoveryPresentationControl extends DefaultCameraDiscoveryPresentationControl
  {
    @Nonnull
    private final Context context;
    
    @Getter
    private final DeviceListAdapter deviceListAdapter;
    
    private final Handler handler = new Handler();
    
    /*******************************************************************************************************************
     *
     * Receiver of Wifi state change notifications.
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
        deviceListAdapter = new DeviceListAdapter(context, cameraDescriptors);
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
    public void notifyCameraDeviceSelected (final @Nonnegative int index)
      {
        final CameraDescriptor cameraDescriptor = cameraDescriptors.get(index);
        
        if (!cameraDescriptor.createDevice().hasApiService("camera"))
          {
            presentation.notifySelectedDeviceNotSupported();
          }
        else
          {
            presentation.notifySelectedDeviceName(cameraDescriptor.getFriendlyName());
            final Intent intent = new Intent(context, CameraPresentationActivity.class);
            intent.putExtra("cameraDescriptor", cameraDescriptor);
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
     * {@inheritDoc} 
     *
     ******************************************************************************************************************/
    @Override
    protected void notifyDevicesChanged() 
      {
        handler.post(new Runnable() 
          {
            @Override
            public void run() 
              {
                deviceListAdapter.notifyDataSetChanged();
              }
          });
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc} 
     *
     ******************************************************************************************************************/
    @Override
    protected void openWifiSettings() 
      {
        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
      }
    
    /*******************************************************************************************************************
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
     ******************************************************************************************************************/
    private void unregisterWiFiReceiver() 
      {
        context.unregisterReceiver(wiFiReceiver);
      }
  }
