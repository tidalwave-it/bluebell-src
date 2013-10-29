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
import it.tidalwave.sony.ServerDevice.ApiService;
import it.tidalwave.sony.ServerDevice;
import it.tidalwave.sony.SimpleRemoteApi;
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
public class DefaultSimpleRemoteApi implements SimpleRemoteApi
  {
    private static final String CAMERA_SERVICE = "camera";
      
    // API server device you want to send requests.
    private final ServerDevice mTargetServer;

    // Request ID of API calling. This will be counted up by each API calling.
    private int mRequestId;
    
    private final HttpClient httpClient = new DefaultHttpClient();
    
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
    public DefaultSimpleRemoteApi (final @Nonnull ServerDevice target) 
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
    public JSONObject getAvailableApiList()
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("getAvailableApiList");
        return call.post();
    }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject getApplicationInfo()
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("getApplicationInfo");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject getShootMode()
      throws IOException
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("getShootMode");
        return call.post();
    }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject setShootMode (final @Nonnull String shootMode) 
      throws IOException
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("setShootMode")
                                                  .withParam(shootMode);
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject getAvailableShootMode() 
      throws IOException
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("getAvailableShootMode");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject getSupportedShootMode()
      throws IOException
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("getSupportedShootMode");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject startLiveview() 
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("startLiveview");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject stopLiveview() 
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("stopLiveview");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject startRecMode() 
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("startRecMode");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject stopRecMode()
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("stopRecMode");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject actTakePicture()
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("actTakePicture");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject startMovieRec() 
      throws IOException
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("startMovieRec");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject stopMovieRec() 
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("stopMovieRec");
        return call.post();
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject getEvent (final boolean longPolling) 
      throws IOException 
      {
        final Call call = new Call(CAMERA_SERVICE).withMethod("getEvent")
                                                  .withParam(longPolling);
        final int longPollingTimeout = longPolling ? 20000 : 8000; // msec
        return call.post(longPollingTimeout);
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
        
        throw new IllegalStateException("actionUrl not found.");
      }
  }
