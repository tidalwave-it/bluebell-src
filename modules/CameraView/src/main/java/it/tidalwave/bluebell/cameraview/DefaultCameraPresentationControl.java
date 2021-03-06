/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
 * %%
 * Copyright (C) 2013 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.concurrent.ExecutorService;
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
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.CameraObserver.Property;
import it.tidalwave.bluebell.liveview.DefaultLiveViewPresentationControl;
import it.tidalwave.bluebell.liveview.LiveViewPresentation;
import it.tidalwave.bluebell.liveview.LiveViewPresentationControl;
import it.tidalwave.bluebell.cameraview.CameraPresentation.EditCallback;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.sony.CameraApi.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public abstract class DefaultCameraPresentationControl implements CameraPresentationControl
  {
    /** The presentation managed by this controller. */
    @Nonnull
    private final CameraPresentation presentation;

    /** The API of the current camera. */
    @Nonnull
    private final CameraApi cameraApi;

    /** An observer of the current camera. */
    @Nonnull
    private final CameraObserver cameraObserver;

    /** The controller for the live view. */
    @Nonnull
    private final LiveViewPresentationControl liveViewPresentationControl;

    /** The set of available APIs. */
    private final Set<String> availableApis = Collections.synchronizedSet(new TreeSet<String>());
    
    /** To run background jobs. */
    @Nonnull
    private final ExecutorService executorService;
    
    /*******************************************************************************************************************
     *
     * The listener of changes in the camera.
     * 
     ******************************************************************************************************************/
    private final CameraObserver.ChangeListener cameraListener = new CameraObserver.ChangeListener()
      {
        @Override
        public void onPropertyChanged (final @Nonnull Property property, final @Nonnull String value)
          {
            log.info("onPropertyChanged({}, {})", property, value);

            switch (property)
              {
                case SHOOT_MODE:
                case CAMERA_STATUS:
                    refreshUi();
                    break;

                default:
                    refreshUiProperty(property);
                    break;
              }
          }

        @Override
        public void onApisChanged (final @Nonnull Set<String> apis,
                                   final @Nonnull Set<String> addedApis, 
                                   final @Nonnull Set<String> removedApis)
          {
            log.info("APIs changed: all: {} added: {} removed: {}", apis, addedApis, removedApis);
            setAvailableApis(apis, addedApis, removedApis);
          }
      };

    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param   presentation            the controlled presentation
     * @param   liveViewPresentation    the controller of the live view
     * @param   cameraDescriptor        the current camera
     * @param   executorService         an {@link ExecutorService} for running background jobs
     * 
     ******************************************************************************************************************/
    public DefaultCameraPresentationControl (final @Nonnull CameraPresentation presentation,
                                             final @Nonnull LiveViewPresentation liveViewPresentation,
                                             final @Nonnull CameraDescriptor cameraDescriptor,
                                             final @Nonnull ExecutorService executorService)
      {
        this.presentation = presentation;
        this.executorService = executorService;
        final CameraDevice cameraDevice = cameraDescriptor.createDevice();
        cameraApi = cameraDevice.getApi();
        cameraObserver = cameraDevice.getObserver();
        liveViewPresentationControl = new DefaultLiveViewPresentationControl(cameraDevice.getApi(), liveViewPresentation);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void start()
      {
        for (final Property property : Property.values())
          {
            presentation.renderProperty(property, "--");
          }
        
        executorService.submit(new Runnable()
          {
            @Override
            public void run()
              {
                log.info("start(): exec.");

                try
                  {
                    final Set<String> apis = cameraApi.getAvailableApiList().getApis();

                    if (apis.contains(API_GET_APPLICATION_INFO))
                      {
                        if (cameraApi.getApplicationInfo().getVersion() < 2)
                          {
                            log.warn("Camera API version < 2, not supported");
                            presentation.notifyDeviceNotSupportedAndQuit();
                            return;
                          }
                      }
                    else // never happens
                      {
                        log.warn("Camera API not found");
                        presentation.notifyDeviceNotSupportedAndQuit();
                        return;
                      }

                    if (apis.contains(API_EVENT)) // FIXME: what if it is not available? CameraObserver will never start
                      {
                        log.info("start(): EventObserver.start()");
                        cameraObserver.setListener(cameraListener);
                        cameraObserver.start();
                      }

                    log.info("start(): completed.");
                  }
                catch (IOException e)
                  {
                    log.warn("start(): IOException: ", e);
                    presentation.notifyConnectionError();
                  }
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        executorService.submit(new Runnable()
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
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void takeAndFetchPicture()
      {
        executorService.submit(new Runnable()
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
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setShootMode (final @Nonnull String mode)
      {
        executorService.submit(new Runnable()
          {
            @Override
            public void run()
              {
                try
                  {
                    cameraApi.setShootMode(mode);
                    // Don't refresh the UI now, the events will do
                  }
                catch (IOException e)
                  {
                    log.warn("setShootMode: IOException: ", e);
                    presentation.notifyGenericError();
                  }
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void startOrStopMovieRecording()
      {
        executorService.submit(new Runnable()
          {
            @Override
            public void run()
              {
                try
                  {
                    final String cameraStatus = cameraObserver.getProperty(Property.CAMERA_STATUS);

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
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void editProperty (final @Nonnull CameraObserver.Property property)
      {
        final String value = cameraObserver.getProperty(property);
        final List<String> feasibleValues = cameraObserver.getPropertyFeasibleValues(property);
        presentation.editProperty(value, feasibleValues, new EditCallback() 
          {
            @Override
            public void setValue (final @Nonnull String value) 
              {
                setProperty(property, value);
              }
          });
      }
    
    /*******************************************************************************************************************
     *
     * Sets a property to the camera.
     * 
     * @param   property    the property
     * @param   value       the value
     *
     ******************************************************************************************************************/
    private void setProperty (final @Nonnull CameraObserver.Property property, final @Nonnull String value)
      {
        executorService.submit(new Runnable()
          {
            @Override
            public void run()
              {
                try 
                  {
                    cameraObserver.setProperty(property, value);
                    presentation.notifyPropertyChanged(property.toString() + "=" + value);
                  } 
                catch (IOException e) 
                  {
                    log.warn("While setting property", e);
                    presentation.notifyErrorWhileSettingProperty();
                  }
              }
          });
      }
    
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected abstract Object loadPicture (final @Nonnull URL url)
      throws IOException;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void refreshUi()
      {
        final String cameraStatus = cameraObserver.getProperty(Property.CAMERA_STATUS);
        final String shootMode = cameraObserver.getProperty(Property.SHOOT_MODE);

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

        presentation.enableTakePhotoButton(SHOOT_MODE_STILL.equals(shootMode)
                                        && CAMERA_STATUS_IDLE.equals(cameraStatus));

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
     * Refresh a camera property on the UI.
     * 
     * @param   property    the property
     *
     ******************************************************************************************************************/
    private void refreshUiProperty (final @Nonnull Property property) 
      {
        final String value = cameraObserver.getProperty(property);
        presentation.renderProperty(property, value);
      }
    
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void setAvailableApis (final @Nonnull Set<String> apis,
                                   final @Nonnull Set<String> addedApis, 
                                   final @Nonnull Set<String> removedApis)
      {
        synchronized (this.availableApis)
          {
            this.availableApis.clear();
            this.availableApis.addAll(apis);
          }
        
        if (addedApis.contains(API_START_REC_MODE))
          {
            try 
              {
                log.info("openConnection(): startRecMode()");
                cameraApi.startRecMode();
              }
            catch (IOException e)
              {
                log.warn("while calling startRecMode()", e);
              }
          }

        if (addedApis.contains(API_START_LIVEVIEW))
          {
            log.info("openConnection(): LiveviewSurface.start()");
            liveViewPresentationControl.start();
          }

        if (addedApis.contains(API_AVAILABLE_SHOOT_MODE))
          {
            log.info("openConnection(): prepareShootModeRadioButtons()");
            prepareShootModeRadioButtons();
          }
        
        // TODO: getTouchAFPosition, setTouchAFPosition
        // TODO: zoom
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
