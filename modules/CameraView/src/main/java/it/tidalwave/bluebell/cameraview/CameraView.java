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
package it.tidalwave.bluebell.cameraview;

import javax.annotation.Nonnull;
import java.util.List;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraView
  {
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public void refreshUi();

    /*******************************************************************************************************************
     *
     * Shows the progress bar.
     *
     ******************************************************************************************************************/
    public void showProgressBar();

    /*******************************************************************************************************************
     *
     * Hides the progress bar.
     *
     ******************************************************************************************************************/
    public void hideProgressBar();

    /*******************************************************************************************************************
     *
     * Starts the live view.
     *
     ******************************************************************************************************************/
    public void startLiveView();

    /*******************************************************************************************************************
     *
     * Stops the live view.
     *
     ******************************************************************************************************************/
    public void stopLiveView();

    /*******************************************************************************************************************
     *
     * Set the shoot mode widgets, creating options for the given available modes and a current mode selection.
     *
     * @param  availableModes   the available modes
     * @param  currentMode      the curent mode
     *
     ******************************************************************************************************************/
    public void setShootModeControl (@Nonnull List<String> availableModes, @Nonnull String currentMode);

    /*******************************************************************************************************************
     *
     * Shows a picture as the latest taken photo.
     *
     * @param  photo            the photo
     *
     ******************************************************************************************************************/
    public void showPhoto (@Nonnull Object photo);

    /*******************************************************************************************************************
     *
     * Notifies that there has been a connection error.
     *
     ******************************************************************************************************************/
    public void notifyConnectionError();

    /*******************************************************************************************************************
     *
     * Notifies that the camera device is not supported and quits.
     *
     ******************************************************************************************************************/
    public void notifyDeviceNotSupportedAndQuit();

    /*******************************************************************************************************************
     *
     * Notifies that there has been an error while taking the photo.
     *
     ******************************************************************************************************************/
    public void notifyErrorWhileTakingPhoto();
  }
