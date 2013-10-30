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
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import it.tidalwave.bluebell.net.impl.DefaultHttpClient;
import it.tidalwave.bluebell.net.HttpClient;
import it.tidalwave.sony.CameraDevice.ApiService;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraApi;
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
public class DefaultCameraApi implements CameraApi
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
        private final JSONObject jsonObject;

        public GenericResponse (final @Nonnull JSONObject jsonObject)
          {
            this.jsonObject = jsonObject;
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
    public Response getAvailableApiList()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getAvailableApiList");
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response getApplicationInfo()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getApplicationInfo");
        return new GenericResponse(call.post());
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
    public Response getAvailableShootMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getAvailableShootMode");
        return new GenericResponse(call.post());
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
    public Response startRecMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("startRecMode");
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response stopRecMode()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("stopRecMode");
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response actTakePicture()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("actTakePicture");
        return new GenericResponse(call.post());
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
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response stopMovieRec()
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("stopMovieRec");
        return new GenericResponse(call.post());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Response getEvent (final boolean longPolling)
      throws IOException
      {
        final Call call = createCall(CAMERA_SERVICE).withMethod("getEvent")
                                                  .withParam(longPolling);
        final int longPollingTimeout = longPolling ? 20000 : 8000; // msec
        return new GenericResponse(call.post(longPollingTimeout));
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
