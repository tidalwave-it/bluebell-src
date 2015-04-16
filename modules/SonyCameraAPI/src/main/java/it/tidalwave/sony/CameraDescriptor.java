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
package it.tidalwave.sony;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/***********************************************************************************************************************
 *
 * A description of a camera device. This entity only contains static data, so its instances can be serialized (e.g. to
 * be passed as a 'extra' in Android intents). The dynamic part has been moved into the {@link CameraDevice} entity.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraDescriptor extends Serializable
  {
    /*******************************************************************************************************************
     *
     * Returns the model name of this device.
     * 
     * @return      the model name
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getModelName();

    /*******************************************************************************************************************
     *
     * Returns the friendly name of this device.
     * 
     * @return      the friendly name
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getFriendlyName();

    /*******************************************************************************************************************
     *
     * Returns the IP address of the device.
     *
     * @return      the IP address of the device
     *
     ******************************************************************************************************************/
    @Nullable
    public String getIpAddress();

    /*******************************************************************************************************************
     *
     * Returns the URL at which the DD can be downloaded.
     * 
     * @return      the URL of the DD
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getDdUrl();

    /*******************************************************************************************************************
     *
     * Returns the URL of the icon of this device.
     * 
     * @return      the icon URL
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getIconUrl();

    /*******************************************************************************************************************
     *
     * Returns the Unique Device Name (UDN) of this device.
     * 
     * @return      the UDN
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getUdn();

    /*******************************************************************************************************************
     *
     * Creates a {@link CameraDevice} instance that allows controlling the device.
     * 
     * @return      the {@code CameraDevice}
     *
     ******************************************************************************************************************/
    @Nonnull
    public CameraDevice createDevice();
  }
