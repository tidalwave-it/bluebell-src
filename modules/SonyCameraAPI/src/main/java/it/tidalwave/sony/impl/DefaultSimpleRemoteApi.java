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

import javax.annotation.Nonnull;
import java.util.List;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import it.tidalwave.bluebell.mobile.utils.DefaultHttpClient;
import it.tidalwave.bluebell.mobile.utils.HttpClient;
import it.tidalwave.sony.ServerDevice.ApiService;
import it.tidalwave.sony.ServerDevice;
import it.tidalwave.sony.SimpleRemoteApi;

/***********************************************************************************************************************
 *
 * Simple Camera Remote API wrapper class. (JSON based API <--> Java API)
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultSimpleRemoteApi implements SimpleRemoteApi
  {
    private static final String TAG = DefaultSimpleRemoteApi.class.getSimpleName();

    // If you'd like to suppress detailed log output, change this value into
    // false.
    private static final boolean FULL_LOG = true;

    // API server device you want to send requests.
    private ServerDevice mTargetServer;

    // Request ID of API calling. This will be counted up by each API calling.
    private int mRequestId;
    
    private final HttpClient httpClient = new DefaultHttpClient();

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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject().put("method",  "getAvailableApiList")
                                                     .put("params",  new JSONArray()).put("id", id())
                                                     .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url, requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          } 
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject().put("method", "getApplicationInfo")
                                                     .put("params", new JSONArray()).put("id", id())
                                                     .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,  requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          } 
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "getShootMode")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url, requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          } 
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "setShootMode")
                    .put("params", new JSONArray().put(shootMode))
                    .put("id", id()).put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url, requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e)
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "getAvailableShootMode")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url, requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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
        String service = "camera";
      
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "getSupportedShootMode")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e)
          {
            throw new IOException(e);
          }
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
        String service = "camera";
      
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "startLiveview")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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
        String service = "camera";
      
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "stopLiveview")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e)
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "startRecMode")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "stopRecMode")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e)
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "actTakePicture")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          } 
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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
        String service = "camera";
        
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "startMovieRec")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          }
        catch (JSONException e)
          {
            throw new IOException(e);
          }
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
        String service = "camera";
      
        try 
          {
            JSONObject requestJson = new JSONObject()
                    .put("method", "stopMovieRec")
                    .put("params", new JSONArray()).put("id", id())
                    .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString());
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          } 
        catch (JSONException e)
          {
            throw new IOException(e);
          }
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public JSONObject getEvent (final boolean longPollingFlag) 
      throws IOException 
      {
        String service = "camera";
      
        try 
          {
            JSONObject requestJson = new JSONObject().put("method", "getEvent")
                    .put("params", new JSONArray().put(longPollingFlag))
                    .put("id", id()).put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;
            int longPollingTimeout = (longPollingFlag) ? 20000 : 8000; // msec

            log("Request:  " + requestJson.toString());
            String responseJson = httpClient.post(url,
                    requestJson.toString(), longPollingTimeout);
            log("Response: " + responseJson);
            return new JSONObject(responseJson);
          } 
        catch (JSONException e) 
          {
            throw new IOException(e);
          }
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

    // Request ID. Counted up after calling.
    private int id() 
      {
        return mRequestId++;
      }

    // Output a log line.
    private void log (final @Nonnull String msg)
      {
        if (FULL_LOG) 
          {
            Log.d(TAG, msg);
          }
      }
  }
