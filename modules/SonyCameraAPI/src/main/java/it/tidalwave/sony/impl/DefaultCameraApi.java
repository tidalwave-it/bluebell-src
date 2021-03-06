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
package it.tidalwave.sony.impl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraApiService;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.StatusCode;
import it.tidalwave.bluebell.net.impl.DefaultHttpClient;
import it.tidalwave.bluebell.net.HttpClient;
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

    private final CameraDevice cameraService;

    // Request ID of API calling. This will be counted up by each API calling.
    private int requestId;

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
        protected final JSONObject responseJson;

        @Nonnull @Getter
        protected /*final*/ StatusCode statusCode;

        public GenericResponse (final @Nonnull JSONObject jsonObject)
          {
            this.responseJson = jsonObject;
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
                final JSONArray resultsObj = responseJson.getJSONArray("result");

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
        public String getProperty (final @Nonnull Property property)
          {
            return getValue(property.getName(), property.getIndex(), property.getType());
          }
        
        @Nonnull
        private String getValue (final @Nonnull String field, 
                                 final @Nonnegative int index, 
                                 final @Nonnull String expectedType) 
          {
            try
              {
                final JSONArray resultObject = responseJson.getJSONArray("result");

                if (!resultObject.isNull(index))
                  {
                    final JSONObject valueObject = resultObject.getJSONObject(index);
                    final String type = valueObject.getString("type");

                    if (expectedType.equals(type))
                      {
                        return valueObject.getString(field);
                      }
                    else
                      {
                        log.warn("Event reply: Illegal Index ({}: {} - {}) ", index, field, type);
                      }
                  }

                return "";
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
                final JSONArray resultArrayJson = responseJson.getJSONArray("result");
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
                final JSONArray resultArrayJson = responseJson.getJSONArray("result");
                final String version = resultArrayJson.getString(1);
                final String[] separated = version.split("\\.");
                final int major = Integer.valueOf(separated[0]);
                return major;
              }
            catch (JSONException e)
              {
                log.warn("in getVersion()", e);
              }
            catch (NumberFormatException e)
              {
                log.warn("in getVersion()", e);
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
                final JSONArray resultsObj = responseJson.getJSONArray("result");
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
                final JSONArray resultsObj = responseJson.getJSONArray("result");
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
    class DefaultStartLiveViewUrlResponse extends ErrorCheckingResponse implements StartLiveViewUrlResponse
      {
        @Getter @Nonnull
        private final URL url;

        public DefaultStartLiveViewUrlResponse (final @Nonnull JSONObject jsonObject)
          throws CameraApiException
          {
            super(jsonObject);
            try
              {
                final JSONArray resultsObj = responseJson.getJSONArray("result");
                String urlAsString = null;

                if (resultsObj.length() >= 1)
                  {
                    urlAsString = resultsObj.getString(0);
                  }

                if (urlAsString == null)
                  {
                    throw new CameraApiException(this, "liveView URL is null", null);
                  }

                url = new URL(urlAsString);
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

        private String version = "1.0";
                
        public Call (final @Nonnull String service)
          throws IOException
          {
            try
              {
                url = findActionListUrl(service) + "/" + service;
                request.put("id", requestId++);
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
        public Call withVersion (final @Nonnull String version)
          throws IOException
          {
            this.version = version;
            return this;
          }

        @Nonnull
        public JSONObject post()
          throws IOException
          {
            return post(0);
          }

        @Nonnull
        public JSONObject post (final @Nonnegative int timeout)
          throws IOException
          {
            try
              {
                request.put("version", version);
                request.put("params", params);

                log.debug("Request: {}", request);
                final long baseTime = System.currentTimeMillis();
                final String response = (timeout > 0) ? httpClient.post(url, request.toString(), timeout)
                                                      : httpClient.post(url, request.toString());
                log.debug("Response in {} msec: {}", System.currentTimeMillis() - baseTime, response);

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
     * @param cameraDevice server device of Remote API
     *
     ******************************************************************************************************************/
    public DefaultCameraApi (final @Nonnull CameraDevice cameraService)
      {
        this.cameraService = cameraService;
        requestId = 1;
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
        return new DefaultAvailableApisResponse(call(CAMERA_SERVICE).withMethod("getAvailableApiList").post());
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
        return new DefaultApplicationInfoResponse(call(CAMERA_SERVICE).withMethod("getApplicationInfo").post());
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
        return new GenericResponse(call(CAMERA_SERVICE).withMethod("getShootMode").post());
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
        return new ErrorCheckingResponse(call(CAMERA_SERVICE).withMethod("setShootMode")
                                                             .withParam(shootMode)
                                                             .post());
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
        return new DefaultAvailableShootModeResponse(call(CAMERA_SERVICE).withMethod("getAvailableShootMode").post());
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
        return new GenericResponse(call(CAMERA_SERVICE).withMethod("getSupportedShootMode").post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public StartLiveViewUrlResponse startLiveview()
      throws IOException
      {
        return new DefaultStartLiveViewUrlResponse(call(CAMERA_SERVICE).withMethod("startLiveview").post());
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
        return new GenericResponse(call(CAMERA_SERVICE).withMethod("stopLiveview").post());
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
        return new DefaultRecModeResponse(call(CAMERA_SERVICE).withMethod("startRecMode").post());
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
        return new DefaultRecModeResponse(call(CAMERA_SERVICE).withMethod("stopRecMode").post());
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
        return new DefaultTakePictureResponse(call(CAMERA_SERVICE).withMethod("actTakePicture").post());
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
        return new ErrorCheckingResponse(call(CAMERA_SERVICE).withMethod("startMovieRec").post());
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
        return new DefaultStopMovieRecResponse(call(CAMERA_SERVICE).withMethod("stopMovieRec").post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public ErrorCheckingResponse setProperty (final @Nonnull Property property, final @Nonnull String value) 
      throws IOException
      {
        log.info("setProperty({}, {})", property, value);
        final String setterMethodName = property.getSetterMethodName();
        
        if (setterMethodName == null)
          {
            throw new IllegalArgumentException("Setting property not supported: " + property);  
          }
          
        return new ErrorCheckingResponse(call(CAMERA_SERVICE).withMethod(setterMethodName).withParam(value).post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public EventResponse getEvent (final @Nonnull Polling polling)
      throws IOException
      {
        // TODO: call v1.1 or v1.2 when available
        final String version = "1.0";
        return new DefaultEventResponse(call(CAMERA_SERVICE).withMethod("getEvent")
                                                            .withParam(polling == Polling.LONG_POLLING)
                                                            .withVersion(version)
                                                            .post(polling.getTimeout()));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Call call (final @Nonnull String service)
      throws IOException
      {
        return new Call(service);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String findActionListUrl (final @Nonnull String service)
      {
        final List<CameraApiService> services = cameraService.getApiServices();

        for (final CameraApiService apiService : services)
          {
            if (apiService.getName().equals(service))
              {
                return apiService.getActionListUrl();
              }
          }

        throw new IllegalStateException("actionUrl not found for service: " + service);
      }
  }
