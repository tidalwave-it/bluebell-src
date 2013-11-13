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
package it.tidalwave.bluebell.cameradiscovery;

import javax.annotation.Nonnull;
import it.tidalwave.sony.CameraDevice;
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
@RequiredArgsConstructor @Slf4j
public class DefaultCameraDiscoveryViewControl implements CameraDiscoveryViewControl
  {
    @Nonnull
    protected final CameraDiscoveryView view;

    private SsdpDiscoverer ssdpClient = new DefaultSsdpDiscoverer();

    private boolean active;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void initialize()
      {
        populateWiFi();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void activate()
      {
        active = true;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        active = false;

        if (ssdpClient.isSearching())
          {
            ssdpClient.cancelSearching();
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
        if (!ssdpClient.isSearching())
          {
            view.disableSearchButton();
            view.clearDevices();
            view.showProgressBar();

            ssdpClient.search(new SsdpDiscoverer.Callback()
              {
                @Override
                public void onDeviceFound(final CameraDevice device)
                  {
                    // Called by non-UI thread.
                    log.info(">>>> Search device found: {}", device.getFriendlyName());
                    view.renderOneMoreDevice(device);
                  }

                @Override
                public void onFinished()
                  {
                    log.info(">>>> Search finished.");
                    view.hideProgressBar();
                    view.enableSearchButton();

                    if (active)
                      {
                        view.notifySearchFinished();
                      }
                  }

                @Override
                public void onErrorFinished()
                  {
                    log.info(">>>> Search Error finished.");
                    view.hideProgressBar();
                    view.enableSearchButton();

                    if (active)
                      {
                        view.notifySearchFinishedWithError();
                      }
                  }
              });
          }
      }

    protected void populateWiFi()
      {
      }
  }
