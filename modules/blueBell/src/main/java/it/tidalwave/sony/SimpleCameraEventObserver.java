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
public class SimpleCameraEventObserver
  {
    private static final String TAG = SimpleCameraEventObserver.class.getSimpleName();

    /**
     * A listener interface to receive these changes. These methods will be
     * called by UI thread.
     */
    public interface ChangeListener
      {
        /**
         * Called when the list of available APIs is modified.
         *
         * @param apis a list of available APIs
         */
        void onApiListModified(List<String> apis);

        /**
         * Called when the value of "Camera Status" is changed.
         *
         * @param status camera status (ex."IDLE")
         */
        void onCameraStatusChanged(String status);

        /**
         * Called when the value of "Shoot Mode" is changed.
         *
         * @param shootMode shoot mode (ex."still")
         */
        void onShootModeChanged(String shootMode);
      }

    private Handler mHandler;

    private CameraApi cameraApi;

    private ChangeListener listener;

    private boolean running = false;

    private String cameraStatus;

    private String shootMode;

    // :
    // : add attributes for Event data as necessary.

    /**
     * Constructor.
     *
     * @param handler handler to notify the changes by UI thread.
     * @param apiClient API client
     */
    public SimpleCameraEventObserver (Handler handler, CameraApi apiClient)
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

    /**
     * Starts monitoring by continuously calling getEvent API.
     *
     * @return true if it successfully started, false if a monitoring is already
     *         started.
     */
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
                        log.debug("getEvent errorCode {}", errorCode);

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

                        if (cameraStatus != null && !cameraStatus.equals(cameraStatus))
                          {
                            cameraStatus = cameraStatus;
                            fireCameraStatusChangeListener(cameraStatus);
                          }

                        // ShootMode
                        String shootMode = response.getShootMode();
                        log.debug("getEvent shootMode: {}", shootMode);

                        if (shootMode != null && !shootMode.equals(shootMode))
                          {
                            shootMode = shootMode;
                            fireShootModeChangeListener(shootMode);
                          }
                      }
                    catch (IOException e)
                      {
                        // Occurs when the server is not available now.
                        log.debug(TAG, "getEvent timeout by client trigger.");
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

    /**
     * Requests to stop the monitoring.
     */
    public void stop()
      {
        running = false;
      }

    /**
     * Checks to see whether a monitoring is already started.
     *
     * @return true when monitoring is started.
     */
    public boolean isStarted()
      {
        return running;
      }

    /**
     * Sets a listener object.
     *
     * @param listener
     */
    public void setEventChangeListener(ChangeListener listener)
      {
        listener = listener;
      }

    /**
     * Clears a listener object.
     */
    public void clearEventChangeListener()
      {
        listener = null;
      }

    /**
     * Returns the current Camera Status value.
     *
     * @return camera status
     */
    public String getCameraStatus()
      {
        return cameraStatus;
      }

    /**
     * Returns the current Shoot Mode value.
     *
     * @return shoot mode
     */
    public String getShootMode()
      {
        return shootMode;
      }

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
