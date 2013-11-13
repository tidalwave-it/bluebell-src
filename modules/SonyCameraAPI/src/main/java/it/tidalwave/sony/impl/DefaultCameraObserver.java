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
package it.tidalwave.sony.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Set;
import java.io.IOException;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraApi.EventResponse;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.sony.CameraApi.Polling.*;

/***********************************************************************************************************************
 *
 * A simple observer class for some status values in Camera. This class supports only a few of values of getEvent
 * result, so please add implementation for the rest of values you want to handle.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
/* package */ class DefaultCameraObserver implements CameraObserver
  {
    @Nonnull
    private final CameraApi cameraApi;

    @CheckForNull
    private ChangeListener listener;

    @Getter
    private volatile boolean running = false;

    @Getter @Nonnull
    private String status = "";

    @Getter @Nonnull
    private String shootMode = "";

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean start()
      {
        if (running)
          {
            log.warn("start() already starting.");
            return false;
          }

        running = true;

        new Thread("CameraObserver")
          {
            @Override
            public void run()
              {
                log.debug("start() exec.");
                boolean firstCall = true;

                MONITORLOOP: while (running)
                  {
                    try
                      {
                        final EventResponse response = cameraApi.getEvent(firstCall ? LONG_POLLING : SHORT_POLLING);
                        final StatusCode statusCode = response.getStatusCode();
                        firstCall = false;
                        log.debug(">>>> statusCode {}", statusCode);

                        switch (statusCode)
                          {
                            case OK:
                                break;

                            case ANY:
                            case NO_SUCH_METHOD:
                                break MONITORLOOP;

                            case TIMEOUT:
                                continue MONITORLOOP;

                            case ALREADY_POLLING:
                                try
                                  {
                                    Thread.sleep(5000);
                                  }
                                catch (InterruptedException e)
                                  {
                                    // do nothing.
                                  }
                                continue MONITORLOOP;

                            default:
                                log.warn("SimpleCameraEventObserver: Unexpected error: {}", statusCode);
                                break MONITORLOOP;
                          }

                        fireApisChanged(response.getApis());

                        final String newStatus = response.getCameraStatus();
                        log.debug("getEvent status: {}", newStatus);

                        if (!status.equals(newStatus))
                          {
                            status = newStatus;
                            fireStatusChanged(status);
                          }

                        final String newShootMode = response.getShootMode();
                        log.debug("getEvent shootMode: {}", newShootMode);

                        if (!shootMode.equals(newShootMode))
                          {
                            shootMode = newShootMode;
                            fireShootModeChanged(shootMode);
                          }
                      }
                    catch (IOException e) // Occurs when the server is not available now.
                      {
                        log.debug("getEvent timeout by client trigger.");
                        break MONITORLOOP;
                      }
                    catch (RuntimeException e)
                      {
                        log.warn("getEvent: JSON format error. ", e);
                        break MONITORLOOP;
                      }
                  }

                running = false;
              }
          }.start();

        return true;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        running = false;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setListener (final @Nonnull ChangeListener listener)
      {
        this.listener = listener;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void unsetListener()
      {
        listener = null;
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of available APIs change.
     *
     *
     ******************************************************************************************************************/
    private void fireApisChanged (final @Nonnull Set<String> availableApis)
      {
        if (listener != null)
          {
            listener.onApisChanged(availableApis);
          }
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of Camera Status change.
     *
     ******************************************************************************************************************/
    private void fireStatusChanged (final @Nonnull String status)
      {
        if (listener != null)
          {
            listener.onStatusChanged(status);
          }
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of Shoot Mode change.
     *
     ******************************************************************************************************************/
    private void fireShootModeChanged (final @Nonnull String shootMode)
      {
        if (listener != null)
          {
            listener.onShootModeChanged(shootMode);
          }
      }
  }
