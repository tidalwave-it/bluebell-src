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
package it.tidalwave.bluebell.cameradiscovery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.io.Serializable;
import it.tidalwave.sony.CameraDescriptor;
import it.tidalwave.sony.SsdpDiscoverer;
import it.tidalwave.sony.impl.DefaultSsdpDiscoverer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public abstract class DefaultCameraDiscoveryPresentationControl implements CameraDiscoveryPresentationControl
  {
    protected final static String NO_SSID = "";
    
    /** The presentation that this controller is managing. */
    @Nonnull
    protected final CameraDiscoveryPresentation presentation;

    /** The facility for discovering devices. */
    private final SsdpDiscoverer ssdpDiscoverer;

    /** Whether this controller is active. */
    private boolean active;
    
    /** The SSID of the current Wifi. */
    private String currentSsid = NO_SSID;

    /** The list of discovered devices. */
    protected final List<CameraDescriptor> cameraDescriptors =
            Collections.synchronizedList(new ArrayList<CameraDescriptor>());
    
    /*******************************************************************************************************************
     *
     * An opaque object representing the internal status of this controller. 
     * See the GoF Memento Pattern: http://en.wikipedia.org/wiki/Memento_pattern
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor
    static class Memento implements Serializable
      {
        private static final long serialVersionUID = 56546340987457L;
        final List<CameraDescriptor> cameraDescriptors;
        final String currentSsid;
      }

    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param   presentation            the controlled presentation
     * @param   executorService         an {@link ExecutorService} for running background jobs
     * 
     ******************************************************************************************************************/
    public DefaultCameraDiscoveryPresentationControl (final @Nonnull CameraDiscoveryPresentation presentation,
                                                      final @Nonnull ExecutorService executorService)
      {
        this.presentation = presentation;
        this.ssdpDiscoverer = new DefaultSsdpDiscoverer(executorService);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void start()
      {
        log.info("start()");
        checkWifiStatusChange();
        active = true;
        
        if (cameraDescriptors.isEmpty() && !ssdpDiscoverer.isSearching())
          {
            startDiscovery();
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        log.info("stop()");
        active = false;

        if (ssdpDiscoverer.isSearching())
          {
            ssdpDiscoverer.cancelSearching();
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void startDiscovery()
      {
        if (!ssdpDiscoverer.isSearching())
          {
            cameraDescriptors.clear();
            notifyDevicesChanged();
            presentation.disableSearchButton();
            presentation.notifySearchInProgress();

            ssdpDiscoverer.search(new SsdpDiscoverer.Callback()
              {
                @Override
                public void onDeviceFound (final CameraDescriptor cameraDescriptor)
                  {
                    if (active)
                      {
                        log.info(">>>> Search found device: {}", cameraDescriptor.getFriendlyName());
                        cameraDescriptors.add(cameraDescriptor);
                        notifyDevicesChanged();
                      }
                  }

                @Override
                public void onFinished()
                  {
                    if (active)
                      {
                        log.info(">>>> Search finished.");
                        presentation.enableSearchButton();
                        presentation.notifySearchFinished();
                      }
                  }

                @Override
                public void onErrorFinished()
                  {
                    if (active)
                      {
                        log.warn(">>>> Search finished with error.");
                        presentation.enableSearchButton();
                        presentation.notifySearchFinishedWithError();
                      }
                  }
              });
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void setupWiFi() 
      {
        openWifiSettings();
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Serializable getMemento() 
      {
        return new Memento(cameraDescriptors, currentSsid);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setMemento (final @Nullable Serializable memento) 
      {
        log.info("setMemento({})", memento);
        
        if (memento != null)
          {
            final Memento m = (Memento)memento;
            cameraDescriptors.clear();
            cameraDescriptors.addAll(m.cameraDescriptors);
            currentSsid = m.currentSsid;
          }
      }

    /*******************************************************************************************************************
     *
     * Sets the current SSID of the Wifi network.
     * 
     * @param       ssid        the SSID
     *
     ******************************************************************************************************************/
    protected void setWifiSsid (final @Nonnull String ssid)
      {
        log.debug("setWifiSsid({})", ssid);
        
        if (!ssid.equals(currentSsid))
          {
            currentSsid = ssid;
            
            if (currentSsid.equals(NO_SSID))
              {
                cameraDescriptors.clear();
                notifyDevicesChanged();
                presentation.renderWiFiState("WiFi disconnected"); // R.string.msg_wifi_disconnect FIXME drop
              }
            else
              {
                presentation.renderWiFiState(String.format("SSID: <b>%s</b>", ssid));
                startDiscovery();
              }
          }
      }
    
    /*******************************************************************************************************************
     *
     * Checks the Wifi status. This method is abstract since its actual implementation depends on Android. It must be
     * implemented in a subclass.
     *
     ******************************************************************************************************************/
    protected abstract void checkWifiStatusChange();
    
    /*******************************************************************************************************************
     *
     * Sends a notification that the device list has been changed. It is called each time the field 
     * {@code cameraDescriptors} is modified. It could be dropped if {@code cameraDescriptors} supported binding - but
     * at the moment there's no simple solution for this in Android.
     *
     ******************************************************************************************************************/
    protected abstract void notifyDevicesChanged();
    
    /*******************************************************************************************************************
     *
     * Opens the UI for setting up the WiFi.
     *
     ******************************************************************************************************************/
    protected abstract void openWifiSettings();
  }
