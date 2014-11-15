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
package it.tidalwave.sony.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraApiService;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.CameraDevice;
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
    private final List<CameraApiService> apiServices = new ArrayList<>();

    private CameraApi api;

    private CameraObserver observer;

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public DefaultCameraDevice (final List<CameraApiService> apiServices) 
      {
        this.apiServices.addAll(apiServices);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<CameraApiService> getApiServices()
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
    public CameraApiService getApiService (final @Nullable String serviceName)
      {
        if (serviceName == null)
          {
            return null;
          }

        for (final CameraApiService apiService : apiServices)
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
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public synchronized CameraApi getApi()
      {
        if (api == null)
          {
            api = new DefaultCameraApi(this);
          }

        return api;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public synchronized CameraObserver getObserver()
      {
        if (observer == null)
          {
            observer = new DefaultCameraObserver(getApi());
          }

        return observer;
      }
  }