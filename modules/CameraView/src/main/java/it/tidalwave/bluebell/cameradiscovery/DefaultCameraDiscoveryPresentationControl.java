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
public abstract class DefaultCameraDiscoveryPresentationControl implements CameraDiscoveryPresentationControl
  {
    @Nonnull
    protected final CameraDiscoveryPresentation presentation;

    private final SsdpDiscoverer ssdpDiscoverer = new DefaultSsdpDiscoverer();

    private boolean active;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void start()
      {
        renderWiFiStatus();
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
            presentation.disableSearchButton();
            presentation.clearDeviceList();
            presentation.notifySearchInProgress();

            ssdpDiscoverer.search(new SsdpDiscoverer.Callback()
              {
                @Override
                public void onDeviceFound(final CameraDevice device)
                  {
                    log.info(">>>> Search device found: {}", device.getFriendlyName());
                    presentation.renderOneMoreDevice(device);
                  }

                @Override
                public void onFinished()
                  {
                    log.info(">>>> Search finished.");
                    presentation.enableSearchButton();

                    if (active)
                      {
                        presentation.notifySearchFinished();
                      }
                  }

                @Override
                public void onErrorFinished()
                  {
                    log.info(">>>> Search finished with error.");
                    presentation.enableSearchButton();

                    if (active)
                      {
                        presentation.notifySearchFinishedWithError();
                      }
                  }
              });
          }
      }

    /*******************************************************************************************************************
     *
     * Renders the WiFi status. This method is abstract since its actual implementation depends on Android. It must be
     * implemented in a subclass.
     *
     ******************************************************************************************************************/
    protected abstract void renderWiFiStatus();
  }
