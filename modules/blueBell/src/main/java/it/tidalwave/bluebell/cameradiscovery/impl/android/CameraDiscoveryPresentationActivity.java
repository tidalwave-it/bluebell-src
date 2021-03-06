/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
 * %%
 * Copyright (C) 2013 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.annotation.Nullable;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import it.tidalwave.bluebell.cameradiscovery.CameraDiscoveryPresentation;
import it.tidalwave.bluebell.mobile.R;
import it.tidalwave.bluebell.mobile.android.ThreadPools;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.bluebell.mobile.android.AndroidUIThreadDecoratorFactory.*;

/***********************************************************************************************************************
 *
 * @stereotype  presentation
 * 
 * An {@link Activity} that implements {@link CameraDiscoveryPresentation}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class CameraDiscoveryPresentationActivity extends Activity implements CameraDiscoveryPresentation
  {
    private static final String KEY_MEMENTO = CameraDiscoveryPresentationActivity.class.getName() + ".memento";
    
    /** The controller of this presentation. */
    private AndroidCameraDiscoveryPresentationControl control;

    // Below are widget references
    private ListView lvCameraDevices;

    private TextView tvWifiStatus;

    private Button btSearch;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderWiFiState (final @Nonnull String wiFiState)
      {
        tvWifiStatus.setText(Html.fromHtml(wiFiState));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifySearchInProgress()
      {
        setProgressBarIndeterminateVisibility(true);
        Toast.makeText(this, R.string.msg_device_search_started, Toast.LENGTH_SHORT).show();
      }
                
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifySearchFinished()
      {
        setProgressBarIndeterminateVisibility(false);
        Toast.makeText(this, R.string.msg_device_search_finish, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifySearchFinishedWithError()
      {
        Toast.makeText(this, R.string.msg_error_device_searching,  Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifySelectedDeviceName (final @Nonnull String deviceName) 
      {
        Toast.makeText(this, deviceName, Toast.LENGTH_SHORT).show();
      }
                
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifySelectedDeviceNotSupported() 
      {
        Toast.makeText(this, R.string.msg_error_non_supported_device, Toast.LENGTH_SHORT).show();
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void enableSearchButton()
      {
        btSearch.setEnabled(true);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void disableSearchButton()
      {
        btSearch.setEnabled(false);
      }

    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onSearchButtonClicked (final @Nonnull View view)
      {
        control.startDiscovery();
      }
    
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onWifiClicked (final @Nonnull View view)
      {
        control.setupWiFi();
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onCreate (final @Nullable Bundle savedInstanceState)
      {
        log.info("onCreate({})", savedInstanceState);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_camera_discovery_presentation);
        setProgressBarIndeterminateVisibility(false);
        
        lvCameraDevices = (ListView)findViewById(R.id.lv_camera_devices);
        tvWifiStatus = (TextView)findViewById(R.id.tv_wifi_status);
        btSearch = (Button)findViewById(R.id.bt_search);
        
        lvCameraDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() 
          {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) 
              {
                control.notifyCameraDeviceSelected(position);
              }
          });

        control = new AndroidCameraDiscoveryPresentationControl(createUIThreadDecorator(this, CameraDiscoveryPresentation.class), 
                                                                this,
                                                                ThreadPools.getInstance());
        if (savedInstanceState != null)
          {
            control.setMemento(savedInstanceState.getSerializable(KEY_MEMENTO));
          }
        
        lvCameraDevices.setAdapter(control.getDeviceListAdapter());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onSaveInstanceState (final @Nonnull Bundle outState) 
      {
        log.info("onSaveInstanceState({})", outState);
        super.onSaveInstanceState(outState); 
        outState.putSerializable(KEY_MEMENTO, control.getMemento());
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
        control.start();
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
        control.stop();
        super.onPause();
      }
 }
