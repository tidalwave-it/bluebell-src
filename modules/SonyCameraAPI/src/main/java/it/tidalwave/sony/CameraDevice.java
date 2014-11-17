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
package it.tidalwave.sony;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/***********************************************************************************************************************
 *
 * This class models the dynamic behaviour of the device and allows it to be controlled.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraDevice
  {
    /*******************************************************************************************************************
     *
     * Returns a reference to the camera API.
     * 
     * @return      the camera API
     *
     ******************************************************************************************************************/
    @Nonnull
    public CameraApi getApi();

    /*******************************************************************************************************************
     *
     * Returns a list of available {@link CameraApiService}.
     * 
     * @return      the available camera devices
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<CameraApiService> getApiServices();

    /*******************************************************************************************************************
     *
     * Returns a {@link CameraApiService} given its name.
     *
     * @param   serviceName     the service name
     * @return                  the service
     *
     ******************************************************************************************************************/
    @CheckForNull
    public CameraApiService getApiService (@Nullable String serviceName);

    /*******************************************************************************************************************
     *
     * Checks whether a {@link CameraApiService} with a given name is available.
     *
     * @param   serviceName     the service name
     * @return                  {@code true} if the service is supported.
     *
     ******************************************************************************************************************/
    public boolean hasApiService (@CheckForNull String serviceName);

    /*******************************************************************************************************************
     *
     * Returns an observer of this camera that allows the status to be read and a state listener to be registered.
     * 
     * @return      the {@link CameraObserver}
     *
     ******************************************************************************************************************/
    @Nonnull
    public CameraObserver getObserver();
  }
