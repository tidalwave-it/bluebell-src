/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import it.tidalwave.sony.CameraDevice.ApiService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * A server device description class.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraDevice
  {
    /*******************************************************************************************************************
     *
     * Camera Remote API service (category). For example, "camera", "guide" and
     * so on. "Action List URL" is API request target URL of each service.
     *
     ******************************************************************************************************************/
    @AllArgsConstructor @ToString
    public static class ApiService
      {
        @Getter @Setter @Nonnull
        private String name;

        @Getter @Setter @Nonnull
        private String actionListUrl;

        /***************************************************************************************************************
         *
         * Returns the endpoint URL of the category.
         *
         * @return endpoint URL
         *
         **************************************************************************************************************/
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

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public CameraApi getApi();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public CameraObserver getObserver();

    /*******************************************************************************************************************
     *
     * Returns a ApiService object.
     *
     * @param serviceName category name
     * @return ApiService object
     *
     ******************************************************************************************************************/
    @CheckForNull
    public ApiService getApiService (@Nullable String serviceName);

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<ApiService> getApiServices();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getDdUrl();

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getFriendlyName();

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getIconUrl();

    /*******************************************************************************************************************
     *
     * Returns IP address of the DD.
     *
     * @return
     *
     ******************************************************************************************************************/
    @Nullable
    public String getIpAddress();

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getModelName();

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getUdn();

    /*******************************************************************************************************************
     *
     * Checks to see whether the server supports the category.
     *
     * @param serviceName category name
     * @return true if it's supported.
     *
     ******************************************************************************************************************/
    public boolean hasApiService (@CheckForNull String serviceName);
  }
