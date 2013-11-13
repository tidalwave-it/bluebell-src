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
package it.tidalwave.bluebell.cameraview;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import java.net.URL;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.bluebell.liveview.DefaultLiveViewControl;
import it.tidalwave.bluebell.liveview.LiveView;
import it.tidalwave.bluebell.liveview.LiveViewControl;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.sony.CameraApi.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultCameraViewControl implements CameraViewControl
  {
    @Nonnull
    private final CameraView view;

    @Nonnull
    private final CameraApi cameraApi;

    private CameraObserver cameraObserver;

    private LiveViewControl liveViewControl;

    private final Set<String> availableApis = Collections.synchronizedSet(new TreeSet<String>());

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public DefaultCameraViewControl (final @Nonnull CameraView view,
                                     final @Nonnull LiveView liveView,
                                     final @Nonnull CameraDevice cameraDevice)
      {
        this.view = view;
        cameraApi = cameraDevice.getApi();
        cameraObserver = cameraDevice.getObserver();
        liveViewControl = new DefaultLiveViewControl(cameraApi, liveView);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    // Open connection to the camera device to start monitoring Camera events
    // and showing liveview.
    @Override
    public void start() 
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

                    if (isApiAvailable(API_GET_APPLICATION_INFO))
                      {
                        log.info("openConnection(): getApplicationInfo()");

                        if (cameraApi.getApplicationInfo().getVersion() < 2)
                          {
                            view.notifyDeviceNotSupportedAndQuit();
                            return;
                          }
                      }
                    else // never happens
                      {
                        view.notifyDeviceNotSupportedAndQuit();
                        return;
                      }

                    if (isApiAvailable(API_START_REC_MODE))
                      {
                        log.info("openConnection(): startRecMode()");
                        cameraApi.startRecMode();
                        setAvailableApis(cameraApi.getAvailableApiList().getApis());
                      }

                    if (isApiAvailable(API_EVENT))
                      {
                        log.info("openConnection(): EventObserver.start()");
                        cameraObserver.start();
                      }

                    if (isApiAvailable(API_START_LIVEVIEW))
                      {
                        log.info("openConnection(): LiveviewSurface.start()");
                        liveViewControl.start();
                      }

                    if (isApiAvailable(API_AVAILABLE_SHOOT_MODE))
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
                log.info("stop()");
                liveViewControl.stop();
                cameraObserver.stop();

                if (isApiAvailable(API_STOP_REC_MODE))
                  {
                    try
                      {
                        cameraApi.stopRecMode();
                        log.debug(">>>> stop() completed");
                      }
                    catch (IOException e)
                      {
                        log.warn("While stopping", e);
                      }
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
        new Thread()
          {
            @Override
            public void run()
              {
                if (!liveViewControl.isRunning())
                  {
                    view.notifyErrorWhileTakingPhoto();
                    return;
                  }

                try
                  {
                    view.showProgressBar();
                    view.showPhoto(loadPicture(cameraApi.actTakePicture().getImageUrl()));
                  }
                catch (IOException e)
                  {
                    log.warn("IOException while closing slicer: ", e);
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
    public void setShootMode (final @Nonnull String mode)
      {
        new Thread()
          {
            @Override
            public void run()
              {
                try
                  {
                    cameraApi.setShootMode(mode);
                    // Don't refresh the UI now, the events will
                  }
                catch (IOException e)
                  {
                    log.warn("setShootMode: IOException: ", e);
                    view.notifyGenericError();
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
    public void startOrStopMovieRecording()
      {
        new Thread()
          {
            @Override
            public void run()
              {
                try
                  {
                    final String cameraStatus = cameraObserver.getStatus();

                    if (CAMERA_STATUS_IDLE.equals(cameraStatus))
                      {
                        log.info("startMovieRec: exec.");
                        cameraApi.startMovieRec();
                        view.notifyRecStart();
                      }
                    else if (CAMERA_STATUS_MOVIE_RECORDING.equals(cameraStatus))
                      {
                        log.info("stopMovieRec: exec.");
                        cameraApi.stopMovieRec().getThumbnailUrl();
                        view.notifyRecStop();
                      }
                  }
                catch (IOException e)
                  {
                    log.warn("startOrStopMovieRecording()", e);
                    view.notifyErrorWhileRecordingMovie();
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
        return null; // to be overridden
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

        if (CAMERA_STATUS_MOVIE_RECORDING.equals(cameraStatus))
          {
            view.renderRecStartStopButtonAsStop();
          }
        else if (CAMERA_STATUS_IDLE.equals(cameraStatus) && SHOOT_MODE_MOVIE.equals(shootMode))
          {
            view.renderRecStartStopButtonAsStart();
          }
        else
          {
            view.disableRecStartStopButton();
          }

        view.enableTakePhotoButton(SHOOT_MODE_STILL.equals(shootMode) && CAMERA_STATUS_IDLE.equals(cameraStatus));

        if (!SHOOT_MODE_STILL.equals(shootMode))
          {
            view.hidePhotoBox();
          }

        if (CAMERA_STATUS_IDLE.equals(cameraStatus))
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
        log.info("prepareShootModeRadioButtons()");

        try
          {
            final AvailableShootModeResponse response = cameraApi.getAvailableShootMode();
            final String currentMode = response.getCurrentMode();
            final List<String> availableModes = new ArrayList<String>(response.getModes());
            availableModes.retainAll(Arrays.asList(SHOOT_MODE_MOVIE, SHOOT_MODE_STILL));
            view.setShootModeControl(availableModes, currentMode);
          }
        catch (IOException e)
          {
            log.warn("prepareShootModeRadioButtons(): ", e);
          }
      }

    /*******************************************************************************************************************
     *
     * Check whether a given API is currently available.
     *
     ******************************************************************************************************************/
    private boolean isApiAvailable (final @Nonnull String apiName)
      {
        return availableApis.contains(apiName);
      }
  }
