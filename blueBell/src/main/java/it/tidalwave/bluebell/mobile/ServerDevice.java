/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import android.util.Log;
import it.tidalwave.bluebell.mobile.utils.DefaultSimpleHttpClient;
import it.tidalwave.bluebell.mobile.utils.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A server device description class.
 */
public class ServerDevice 
  {
    private static final String TAG = ServerDevice.class.getSimpleName();
    
//    private final SimpleHttpClient httpClient = new DefaultSimpleHttpClient();

    /**
     * Camera Remote API service (category). For example, "camera", "guide" and
     * so on. "Action List URL" is API request target URL of each service.
     */
    @AllArgsConstructor
    public static class ApiService 
      {
        @Getter @Setter @Nonnull
        private String name;

        @Getter @Setter @Nonnull
        private String actionListUrl;

        /**
         * Returns the endpoint URL of the category.
         * 
         * @return endpoint URL
         */
        @Nonnull
        public String getEndpointUrl()
          {
            String url = null;
            
            if (actionListUrl == null || name == null) 
              {
                url = null;
              } 
            else if (actionListUrl.endsWith("/")) 
              {
                url = actionListUrl + name;
              }
            else 
              {
                url = actionListUrl + "/" + name;
              }
            
            return url;
          }
      }

    @Getter
    private String ddUrl;
    
    @Getter
    private String friendlyName;
    
    @Getter
    private String modelName;
    
    @Getter
    private String udn;
    
    @Getter
    private String iconUrl;
    
    private final List<ApiService> apiServices = new ArrayList<ServerDevice.ApiService>();

    /**
     * Returns IP address of the DD.
     * 
     * @return
     */
    @Nullable
    public String getIpAddres()
      {
        String ip = null;
        
        if (ddUrl != null) 
          {
            return toHost(ddUrl);
          }
        
        return ip;
      }

    /**
     * Returns a list of categories that the server supports.
     * 
     * @return a list of categories
     */
    @Nonnull
    public List<ApiService> getApiServices()
      {
        return Collections.unmodifiableList(apiServices);
      }   

    /**
     * Checks to see whether the server supports the category.
     * 
     * @param serviceName category name
     * @return true if it's supported.
     */
    public boolean hasApiService (final @CheckForNull String serviceName)
      {
//        if (serviceName == null) 
//          {
//            return false;
//          }
//        
//        for (final ApiService apiService : apiServices) 
//          {
//            if (serviceName.equals(apiService.getName()))
//              {
//                return true;
//              }
//          }
        
        return getApiService(serviceName) != null;
    }

    /**
     * Returns a ApiService object.
     * 
     * @param serviceName category name
     * @return ApiService object
     */
    @CheckForNull
    public ApiService getApiService (@Nullable String serviceName)
      {
        if (serviceName == null) 
          {
            return null;
          }
        
        for (final ApiService apiService : apiServices)
          {
            if (serviceName.equals(apiService.getName())) 
              {
                return apiService;
              }
          }
        
        return null;
    }

    private void addApiService (final @Nonnull String name, final @Nonnull String actionUrl)
      {
        apiServices.add(new ApiService(name, actionUrl));
      }

    /**
     * Fetches device description xml file from server and parses it.
     * 
     * @param ddUrl URL of device description xml.
     * @return ServerDevice instance
     */
    @CheckForNull
    public static ServerDevice fetch (@Nonnull String ddUrl) 
      {
        if (ddUrl == null) 
          {
            throw new NullPointerException("ddUrl is null.");
          }

        String ddXml = "";
        try 
          {
            ddXml = new DefaultSimpleHttpClient().get(ddUrl); // FIXME
            Log.d(TAG, "fetch() httpGet done.");
          } 
        catch (IOException e) 
          {
            Log.e(TAG, "fetch: IOException.", e);
            return null;
          }
        /*
          * catch (Exception e) { Log.e(TAG, "fetch: Exception.", e); return
          * null; }
          */
        XmlElement rootElement = XmlElement.parse(ddXml);

        // "root"
        ServerDevice device = null;
        
        if ("root".equals(rootElement.getTagName())) 
          {
            device = new ServerDevice();
            device.ddUrl = ddUrl;

            // "device"
            final XmlElement deviceElement = rootElement.findChild("device");
            device.friendlyName = deviceElement.findChild("friendlyName").getValue();
            device.modelName = deviceElement.findChild("modelName").getValue();
            device.udn = deviceElement.findChild("UDN").getValue();

            // "iconList"
            final XmlElement iconListElement = deviceElement.findChild("iconList");
            final List<XmlElement> iconElements = iconListElement.findChildren("icon");
            
            for (final XmlElement iconElement : iconElements)
              {
                // Choose png icon to show Android UI.
                if ("image/png".equals(iconElement.findChild("mimetype").getValue())) 
                  {
                    final String _uri = iconElement.findChild("url").getValue();
                    final String hostUrl = toSchemeAndHost(ddUrl);
                    device.iconUrl = hostUrl + _uri;
                  }
              }

            // "av:X_ScalarWebAPI_DeviceInfo"
            final XmlElement wApiElement = deviceElement.findChild("X_ScalarWebAPI_DeviceInfo");
            final XmlElement wApiServiceListElement = wApiElement.findChild("X_ScalarWebAPI_ServiceList");
            final List<XmlElement> wApiServiceElements = wApiServiceListElement.findChildren("X_ScalarWebAPI_Service");
            
            for (final XmlElement wApiServiceElement : wApiServiceElements) 
              {
                final String serviceName = wApiServiceElement.findChild("X_ScalarWebAPI_ServiceType").getValue();
                final String actionUrl = wApiServiceElement.findChild("X_ScalarWebAPI_ActionList_URL").getValue();
                device.addApiService(serviceName, actionUrl);
              }
          }
  
        Log.d(TAG, "fetch () parsing XML done.");
        return device;
      }

    private static String toSchemeAndHost(String url) {
        int i = url.indexOf("://"); // http:// or https://
        if (i == -1)
            return "";
        int j = url.indexOf("/", i + 3);
        if (j == -1)
            return "";
        String hostUrl = url.substring(0, j);
        return hostUrl;
    }

    private static String toHost(String url) {
        int i = url.indexOf("://"); // http:// or https://
        if (i == -1)
            return "";
        int j = url.indexOf(":", i + 3);
        if (j == -1)
            return "";
        String host = url.substring(i + 3, j);
        return host;
    }
}
