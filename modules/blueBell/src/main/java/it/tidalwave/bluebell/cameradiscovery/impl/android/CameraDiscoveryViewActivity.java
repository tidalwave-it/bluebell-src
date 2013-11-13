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
    private final CameraDiscoveryViewControl control = new AndroidCameraDiscoveryViewControl(this);

    private Handler handler;

    private DeviceListAdapter listAdapter;

    private ListView lvDevices;

    private TextView tvWifiStatus;

    private View btSearch;

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
                tvWifiStatus.setText(Html.fromHtml(wiFiState));
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
                btSearch.setEnabled(true);
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
                Toast.makeText(CameraDiscoveryViewActivity.this, R.string.msg_device_search_finish, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CameraDiscoveryViewActivity.this, R.string.msg_error_device_searching, Toast.LENGTH_SHORT).show();
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
                btSearch.setEnabled(false);
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
    protected void onCreate (final @Nonnull Bundle savedInstanceState)
      {
        log.info("onCreate({})", savedInstanceState);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_discovery);
        setProgressBarIndeterminateVisibility(false);

        lvDevices = (ListView)findViewById(R.id.list_device);
        tvWifiStatus = (TextView)findViewById(R.id.text_wifi_ssid);
        btSearch = findViewById(R.id.button_search);

        handler = new Handler();
        listAdapter = new DeviceListAdapter(this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onResume()
      {
        log.info("onResume()");
        super.onResume();
        control.activate();
        lvDevices.setAdapter(listAdapter);

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener()
          {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id)
              {
                ListView listView = (ListView) parent;
                CameraDevice device = (CameraDevice) listView.getAdapter().getItem(position);
                launchSampleActivity(device);
              }
          });

        btSearch.setOnClickListener(new View.OnClickListener()
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
        log.info("onPause()");
        super.onPause();
        control.stop();
      }

    /*******************************************************************************************************************
     *
     * FIXME: move to controller
     *
     ******************************************************************************************************************/
    private void launchSampleActivity (final @Nonnull CameraDevice device)
      {
        if (device.hasApiService("camera"))
          {
            Toast.makeText(CameraDiscoveryViewActivity.this, device.getFriendlyName(), Toast.LENGTH_SHORT).show();
            final BlueBellApplication application = (BlueBellApplication) getApplication();
            application.setCameraDevice(device);
            final Intent intent = new Intent(this, CameraViewActivity.class);
            startActivity(intent);
          }
        else
          {
            Toast.makeText(this, R.string.msg_error_non_supported_device, Toast.LENGTH_SHORT).show();
          }
      }
  }