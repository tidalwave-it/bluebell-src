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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import it.tidalwave.bluebell.net.impl.DefaultHttpClient;
import it.tidalwave.bluebell.net.impl.XmlElement;
import it.tidalwave.sony.CameraDevice;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A server device description class.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @ToString
public class DefaultCameraDevice implements CameraDevice
  {
//    private final SimpleHttpClient httpClient = new DefaultSimpleHttpClient();

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

    private final List<ApiService> apiServices = new ArrayList<ApiService>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nullable
    public String getIpAddress()
      {
        String ip = null;

        if (ddUrl != null)
          {
            return toHost(ddUrl);
          }

        return ip;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<ApiService> getApiServices()
      {
        return Collections.unmodifiableList(apiServices);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean hasApiService (final @CheckForNull String serviceName)
      {
        return getApiService(serviceName) != null;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @CheckForNull
    public ApiService getApiService (final @Nullable String serviceName)
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

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @CheckForNull
    public static DefaultCameraDevice fetch (@Nonnull String ddUrl)
      {
        if (ddUrl == null)
          {
            throw new NullPointerException("ddUrl is null.");
          }

        String ddXml = "";
        try
          {
            ddXml = new DefaultHttpClient().get(ddUrl); // FIXME
            log.debug("fetch() httpGet done.");
          }
        catch (IOException e)
          {
            log.error("fetch(): IOException.", e);
            return null;
          }
        /*
          * catch (Exception e) { Log.e(TAG, "fetch: Exception.", e); return
          * null; }
          */
        XmlElement rootElement = XmlElement.parse(ddXml);

        log.info("response XLM element: {}", rootElement);
        // "root"
        DefaultCameraDevice device = null;

        if ("root".equals(rootElement.getTagName()))
          {
            device = new DefaultCameraDevice();
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
            final XmlElement wApiElement = deviceElement.findChild("av:X_ScalarWebAPI_DeviceInfo");
            final XmlElement wApiServiceListElement = wApiElement.findChild("av:X_ScalarWebAPI_ServiceList");
            final List<XmlElement> wApiServiceElements = wApiServiceListElement.findChildren("av:X_ScalarWebAPI_Service");

            for (final XmlElement wApiServiceElement : wApiServiceElements)
              {
                final String serviceName = wApiServiceElement.findChild("av:X_ScalarWebAPI_ServiceType").getValue();
                final String actionUrl = wApiServiceElement.findChild("av:X_ScalarWebAPI_ActionList_URL").getValue();
                device.addApiService(serviceName, actionUrl);
              }
          }

        log.debug("fetch() parsing XML done.");
        return device;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void addApiService (final @Nonnull String name, final @Nonnull String actionUrl)
      {
        apiServices.add(new ApiService(name, actionUrl));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String toSchemeAndHost (final @Nonnull String url)
      {
        int i = url.indexOf("://"); // http:// or https://

        if (i == -1)
          {
            return "";
          }

        int j = url.indexOf("/", i + 3);

        if (j == -1)
          {
            return "";
          }

        return url.substring(0, j);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private static String toHost(String url)
      {
        int i = url.indexOf("://"); // http:// or https://

        if (i == -1)
          {
            return "";
          }

        int j = url.indexOf(":", i + 3);

        if (j == -1)
          {
            return "";
          }

        return url.substring(i + 3, j);
      }
  }