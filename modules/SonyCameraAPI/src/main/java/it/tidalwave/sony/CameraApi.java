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
     *
     ******************************************************************************************************************/
    public static interface RecModeResponse extends Response
      {
      }

    /*******************************************************************************************************************
     *
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
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static interface AvailableApisResponse extends Response
      {
        @Nonnull
        public Set<String> getApis();
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static interface ApplicationInfoResponse extends Response
      {
        @Nonnegative
        public int getVersion();
      }

    /*******************************************************************************************************************
     *
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
     *
     ******************************************************************************************************************/
    public static interface TakePictureResponse extends Response
      {
        @Nonnull
        public URL getImageUrl();
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static interface StopMovieRecResponse extends Response
      {
        @Nonnull
        public URL getThumbnailUrl();
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static interface StartLiveViewUrlResponse extends Response
      {
        @Nonnull
        public URL getUrl();
      }

    /*******************************************************************************************************************
     *
     * Calls actTakePicture API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "actTakePicture",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public TakePictureResponse actTakePicture()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls getApplicationInfo API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "getApplicationInfo",
     *   "params": [""],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public ApplicationInfoResponse getApplicationInfo()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls getAvailableApiList API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "getAvailableApiList",
     *   "params": [""],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public AvailableApisResponse getAvailableApiList()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls getAvailableShootMode API to the target server. Request JSON data
     * is such like as below.
     *
     * <pre>
     * {
     *   "method": "getAvailableShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public AvailableShootModeResponse getAvailableShootMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls getEvent API to the target server. Request JSON data is such like
     * as below.
     *
     * <pre>
     * {
     *   "method": "getEvent",
     *   "params": [true],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param longPollingFlag true means long polling request.
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public EventResponse getEvent (final Polling polling)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls getShootMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "getShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response getShootMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls getSupportedShootMode API to the target server. Request JSON data
     * is such like as below.
     *
     * <pre>
     * {
     *   "method": "getSupportedShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response getSupportedShootMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls setShootMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "setShootMode",
     *   "params": ["still"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param shootMode shoot mode (ex. "still")
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response setShootMode (@Nonnull String shootMode)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls startLiveview API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "startLiveview",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public StartLiveViewUrlResponse startLiveview()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls startMovieRec API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "startMovieRec",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response startMovieRec()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls startRecMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "startRecMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public RecModeResponse startRecMode()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls stopLiveview API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "stopLiveview",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public Response stopLiveview()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls stopMovieRec API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "stopMovieRec",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public StopMovieRecResponse stopMovieRec()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Calls stopRecMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "stopRecMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     *
     ******************************************************************************************************************/
    @Nonnull
    public RecModeResponse stopRecMode()
      throws IOException;
  }
