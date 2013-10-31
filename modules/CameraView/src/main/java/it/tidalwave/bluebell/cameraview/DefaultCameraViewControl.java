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
package it.tidalwave.bluebell.cameraview;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultCameraViewControl implements CameraViewControl
  {
    @Nonnull
    private final CameraView view;

    @Getter // FIXME: temporary
    private CameraDevice cameraDevice;

    @Getter // FIXME: temporary
    private CameraApi cameraApi;

    @Getter // FIXME: temporary
    private CameraObserver cameraObserver;

    private boolean liveViewStarted;

    private final Set<String> availableApis = Collections.synchronizedSet(new TreeSet<String>());

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void bind (final @Nonnull CameraDevice cameraDevice)
      {
        this.cameraDevice = cameraDevice;
        cameraApi = cameraDevice.getApi();
        cameraObserver = cameraDevice.getObserver();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    // Open connection to the camera device to start monitoring Camera events
    // and showing liveview.
    @Override
    public void initialize()
      {
        cameraObserver.setListener(new CameraObserver.ChangeListener()
          {
            @Override
            public void onShootModeChanged (final @Nonnull String shootMode)
              {
                log.info("onShootModeChanged({})", shootMode);
                refreshUi();
              }

            @Override
            public void onStatusChanged (final @Nonnull String status)
              {
                log.info("onStatusChanged({})", status);
                refreshUi();
              }

            @Override
            public void onApisChanged (final @Nonnull Set<String> apis)
              {
                log.info("onApisChanged({})", apis);
                setAvailableApis(apis);
              }
          });

        new Thread()
          {
            @Override
            public void run()
              {
                log.info("openConnection(): exec.");

                try
                  {
                    setAvailableApis(cameraApi.getAvailableApiList().getApis());

                    // check version of the server device
                    if (isApiAvailable("getApplicationInfo"))
                      {
                        log.info("openConnection(): getApplicationInfo()");
                        final JSONObject replyJson = cameraApi.getApplicationInfo().getJsonObject();

                        if (!isSupportedServerVersion(replyJson))
                          {
                            view.notifyDeviceNotSupportedAndQuit();
                            return;
                          }
                      }
                    else
                      {
                        // never happens;
                        return;
                      }

                    if (isApiAvailable("startRecMode"))
                      {
                        log.info("openConnection(): startRecMode()");
                        cameraApi.startRecMode();
                        setAvailableApis(cameraApi.getAvailableApiList().getApis());
                      }

                    if (isApiAvailable("getEvent"))
                      {
                        log.info("openConnection(): EventObserver.start()");
                        cameraObserver.start();
                      }

                    if (isApiAvailable("startLiveview"))
                      {
                        log.info("openConnection(): LiveviewSurface.start()");
                        view.startLiveView();
                        liveViewStarted = true;
                      }

                    if (isApiAvailable("getAvailableShootMode"))
                      {
                        log.info("openConnection(): prepareShootModeRadioButtons()");
                        prepareShootModeRadioButtons();
                      }

                    log.info("openConnection(): completed.");
                  }
                catch (IOException e)
                  {
                    log.warn("openConnection: IOException: ", e);
                    view.notifyConnectionError();
                  }
              }
          }.start();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        new Thread()
          {
            @Override
            public void run()
              {
                log.info("closeConnection(): exec.");

                try
                  {
                    // Liveview stop
                    log.info("closeConnection(): LiveviewSurface.stop()");
                    view.stopLiveView();
                    liveViewStarted = false;

                    // getEvent stop
                    log.info("closeConnection(): EventObserver.stop()");
                    cameraObserver.stop();

                    // stopRecMode if necessary.
                    if (isApiAvailable("stopRecMode"))
                      {
                        log.info("closeConnection(): stopRecMode()");
                        cameraApi.stopRecMode();
                      }

                    log.info("closeConnection(): completed.");
                  }
                catch (IOException e)
                  {
                    log.warn("closeConnection: IOException: ", e);
                  }
              }
          }.start();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void takeAndFetchPicture()
      {
        if (!liveViewStarted)
          {
            view.notifyErrorWhileTakingPhoto();
            return;
          }

        new Thread()
          {
            @Override
            public void run()
              {
                try
                  {
                    final JSONObject replyJson = cameraApi.actTakePicture().getJsonObject();
                    final JSONArray resultsObj = replyJson.getJSONArray("result");
                    final JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                    String postImageUrl = null;

                    if (1 <= imageUrlsObj.length())
                      {
                        postImageUrl = imageUrlsObj.getString(0);
                      }

                    if (postImageUrl == null)
                      {
                        log.warn("takeAndFetchPicture: post image URL is null.");
                        view.notifyErrorWhileTakingPhoto();
                        return;
                      }

                    view.showProgressBar();
                    final URL url = new URL(postImageUrl);
                    final Object picture = loadPicture(url);
                    view.showPhoto(picture);
                  }
                catch (IOException e)
                  {
                    log.warn("IOException while closing slicer: ", e);
                    view.notifyErrorWhileTakingPhoto();
                  }
                catch (JSONException e)
                  {
                    log.warn("JSONException while closing slicer");
                    view.notifyErrorWhileTakingPhoto();
                  }
                finally
                  {
                    view.hideProgressBar();
                  }
              }
          }.start();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void startMovieRec()
      {
        new Thread()
          {
            @Override
            public void run()
              {
                try
                  {
                    log.info("startMovieRec: exec.");
                    final JSONObject replyJson = cameraApi.startMovieRec().getJsonObject();
                    final JSONArray resultsObj = replyJson.getJSONArray("result");
                    final int resultCode = resultsObj.getInt(0);

                    if (resultCode == 0)
                      {
                        view.notifyRecStart();
                      }
                    else
                      {
                        log.warn("startMovieRec: error: {}", resultCode);
                        view.notifyErrorWhileRecordingMovie();
                      }
                  }
                catch (IOException e)
                  {
                    log.warn("startMovieRec: IOException: ", e);
                  }
                catch (JSONException e)
                  {
                    log.warn("startMovieRec: JSON format error.");
                  }
              }
          }.start();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stopMovieRec()
      {
        new Thread()
          {
            @Override
            public void run()
              {
                try
                  {
                    log.info("stopMovieRec: exec.");
                    final JSONObject replyJson = cameraApi.stopMovieRec().getJsonObject();
                    final JSONArray resultsObj = replyJson.getJSONArray("result");
                    final String thumbnailUrl = resultsObj.getString(0);

                    if (thumbnailUrl != null)
                      {
                        view.notifyRecStop();
                      }
                    else
                      {
                        log.warn("stopMovieRec: error");
                        view.notifyErrorWhileRecordingMovie();
                      }
                  }
                catch (IOException e)
                  {
                    log.warn("stopMovieRec: IOException: ", e);
                  }
                catch (JSONException e)
                  {
                    log.warn("stopMovieRec: JSON format error.");
                  }
              }
          }.start();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected Object loadPicture (final @Nonnull URL url)
      throws IOException
      {
        return null;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void refreshUi()
      {
        final String cameraStatus = cameraObserver.getStatus();
        final String shootMode = cameraObserver.getShootMode();

        view.renderCameraStatus(cameraStatus);

        if ("MovieRecording".equals(cameraStatus))
          {
            view.renderRecStartStopButtonAsStop();
          }
        else if ("IDLE".equals(cameraStatus) && "movie".equals(shootMode))
          {
            view.renderRecStartStopButtonAsStart();
          }
        else
          {
            view.disableRecStartStopButton();
          }

        view.enableTakePhotoButton("still".equals(shootMode) && "IDLE".equals(cameraStatus));

        if (!"still".equals(shootMode))
          {
            view.hidePhotoBox();
          }

        if ("IDLE".equals(cameraStatus))
          {
            view.enableShootModeSelector(shootMode);
          }
        else
          {
            view.disableShootModeSelector();
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void setAvailableApis (final @Nonnull Set<String> availableApis)
      {
        synchronized (this.availableApis)
          {
            this.availableApis.clear();
            this.availableApis.addAll(availableApis);
            log.info(">>>> available APIs: {}", availableApis);
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Prepare for RadioButton to select "shootMode" by user.
    private void prepareShootModeRadioButtons()
      {
        new Thread()
          {
            @Override
            public void run()
              {
                log.info("prepareShootModeRadioButtons(): exec.");

                try
                  {
                    final JSONObject replyJson = cameraApi.getAvailableShootMode().getJsonObject();
                    final JSONArray resultsObj = replyJson.getJSONArray("result");
                    final String currentMode = resultsObj.getString(0);
                    final JSONArray availableModesJson = resultsObj.getJSONArray(1);
                    final ArrayList<String> availableModes = new ArrayList<String>();

                    for (int i = 0; i < availableModesJson.length(); i++)
                      {
                        final String mode = availableModesJson.getString(i);

                        if (!"still".equals(mode) && !"movie".equals(mode))
                          {
                            continue;
                          }

                        availableModes.add(mode);
                      }

                    view.setShootModeControl(availableModes, currentMode);
                  }
                catch (IOException e)
                  {
                    log.warn("prepareShootModeRadioButtons: IOException: ", e);
                  }
                catch (JSONException e)
                  {
                    log.warn("prepareShootModeRadioButtons: JSON format error.");
                  }
              };
          }.start();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Check if the indicated API is available at present.
    private boolean isApiAvailable(String apiName)
      {
        return availableApis.contains(apiName);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Check if the version of the server is supported in this application.
    private boolean isSupportedServerVersion(JSONObject replyJson) {
        try {
            JSONArray resultArrayJson = replyJson.getJSONArray("result");
            String version = resultArrayJson.getString(1);
            String[] separated = version.split("\\.");
            int major = Integer.valueOf(separated[0]);
            if (2 <= major) {
                return true;
            }
        } catch (JSONException e) {
            log.warn("isSupportedServerVersion: JSON format error.");
        } catch (NumberFormatException e) {
            log.warn("isSupportedServerVersion: Number format error.");
        }
        return false;
    }
  }
