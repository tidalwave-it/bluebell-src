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

import javax.annotation.Nonnull;
import java.util.Set;
import java.io.IOException;
import org.json.JSONObject;
import lombok.Getter;

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
        public JSONObject getJsonObject();

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
        public int getVersion();
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
    public Response actTakePicture()
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
    public Response getAvailableShootMode()
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
    public EventResponse getEvent (boolean longPollingFlag)
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
    public Response startLiveview()
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
    public Response stopMovieRec()
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
