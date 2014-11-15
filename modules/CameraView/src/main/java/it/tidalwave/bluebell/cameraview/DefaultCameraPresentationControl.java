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
import it.tidalwave.sony.CameraDescriptor;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.bluebell.liveview.DefaultLiveViewPresentationControl;
import it.tidalwave.bluebell.liveview.LiveViewPresentation;
import it.tidalwave.bluebell.liveview.LiveViewPresentationControl;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.sony.CameraApi.*;
import it.tidalwave.sony.CameraDevice;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultCameraPresentationControl implements CameraPresentationControl
  {
    @Nonnull
    private final CameraPresentation presentation;

    @Nonnull
    private final CameraApi cameraApi;

    @Nonnull
    private final CameraObserver cameraObserver;

    @Nonnull
    private final LiveViewPresentationControl liveViewPresentationControl;

    private final Set<String> availableApis = Collections.synchronizedSet(new TreeSet<String>());

    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param presentation              the controlled presentation
     * @param liveViewPresentation      the controller of the live view
     * @param cameraDescriptor          the current camera
     * 
     ******************************************************************************************************************/
    public DefaultCameraPresentationControl (final @Nonnull CameraPresentation presentation,
                                             final @Nonnull LiveViewPresentation liveViewPresentation,
                                             final @Nonnull CameraDescriptor cameraDescriptor)
      {
        this.presentation = presentation;
        final CameraDevice cameraDevice = cameraDescriptor.createDevice();
        cameraApi = cameraDevice.getApi();
        cameraObserver = cameraDevice.getObserver();
        liveViewPresentationControl = new DefaultLiveViewPresentationControl(cameraApi, liveViewPresentation);
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
                            presentation.notifyDeviceNotSupportedAndQuit();
                            return;
                          }
                      }
                    else // never happens
                      {
                        presentation.notifyDeviceNotSupportedAndQuit();
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
                        liveViewPresentationControl.start();
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
                    presentation.notifyConnectionError();
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
                liveViewPresentationControl.stop();
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
                if (!liveViewPresentationControl.isRunning())
                  {
                    presentation.notifyErrorWhileTakingPhoto();
                    return;
                  }

                try
                  {
                    presentation.showProgressBar();
                    presentation.showPhoto(loadPicture(cameraApi.actTakePicture().getImageUrl()));
                  }
                catch (IOException e)
                  {
                    log.warn("IOException while closing slicer: ", e);
                    presentation.notifyErrorWhileTakingPhoto();
                  }
                finally
                  {
                    presentation.hideProgressBar();
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
                    presentation.notifyGenericError();
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
                        presentation.notifyRecStart();
                      }
                    else if (CAMERA_STATUS_MOVIE_RECORDING.equals(cameraStatus))
                      {
                        log.info("stopMovieRec: exec.");
                        cameraApi.stopMovieRec().getThumbnailUrl();
                        presentation.notifyRecStop();
                      }
                  }
                catch (IOException e)
                  {
                    log.warn("startOrStopMovieRecording()", e);
                    presentation.notifyErrorWhileRecordingMovie();
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

        presentation.renderCameraStatus(cameraStatus);

        if (CAMERA_STATUS_MOVIE_RECORDING.equals(cameraStatus))
          {
            presentation.renderRecStartStopButtonAsStop();
          }
        else if (CAMERA_STATUS_IDLE.equals(cameraStatus) && SHOOT_MODE_MOVIE.equals(shootMode))
          {
            presentation.renderRecStartStopButtonAsStart();
          }
        else
          {
            presentation.disableRecStartStopButton();
          }

        presentation.enableTakePhotoButton(SHOOT_MODE_STILL.equals(shootMode) && CAMERA_STATUS_IDLE.equals(cameraStatus));

        if (!SHOOT_MODE_STILL.equals(shootMode))
          {
            presentation.hidePhotoBox();
          }

        if (CAMERA_STATUS_IDLE.equals(cameraStatus))
          {
            presentation.enableShootModeSelector(shootMode);
          }
        else
          {
            presentation.disableShootModeSelector();
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
            final List<String> availableModes = new ArrayList<>(response.getModes());
            availableModes.retainAll(Arrays.asList(SHOOT_MODE_MOVIE, SHOOT_MODE_STILL));
            presentation.setShootModeControl(availableModes, currentMode);
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
