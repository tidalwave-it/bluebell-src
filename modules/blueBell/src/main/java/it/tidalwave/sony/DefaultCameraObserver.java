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
package it.tidalwave.sony;

import java.util.List;
import java.io.IOException;
import android.os.Handler;
import it.tidalwave.sony.CameraApi.EventResponse;
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
@Slf4j
public class DefaultCameraObserver implements CameraObserver
  {
    private Handler mHandler;

    private CameraApi cameraApi;

    private ChangeListener listener;

    private boolean running = false;

    private String cameraStatus;

    private String shootMode;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public DefaultCameraObserver (Handler handler, CameraApi apiClient)
      {
        if (handler == null)
          {
            throw new IllegalArgumentException("handler is null.");
          }

        if (apiClient == null)
          {
            throw new IllegalArgumentException("apiClient is null.");
          }

        mHandler = handler;
        cameraApi = apiClient;
      }

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
                // Call getEvent API continuously.
                boolean fisrtCall = true;
                MONITORLOOP: while (running)
                  {
                    // At first, call as non-Long Polling.
                    boolean longPolling = fisrtCall ? false : true;

                    try
                      {
                        final EventResponse response = cameraApi.getEvent(longPolling);
                        final StatusCode errorCode = response.getStatusCode();
                        log.info("getEvent errorCode {}", errorCode);

                        switch (errorCode)
                          {
                            case OK: // no error
                                // Pass through.
                                break;

                            case ANY: // "Any" error
                            case NO_SUCH_METHOD: // "No such method" error
                                break MONITORLOOP; // end monitoring.

                            case TIMEOUT: // "Timeout" error
                                // Re-call immediately.
                                continue MONITORLOOP;

                            case ALREADY_POLLING: // "Already polling" error
                                // Retry after 5 sec.
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
                                break MONITORLOOP; // end monitoring.
                          }

                        fireApiListModifiedListener(response.getAvailableApiList());

                        // CameraStatus
                        String cameraStatus = response.getCameraStatus();
                        log.debug("getEvent cameraStatus: {}", cameraStatus);

                        if (cameraStatus != null && !cameraStatus.equals(DefaultCameraObserver.this.cameraStatus))
                          {
                            DefaultCameraObserver.this.cameraStatus = cameraStatus;
                            fireCameraStatusChangeListener(cameraStatus);
                          }

                        // ShootMode
                        String shootMode = response.getShootMode();
                        log.debug("getEvent shootMode: {}", shootMode);

                        if (shootMode != null && !shootMode.equals(DefaultCameraObserver.this.shootMode))
                          {
                            DefaultCameraObserver.this.shootMode = shootMode;
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

                    fisrtCall = false;
                  } // MONITORLOOP end.

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
    public void setEventChangeListener(ChangeListener listener)
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
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public String getCameraStatus()
      {
        return cameraStatus;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public String getShootMode()
      {
        return shootMode;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Notifies the listener of available APIs change.
    private void fireApiListModifiedListener(final List<String> availableApis)
      {
        mHandler.post(new Runnable()
          {
            @Override
            public void run()
              {
                if (listener != null)
                  {
                    listener.onApiListModified(availableApis);
                  }
              }
          });
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Notifies the listener of Camera Status change.
    private void fireCameraStatusChangeListener(final String status)
      {
        mHandler.post(new Runnable()
          {
            @Override
            public void run()
              {
                if (listener != null)
                  {
                    listener.onCameraStatusChanged(status);
                  }
              }
          });
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Notifies the listener of Shoot Mode change.
    private void fireShootModeChangeListener(final String shootMode)
      {
        mHandler.post(new Runnable()
          {
            @Override
            public void run()
              {
                if (listener != null)
                  {
                    listener.onShootModeChanged(shootMode);
                  }
              }
          });
      }
  }
