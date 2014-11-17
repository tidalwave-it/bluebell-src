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
package it.tidalwave.sony;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Set;
import java.io.IOException;
import java.net.URL;
import org.json.JSONObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * This class exposes methods that allow to query and control the device.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraApi
  {
    public static final String API_GET_APPLICATION_INFO = "getApplicationInfo";
    public static final String API_START_REC_MODE = "startRecMode";
    public static final String API_STOP_REC_MODE = "stopRecMode";
    public static final String API_EVENT = "getEvent";
    public static final String API_START_LIVEVIEW = "startLiveview";
    public static final String API_AVAILABLE_SHOOT_MODE = "getAvailableShootMode";

    public static final String SHOOT_MODE_STILL = "still";
    public static final String SHOOT_MODE_MOVIE = "movie";

    public static final String CAMERA_STATUS_IDLE = "IDLE";
    public static final String CAMERA_STATUS_MOVIE_RECORDING = "MovieRecording";

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor @Getter
    public static enum Property
      {
        // TODO: remove internal API bindings from public visibility
        EXPOSURE_COMPENSATION("currentExposureCompensation", 25, "exposureCompensation", "setExposureCompensation"),
        FLASH_MODE(           "currentFlashMode",            26, "flashMode",            "setFlashMode"),
        F_NUMBER(             "currentFNumber",              27, "fNumber",              "setFNumber"),
        FOCUS_MODE(           "currentFocusMode",            28, "focusMode",            "setFocusMode"),
        ISO_SPEED_RATE(       "currentIsoSpeedRate",         29, "isoSpeedRate",         "setIsoSpeedRate"),
        SHUTTER_SPEED(        "currentShutterSpeed",         32, "shutterSpeed",         "setShutterSpeed"),
        WHITE_BALANCE(        "currentWhiteBalanceMode",     33, "whiteBalance",         "setWhiteBalance");
        
        @Nonnull
        private final String name;
        
        private final int index;
        
        @Nonnull
        private final String type;
        
        @CheckForNull
        private final String setterMethodName;
      }
        
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor
    public static enum Polling
      {
        SHORT_POLLING(8000),
        LONG_POLLING(20000);

        @Getter
        private final int timeout;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static class CameraApiException extends IOException
      {
        @Getter @Nonnull
        private final Response response;

        public CameraApiException (final @Nonnull Response response,
                                   final @Nonnull String message,
                                   final @Nonnull Throwable cause)
          {
            super(message, cause);
            this.response = response;
          }

        @Nonnull
        public StatusCode getStatusCode()
          {
            return response.getStatusCode();
          }
      }

    /*******************************************************************************************************************
     *
     * This interface declares the common methods available to all kinds of responses.
     *
     ******************************************************************************************************************/
    public static interface Response
      {
        @Nonnull
        public JSONObject getResponseJson();

        @Nonnull
        public StatusCode getStatusCode();
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'RecMode' operation response.
     *
     ******************************************************************************************************************/
    public static interface RecModeResponse extends Response
      {
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'Event' response.
     *
     ******************************************************************************************************************/
    public static interface EventResponse extends Response
      {
        @Nonnull
        public Set<String> getApis();

        @Nonnull
        public String getShootMode();

        @Nonnull
        public String getCameraStatus();
        
        @Nonnull
        public String getProperty (@Nonnull Property property);
        
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'AvailableApisResponse' response.
     *
     ******************************************************************************************************************/
    public static interface AvailableApisResponse extends Response
      {
        @Nonnull
        public Set<String> getApis();
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'ApplicationInfo' response.
     *
     ******************************************************************************************************************/
    public static interface ApplicationInfoResponse extends Response
      {
        @Nonnegative
        public int getVersion();
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'AvailableShootMode' response.
     *
     ******************************************************************************************************************/
    public static interface AvailableShootModeResponse extends Response
      {
        @Nonnull
        public String getCurrentMode();

        @Nonnull
        public Set<String> getModes();
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'TakePicture' response.
     *
     ******************************************************************************************************************/
    public static interface TakePictureResponse extends Response
      {
        @Nonnull
        public URL getImageUrl();
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'StopMovieRec' response.
     *
     ******************************************************************************************************************/
    public static interface StopMovieRecResponse extends Response
      {
        @Nonnull
        public URL getThumbnailUrl();
      }

    /*******************************************************************************************************************
     *
     * This interface declares the additional methods available to the 'StartLiveView' response.
     *
     ******************************************************************************************************************/
    public static interface StartLiveViewUrlResponse extends Response
      {
        @Nonnull
        public URL getUrl();
      }

    /*******************************************************************************************************************
     *
     * Takes a picture.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public TakePictureResponse actTakePicture()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Retrieves the application info.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public ApplicationInfoResponse getApplicationInfo()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Retrieves the available APIs.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public AvailableApisResponse getAvailableApiList()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Retrieves the available shoot modes.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public AvailableShootModeResponse getAvailableShootMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Polls for the next update event from the camera.
     * 
     * @param   polling         long or short polling
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public EventResponse getEvent (final Polling polling)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Retrieves the current shoot mode.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response getShootMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Retrieves the supported shoot modes.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response getSupportedShootMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Sets the shoot mode.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response setShootMode (@Nonnull String shootMode)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Starts the live view.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public StartLiveViewUrlResponse startLiveview()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Stops the live view.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response stopLiveview()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Starts movie recording.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response startMovieRec()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Starts movie recording.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public StopMovieRecResponse stopMovieRec()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Starts rec mode.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public RecModeResponse startRecMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Stops rec mode.
     * 
     * @return                  a response object with available information
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public RecModeResponse stopRecMode()
      throws IOException;
    
    /*******************************************************************************************************************
     *
     * Sets a property to the camera.
     * 
     * @param   property    the property
     * @param   value       the value
     * @return                  a response object with available information
     * @throws  IOException in case of error
     *
     ******************************************************************************************************************/
    public Response setProperty (@Nonnull Property property, @Nonnull String value)
      throws IOException;
  }
