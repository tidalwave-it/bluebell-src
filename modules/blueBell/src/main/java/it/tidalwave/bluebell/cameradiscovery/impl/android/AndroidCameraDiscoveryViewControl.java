/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
 * %%
 * Copyright (C) 2013 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import it.tidalwave.bluebell.cameradiscovery.CameraDiscoveryView;
import it.tidalwave.bluebell.cameradiscovery.DefaultCameraDiscoveryViewControl;
import static android.content.Context.WIFI_SERVICE;
import it.tidalwave.bluebell.mobile.R;

/***********************************************************************************************************************
 *
 * The Android specialisation of {@link DefaultCameraDiscoveryViewControl} that is able to populate the WiFi state.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class AndroidCameraDiscoveryViewControl extends DefaultCameraDiscoveryViewControl
  {
    @Nonnull
    private final WifiManager wifiManager;

    public AndroidCameraDiscoveryViewControl (final @Nonnull CameraDiscoveryView view)
      {
        super(view);
        wifiManager = (WifiManager)((Activity)view).getSystemService(WIFI_SERVICE); // FIXME: getContext
        // TODO: poll and notify state changes
      }

    @Override
    protected void populateWiFi()
      {
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
          {
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            final String htmlLabel = String.format("SSID: <b>%s</b>", wifiInfo.getSSID());
            view.populateWiFiState(htmlLabel);
          }
        else
          {
            view.populateWiFiState("WiFi disconnected");
            // R.string.msg_wifi_disconnect FIXME drop
          }
      }
  }