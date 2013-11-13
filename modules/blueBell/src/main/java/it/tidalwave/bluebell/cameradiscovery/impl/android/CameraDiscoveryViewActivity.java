/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
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
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.bluebell.cameraview.impl.android.CameraViewActivity;
import it.tidalwave.bluebell.cameradiscovery.CameraDiscoveryView;
import it.tidalwave.bluebell.cameradiscovery.CameraDiscoveryViewControl;
import it.tidalwave.bluebell.mobile.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import it.tidalwave.bluebell.mobile.android.BlueBellApplication;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * An Activity class of Device Discovery screen.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class CameraDiscoveryViewActivity extends Activity implements CameraDiscoveryView
  {
    private Handler handler;

    private DeviceListAdapter listAdapter;

    private final CameraDiscoveryViewControl control = new AndroidCameraDiscoveryViewControl(this);

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void populateWiFiState (final @Nonnull String wiFiState)
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                final TextView textWifiSsid = (TextView)findViewById(R.id.text_wifi_ssid); // FIXME
                textWifiSsid.setText(Html.fromHtml(wiFiState));
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void hideProgressBar()
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                setProgressBarIndeterminateVisibility(false);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void enableSearchButton()
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                findViewById(R.id.button_search).setEnabled(true);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifySearchFinished()
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                Toast.makeText(CameraDiscoveryViewActivity.this,
                               R.string.msg_device_search_finish,
                               Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifySearchFinishedWithError()
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                Toast.makeText(CameraDiscoveryViewActivity.this,
                               R.string.msg_error_device_searching,
                               Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderOneMoreDevice (final @Nonnull CameraDevice device)
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                listAdapter.addDevice(device);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void disableSearchButton()
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                findViewById(R.id.button_search).setEnabled(false);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void clearDevices()
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                listAdapter.clearDevices();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void showProgressBar()
      {
        handler.post(new Runnable()
          {
            public void run()
              {
                setProgressBarIndeterminateVisibility(true);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onCreate (Bundle savedInstanceState)
      {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_discovery);
        setProgressBarIndeterminateVisibility(false);

        handler = new Handler();
        listAdapter = new DeviceListAdapter(this);

        log.debug("onCreate() completed.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onResume()
      {
        super.onResume();
        control.activate();
        ListView listView = (ListView) findViewById(R.id.list_device);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
          {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id)
              {
                ListView listView = (ListView) parent;
                CameraDevice device = (CameraDevice) listView.getAdapter().getItem(position);
                launchSampleActivity(device);
              }
          });

        findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick (View v)
              {
                control.startDiscovery();
              }
          });

        control.initialize();

        log.debug("onResume() completed.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onPause()
      {
        super.onPause();
        control.stop();
        log.debug("onPause() completed.");
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Launch a SampleCameraActivity.
    private void launchSampleActivity (final @Nonnull CameraDevice device)
      {
        if (device.hasApiService("camera"))
          {
            // Go to CameraSampleActivity.
            Toast.makeText(CameraDiscoveryViewActivity.this,
                    device.getFriendlyName(), Toast.LENGTH_SHORT).show();

            // Set target ServerDevice instance to control in Activity.
            BlueBellApplication app = (BlueBellApplication) getApplication();
            app.setCameraDevice(device);
            Intent intent = new Intent(this, CameraViewActivity.class);
            startActivity(intent);
          }
        else
          {
            Toast.makeText(this, R.string.msg_error_non_supported_device,
                    Toast.LENGTH_SHORT).show();
          }
      }
  }
