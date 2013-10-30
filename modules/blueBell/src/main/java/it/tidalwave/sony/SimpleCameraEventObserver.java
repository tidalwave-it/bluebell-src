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
import android.util.Log;
import it.tidalwave.sony.CameraApi.EventResponse;

/***********************************************************************************************************************
 *
 * A simple observer class for some status values in Camera. This class supports only a few of values of getEvent
 * result, so please add implementation for the rest of values you want to handle.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
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

    private CameraApi mRemoteApi;

    private ChangeListener mListener;

    private boolean mWhileEventMonitoring = false;

    // Current Camera Status value.
    private String mCameraStatus;

    // Current Shoot Mode value.
    private String mShootMode;

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
        mRemoteApi = apiClient;
      }

    /**
     * Starts monitoring by continuously calling getEvent API.
     *
     * @return true if it successfully started, false if a monitoring is already
     *         started.
     */
    public boolean start()
      {
        if (mWhileEventMonitoring)
          {
            Log.w(TAG, "start() already starting.");
            return false;
          }

        mWhileEventMonitoring = true;

        new Thread()
          {
            @Override
            public void run()
              {
                Log.d(TAG, "start() exec.");
                // Call getEvent API continuously.
                boolean fisrtCall = true;
                MONITORLOOP: while (mWhileEventMonitoring)
                  {
                    // At first, call as non-Long Polling.
                    boolean longPolling = fisrtCall ? false : true;

                    try
                      {
                        final EventResponse response = mRemoteApi.getEvent(longPolling);
                        final StatusCode errorCode = response.getStatusCode();
                        Log.d(TAG, "getEvent errorCode: " + errorCode);

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
                                Log.w(TAG,
                                        "SimpleCameraEventObserver: Unexpected error: "
                                                + errorCode);
                                break MONITORLOOP; // end monitoring.
                          }

                        fireApiListModifiedListener(response.getAvailableApiList());

                        // CameraStatus
                        String cameraStatus = response.getCameraStatus();
                        Log.d(TAG, "getEvent cameraStatus: " + cameraStatus);

                        if (cameraStatus != null && !cameraStatus.equals(mCameraStatus))
                          {
                            mCameraStatus = cameraStatus;
                            fireCameraStatusChangeListener(cameraStatus);
                          }

                        // ShootMode
                        String shootMode = response.getShootMode();
                        Log.d(TAG, "getEvent shootMode: " + shootMode);

                        if (shootMode != null && !shootMode.equals(mShootMode))
                          {
                            mShootMode = shootMode;
                            fireShootModeChangeListener(shootMode);
                          }
                      }
                    catch (IOException e)
                      {
                        // Occurs when the server is not available now.
                        Log.d(TAG, "getEvent timeout by client trigger.");
                        break MONITORLOOP;
                      }
                    catch (RuntimeException e)
                      {
                        Log.w(TAG, "getEvent: JSON format error. " + e.getMessage());
                        break MONITORLOOP;
                      }

                    fisrtCall = false;
                  } // MONITORLOOP end.

                mWhileEventMonitoring = false;
              }
          }.start();

        return true;
      }

    /**
     * Requests to stop the monitoring.
     */
    public void stop()
      {
        mWhileEventMonitoring = false;
      }

    /**
     * Checks to see whether a monitoring is already started.
     *
     * @return true when monitoring is started.
     */
    public boolean isStarted()
      {
        return mWhileEventMonitoring;
      }

    /**
     * Sets a listener object.
     *
     * @param listener
     */
    public void setEventChangeListener(ChangeListener listener)
      {
        mListener = listener;
      }

    /**
     * Clears a listener object.
     */
    public void clearEventChangeListener()
      {
        mListener = null;
      }

    /**
     * Returns the current Camera Status value.
     *
     * @return camera status
     */
    public String getCameraStatus()
      {
        return mCameraStatus;
      }

    /**
     * Returns the current Shoot Mode value.
     *
     * @return shoot mode
     */
    public String getShootMode()
      {
        return mShootMode;
      }

    // Notifies the listener of available APIs change.
    private void fireApiListModifiedListener(final List<String> availableApis)
      {
        mHandler.post(new Runnable()
          {
            @Override
            public void run()
              {
                if (mListener != null)
                  {
                    mListener.onApiListModified(availableApis);
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
                if (mListener != null)
                  {
                    mListener.onCameraStatusChanged(status);
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
                if (mListener != null)
                  {
                    mListener.onShootModeChanged(shootMode);
                  }
              }
          });
      }

//    // Finds and extracts an error code from reply JSON data.
//    private static int findErrorCode (JSONObject replyJson)
//      throws JSONException
//      {
//        int code = 0; // 0 means no error.
//
//        if (replyJson.has("error"))
//          {
//            JSONArray errorObj = replyJson.getJSONArray("error");
//            code = errorObj.getInt(0);
//          }
//
//        return code;
//      }
  }
