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
package it.tidalwave.bluebell.cameradiscovery;

import it.tidalwave.sony.CameraDevice;
import javax.annotation.Nonnull;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraDiscoveryPresentationControl
  {
    /*******************************************************************************************************************
     *
     * Start the controller.
     * 
     ******************************************************************************************************************/
    public void start();
    
    /*******************************************************************************************************************
     *
     * Stop the controller.
     * 
     ******************************************************************************************************************/
    public void stop();

    /*******************************************************************************************************************
     *
     * Starts the discovery of devices.
     * 
     ******************************************************************************************************************/
    public void startDiscovery();
    
    /*******************************************************************************************************************
     *
     * Shows the presentation for controlling the given {@link CameraDevice} if it exposes an API for remote control;
     * otherwise notify that the device is not compatible.
     *
     * @param cameraDevice    the camera
     * 
     ******************************************************************************************************************/
    public void showCameraPresentation (@Nonnull CameraDevice cameraDevice);
  }
