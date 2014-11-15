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

import javax.annotation.Nonnull;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.io.Serializable;

/***********************************************************************************************************************
 *
 * The controller of {@link CameraDiscoveryPresentation}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraDiscoveryPresentationControl
  {
    /*******************************************************************************************************************
     *
     * Starts the controller.
     * 
     ******************************************************************************************************************/
    public void start();
    
    /*******************************************************************************************************************
     *
     * Stops the controller.
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
     * Returns a {@link Serializable} object that represents the internal status of the controller.
     * 
     * @return      the memento
     * 
     ******************************************************************************************************************/
    @Nonnull
    public Serializable getMemento();
    
    /*******************************************************************************************************************
     *
     * Sets the internal status of the controller from a previously retrieved memento.
     * 
     * @param       memento     the memento
     * @see #getMemento() 
     * 
     ******************************************************************************************************************/
    public void setMemento (@Nullable Serializable memento);
    
    /*******************************************************************************************************************
     *
     * Notifies that the presentation has selected a {@link it.tidalwave.sony.CameraDescriptor}. If it exposes an API 
     * for remote control, a presentation for controlling it will be shown. Otherwise a notification that the device 
     * is not compatible will be rendered.
     * 
     * @param       selectedCameraIndex    the index of the selected camera
     * 
     ******************************************************************************************************************/
    public void notifyCameraDeviceSelected (@Nonnegative int selectedCameraIndex);
  }
