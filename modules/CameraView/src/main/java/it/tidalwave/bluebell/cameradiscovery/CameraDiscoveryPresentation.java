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
import it.tidalwave.sony.CameraDeviceDescriptor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraDiscoveryPresentation
  {
    /*******************************************************************************************************************
     *
     * Renders the current WiFi state.
     * 
     * @param       wiFiState       the WiFi state
     *
     ******************************************************************************************************************/
    public void renderWiFiState (@Nonnull String wiFiState);

    /*******************************************************************************************************************
     *
     * Notifies that the device search is in progress.
     *
     ******************************************************************************************************************/
    public void notifySearchInProgress();

    /*******************************************************************************************************************
     *
     * Notifies that the device search has completed.
     *
     ******************************************************************************************************************/
    public void notifySearchFinished();

    /*******************************************************************************************************************
     *
     * Notifies that the device search has completed with an error.
     *
     ******************************************************************************************************************/
    public void notifySearchFinishedWithError();

    /*******************************************************************************************************************
     *
     * Notifies the selected device name.
     *
     ******************************************************************************************************************/
    public void notifySelectedDeviceName (@Nonnull String deviceName);
    
    /*******************************************************************************************************************
     *
     * Notifies that the selected device is not supported.
     *
     ******************************************************************************************************************/
    public void notifySelectedDeviceNotSupported();

    /*******************************************************************************************************************
     *
     * Clears the device list.
     *
     ******************************************************************************************************************/
    public void clearDeviceList();
    
    /*******************************************************************************************************************
     *
     * Renders one more device in the list. 
     *
     ******************************************************************************************************************/
    public void renderOneMoreDevice (@Nonnull CameraDeviceDescriptor cameraDeviceDescriptor);

    /*******************************************************************************************************************
     *
     * Enables the search button.
     *
     ******************************************************************************************************************/
    public void enableSearchButton();

    /*******************************************************************************************************************
     *
     * Disables the search button.
     *
     ******************************************************************************************************************/
    public void disableSearchButton();
  }
