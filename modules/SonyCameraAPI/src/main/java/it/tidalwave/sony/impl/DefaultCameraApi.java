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
package it.tidalwave.sony.impl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import it.tidalwave.bluebell.net.impl.DefaultHttpClient;
import it.tidalwave.bluebell.net.HttpClient;
import it.tidalwave.sony.CameraDevice.ApiService;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.StatusCode;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Simple Camera Remote API wrapper class. (JSON based API <--> Java API)
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
/* package */ class DefaultCameraApi implements CameraApi
  {
    private static final String CAMERA_SERVICE = "camera";

    // API server device you want to send requests.
    private final CameraDevice mTargetServer;

    // Request ID of API calling. This will be counted up by each API calling.
    private int mRequestId;

    private final HttpClient httpClient = new DefaultHttpClient();

    /*******************************************************************************************************************
     *
     * A generic implementation of {@link Response}, it just wraps the returned {@link JSONObject}.
     *
     ******************************************************************************************************************/
    @ToString
    class GenericResponse implements Response
      {
        @Nonnull @Getter
        protected final JSONObject jsonObject;

        @Nonnull @Getter
        protected /*final*/ StatusCode statusCode;

        public GenericResponse (final @Nonnull JSONObject jsonObject)
          {
            this.jsonObject = jsonObject;
            this.statusCode = StatusCode.OK;
          }

//        @Nonnull
//        public StatusCode getStatusCode()
//          {
//            try
//              {
//                final JSONArray resultsObj = jsonObject.getJSONArray("result");
//                final int code = resultsObj.getInt(0);
//                return StatusCode.findStatusCode(code);
//              }
//            catch (JSONException e)
//              {
//                throw new RuntimeException("malformed JSON", e);
//              }
//          }
      }

    /*******************************************************************************************************************
     *
     * FIXME: merge with GenericResponse once all the error management code has been refactored.
     *
     ******************************************************************************************************************/
    class ErrorCheckingResponse extends GenericResponse
      {
        public ErrorCheckingResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
            try
              {
                if (jsonObject.has("error"))
                  {
                    final JSONArray errorObj = jsonObject.getJSONArray("error");
                    final int code = errorObj.getInt(0);
                    statusCode = StatusCode.findStatusCode(code);
                    throw new CameraApiException(this, errorObj.getString(1), null);
                  }

//                if ((getStatusCode() != StatusCode.OK) && (getStatusCode() != StatusCode.ANY))
//                  {
//                    final JSONArray resultsObj = jsonObject.getJSONArray("result");
//                    throw new CameraApiException(this, resultsObj.getString(1), null);
//                  }
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class DefaultRecModeResponse extends ErrorCheckingResponse implements RecModeResponse
      {
        public DefaultRecModeResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class DefaultEventResponse extends ErrorCheckingResponse implements EventResponse
      {
        public DefaultEventResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
          }

    // Finds and extracts a list of available APIs from reply JSON data.
    // As for getEvent v1.0, results[0] => "availableApiList"
        @Override @Nonnull
        public Set<String> getApis()
          {
            try
              {
                final Set<String> availableApis = new TreeSet<String>();
                int indexOfAvailableApiList = 0;
                final JSONArray resultsObj = jsonObject.getJSONArray("result");

                if (!resultsObj.isNull(indexOfAvailableApiList))
                  {
                    final JSONObject availableApiListObj = resultsObj.getJSONObject(indexOfAvailableApiList);
                    final String type = availableApiListObj.getString("type");

                    if ("availableApiList".equals(type))
                      {
                        JSONArray apiArray = availableApiListObj.getJSONArray("names");

                        for (int i = 0; i < apiArray.length(); i++)
                          {
                            availableApis.add(apiArray.getString(i));
                          }
                      }
                    else
                      {
                        log.warn("Event reply: Illegal Index (0: AvailableApiList) {}", type);
                      }
                  }

                return availableApis;
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }

        @Override @Nonnull
        public String getCameraStatus()
          {
            try
              {
                String cameraStatus = "";
                int indexOfCameraStatus = 1;
                JSONArray resultsObj = jsonObject.getJSONArray("result");

                if (!resultsObj.isNull(indexOfCameraStatus))
                  {
                    JSONObject cameraStatusObj = resultsObj.getJSONObject(indexOfCameraStatus);
                    String type = cameraStatusObj.getString("type");

                    if ("cameraStatus".equals(type))
                      {
                        cameraStatus = cameraStatusObj.getString("cameraStatus");
                      }
                    else
                      {
                        log.warn("Event reply: Illegal Index (1: CameraStatus) ", type);
                      }
                  }

                return cameraStatus;
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }

        @Override @Nonnull
        public String getShootMode()
          {
            try
              {
                String shootMode = "";
                int indexOfShootMode = 21;
                JSONArray resultsObj = jsonObject.getJSONArray("result");

                if (!resultsObj.isNull(indexOfShootMode))
                  {
                    JSONObject shootModeObj = resultsObj.getJSONObject(indexOfShootMode);
                    String type = shootModeObj.getString("type");

                    if ("shootMode".equals(type))
                      {
                        shootMode = shootModeObj.getString("currentShootMode");
                      }
                    else
                      {
                        log.warn("Event reply: Illegal Index (21: ShootMode) ", type);
                      }
                  }

                return shootMode;
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class DefaultAvailableApisResponse extends ErrorCheckingResponse implements AvailableApisResponse
      {
        public DefaultAvailableApisResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
          }

    // Finds and extracts a list of available APIs from reply JSON data.
    // As for getEvent v1.0, results[0] => "availableApiList"
        @Override @Nonnull
        public Set<String> getApis()
          {
            try
              {
                final Set<String> availableApis = new TreeSet<String>();
                final JSONArray resultArrayJson = jsonObject.getJSONArray("result");
                final JSONArray apiListJson = resultArrayJson.getJSONArray(0);

                for (int i = 0; i < apiListJson.length(); i++)
                  {
                    availableApis.add(apiListJson.getString(i));
                  }

                return availableApis;
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class DefaultApplicationInfoResponse extends ErrorCheckingResponse implements ApplicationInfoResponse
      {
        public DefaultApplicationInfoResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
          }

        @Override
        public int getVersion()
          {
            try
              {
                final JSONArray resultArrayJson = jsonObject.getJSONArray("result");
                final String version = resultArrayJson.getString(1);
                final String[] separated = version.split("\\.");
                final int major = Integer.valueOf(separated[0]);
                return major;
              }
            catch (JSONException e)
              {
                log.warn("isSupportedServerVersion: JSON format error.");
              }
            catch (NumberFormatException e)
              {
                log.warn("isSupportedServerVersion: Number format error.");
              }

            return 0;
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class DefaultAvailableShootModeResponse extends ErrorCheckingResponse implements AvailableShootModeResponse
      {
        public DefaultAvailableShootModeResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
          }

        @Override @Nonnull
        public String getCurrentMode()
          {
            try
              {
                final JSONArray resultsObj = jsonObject.getJSONArray("result");
                return resultsObj.getString(0);
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }

        @Override @Nonnull
        public Set<String> getModes()
          {
            try
              {
                final JSONArray resultsObj = jsonObject.getJSONArray("result");
                final JSONArray availableModesJson = resultsObj.getJSONArray(1);
                final Set<String> availableModes = new TreeSet<String>();

                for (int i = 0; i < availableModesJson.length(); i++)
                  {
                    availableModes.add(availableModesJson.getString(i));
                  }

                return availableModes;
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class DefaultTakePictureResponse extends ErrorCheckingResponse implements TakePictureResponse
      {
        @Getter @Nonnull
        private final URL imageUrl;

        public DefaultTakePictureResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
            try
              {
                final JSONArray resultsObj = jsonObject.getJSONArray("result");
                final JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                String urlAsString = null;

                if (imageUrlsObj.length() >= 1)
                  {
                    urlAsString = imageUrlsObj.getString(0);
                  }

                if (urlAsString == null)
                  {
                    throw new CameraApiException(this, "takeAndFetchPicture: post image URL is null.", null);
                  }

                imageUrl = new URL(urlAsString);
              }
            catch (MalformedURLException e)
              {
                throw new RuntimeException("malformed URL", e);
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class DefaultStopMovieRecResponse extends ErrorCheckingResponse implements StopMovieRecResponse
      {
        @Getter @Nonnull
        private final URL thumbnailUrl;

        public DefaultStopMovieRecResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
            try
              {
                final JSONArray resultsObj = jsonObject.getJSONArray("result");
                final String thumbnailUrlAsString = resultsObj.getString(0);

                if (thumbnailUrlAsString == null)
                  {
                    throw new CameraApiException(this, "stopMovieRec: thumbnail URL is null.", null);
                  }

                thumbnailUrl = new URL(thumbnailUrlAsString);
              }
            catch (MalformedURLException e)
              {
                throw new RuntimeException("malformed URL", e);
              }
            catch (JSONException e)
              {
                throw new RuntimeException("malformed JSON", e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    class Call
      {
        private final JSONObject request = new JSONObject();

        private final JSONArray params = new JSONArray();

        private final String url;

        public Call (final @Nonnull String service)
          throws IOException
          {
            try
              {
                url = findActionListUrl(service) + "/" + service;
                request.put("version", "1.0");
                request.put("id", mRequestId++);
              }
            catch (JSONException e)
              {
                throw new IOException(e);
              }
          }

        @Nonnull
        public Call withParam (final @Nonnull Object value)
          throws IOException
          {
            params.put(value);
            return this;
          }

        @Nonnull
        public Call withMethod (final @Nonnull String methodName)
          throws IOException
          {
            try
              {
                request.put("method", methodName);
                return this;
              }
            catch (JSONException e)
              {
                throw new IOException(e);
              }
          }

        @Nonnull
        public JSONObject post()
          throws IOException
          {
            try
              {
                request.put("params", params);
                log.info("Request: {}", request);
                final String response = httpClient.post(url, request.toString());
                log.info("Response: {}", response);

                return new JSONObject(response);
              }
            catch (JSONException e)
              {
                throw new IOException(e);
              }
          }

        @Nonnull
        public JSONObject post (final @Nonnegative int timeout)
          throws IOException
          {
            try
              {
                request.put("params", params);
                log.info("Request: {}", request);
                final String response = httpClient.post(url, request.toString(), timeout);
                log.info("Response: {}", response);

                return new JSONObject(response);
              }
            catch (JSONException e)
              {
                throw new IOException(e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     * Constructor.
     *
     * @param target server device of Remote API
     *
     ******************************************************************************************************************/
    public DefaultCameraApi (final @Nonnull CameraDevice target)
      {
        mTargetServer = target;
        mRequestId = 1;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public AvailableApisResponse getAvailableApiList()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getAvailableApiList");
        return new DefaultAvailableApisResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ApplicationInfoResponse getApplicationInfo()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getApplicationInfo");
        return new DefaultApplicationInfoResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response getShootMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getShootMode");
        return new GenericResponse(call.post());
    }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response setShootMode (final @Nonnull String shootMode)
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("setShootMode")
                                                    .withParam(shootMode);
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public AvailableShootModeResponse getAvailableShootMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getAvailableShootMode");
        return new DefaultAvailableShootModeResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response getSupportedShootMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getSupportedShootMode");
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response startLiveview()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("startLiveview");
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response stopLiveview()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("stopLiveview");
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public RecModeResponse startRecMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("startRecMode");
        return new DefaultRecModeResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public RecModeResponse stopRecMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("stopRecMode");
        return new DefaultRecModeResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public TakePictureResponse actTakePicture()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("actTakePicture");
        return new DefaultTakePictureResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response startMovieRec()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("startMovieRec");
        return new ErrorCheckingResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public StopMovieRecResponse stopMovieRec()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("stopMovieRec");
        return new DefaultStopMovieRecResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public EventResponse getEvent (final boolean longPolling)
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getEvent")
                                                    .withParam(longPolling);
        final int longPollingTimeout = longPolling ? 20000 : 8000; // msec
        return new DefaultEventResponse(call.post(longPollingTimeout));
      }

    @Nonnull
    private Call createCall (final @Nonnull String service)
      throws IOException
      {
        return new Call(service);
      }

    // Retrieves Action List URL from Server information.
    private String findActionListUrl (final @Nonnull String service)
      {
        final List<ApiService> services = mTargetServer.getApiServices();

        for (final ApiService apiService : services)
          {
            if (apiService.getName().equals(service))
              {
                return apiService.getActionListUrl();
              }
          }

        throw new IllegalStateException("actionUrl not found for service: " + service);
      }
  }
