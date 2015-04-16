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
package it.tidalwave.bluebell.cameraview;

import javax.annotation.Nonnull;
import it.tidalwave.sony.CameraObserver;

/***********************************************************************************************************************
 *
 * @stereotype Controller
 * 
 * The controller of {@link CameraPresentation}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraPresentationControl
  {
    /*******************************************************************************************************************
     *
     * Starts the controller. This means that the live view is enabled (if available) and the presentation is updated
     * with the camera status.
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
     * Sets the shoot mode.
     * 
     * @param   mode        the mode
     *
     ******************************************************************************************************************/
    public void setShootMode (@Nonnull String mode);

    /*******************************************************************************************************************
     *
     * Takes a picture and downloads it.
     *
     ******************************************************************************************************************/
    public void takeAndFetchPicture();

    /*******************************************************************************************************************
     *
     * Toggles start/stop movie recording.
     *
     ******************************************************************************************************************/
    public void startOrStopMovieRecording();
    
    /*******************************************************************************************************************
     *
     * Allow the user to edit the given property and set to the camera.
     * 
     * @param   property    the property
     *
     ******************************************************************************************************************/
    public void editProperty (@Nonnull CameraObserver.Property property);
  }
