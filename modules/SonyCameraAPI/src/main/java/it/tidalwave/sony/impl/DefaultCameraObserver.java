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
package it.tidalwave.sony.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.io.IOException;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraApi.EventResponse;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    private boolean running = false;

    @Getter
    private String cameraStatus = "";

    @Getter
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

        new Thread()
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
                        final EventResponse response = cameraApi.getEvent(!firstCall);
                        final StatusCode errorCode = response.getStatusCode();
                        log.info("getEvent errorCode {}", errorCode);

                        switch (errorCode)
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
                                log.warn("SimpleCameraEventObserver: Unexpected error: {}", errorCode);
                                break MONITORLOOP;
                          }

                        fireApiListModifiedListener(response.getAvailableApiList());

                        final String newCameraStatus = response.getCameraStatus();
                        log.debug("getEvent cameraStatus: {}", newCameraStatus);

                        if (!cameraStatus.equals(newCameraStatus))
                          {
                            cameraStatus = newCameraStatus;
                            fireCameraStatusChangeListener(cameraStatus);
                          }

                        final String newShootMode = response.getShootMode();
                        log.debug("getEvent shootMode: {}", newShootMode);

                        if (!shootMode.equals(newShootMode))
                          {
                            shootMode = newShootMode;
                            fireShootModeChangeListener(shootMode);
                          }
                      }
                    catch (IOException e)
                      {
                        // Occurs when the server is not available now.
                        log.debug("getEvent timeout by client trigger.");
                        break MONITORLOOP;
                      }
                    catch (RuntimeException e)
                      {
                        log.warn("getEvent: JSON format error. ", e);
                        break MONITORLOOP;
                      }

                    firstCall = false;
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
    public boolean isStarted()
      {
        return running;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setEventChangeListener (final @Nonnull ChangeListener listener)
      {
        this.listener = listener;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void clearEventChangeListener()
      {
        listener = null;
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of available APIs change.
     *
     *
     ******************************************************************************************************************/
    private void fireApiListModifiedListener (final @Nonnull List<String> availableApis)
      {
        if (listener != null)
          {
            listener.onApiListModified(availableApis);
          }
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of Camera Status change.
     *
     ******************************************************************************************************************/
    private void fireCameraStatusChangeListener (final @Nonnull String status)
      {
        if (listener != null)
          {
            listener.onCameraStatusChanged(status);
          }
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of Shoot Mode change.
     *
     ******************************************************************************************************************/
    private void fireShootModeChangeListener (final @Nonnull String shootMode)
      {
        if (listener != null)
          {
            listener.onShootModeChanged(shootMode);
          }
      }
  }
