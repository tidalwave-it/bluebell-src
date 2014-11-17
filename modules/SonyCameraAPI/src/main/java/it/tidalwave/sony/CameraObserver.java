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

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.List;
import java.io.IOException;

/***********************************************************************************************************************
 *
 * A simple observer class for some status values in Camera. This class supports only a few of values of getEvent
 * result, so please add implementation for the rest of values you want to handle.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraObserver
  {
    public enum Property
      {
        CAMERA_STATUS,
        SHOOT_MODE,
        F_NUMBER,
        SHUTTER_SPEED,
        ISO_SPEED_RATE,
        EXPOSURE_COMPENSATION,
        FLASH_MODE,
        FOCUS_MODE,
        WHITE_BALANCE;
      }

    /*******************************************************************************************************************
     *
     * A listener interface to receive these changes. These methods will be called by UI thread.
     *
     ******************************************************************************************************************/
    public static interface ChangeListener
      {
        /***************************************************************************************************************
         *
         * Called when the list of available APIs is modified.
         *
         * @param   apis            all the available APIs
         * @param   addedApis       APIs that have been added
         * @param   removedApis     APIs that have been deleted
         *
         **************************************************************************************************************/
        public void onApisChanged (@Nonnull Set<String> apis,
                                   @Nonnull Set<String> addedApis, 
                                   @Nonnull Set<String> removedApis);

        /***************************************************************************************************************
         *
         * Called when the value of a property in the camera has been changed.
         *
         * @param   property    the property
         * @param   value       the property value
         *
         **************************************************************************************************************/
        public void onPropertyChanged (@Nonnull Property property, @Nonnull String value);
      }

    /*******************************************************************************************************************
     *
     * Clears a listener object.
     *
     ******************************************************************************************************************/
    public void unsetListener();

    /*******************************************************************************************************************
     *
     * Gets a property from the camera.
     * 
     * @param   property    the property
     * @return              the property value
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getProperty (@Nonnull Property property);
            
    /*******************************************************************************************************************
     *
     * Sets a property to the camera.
     * 
     * @param   property    the property
     * @param   value       the value
     * @throws  IOException in case of error
     *
     ******************************************************************************************************************/
    public void setProperty (@Nonnull Property property, @Nonnull String value)
      throws IOException;
    
    /*******************************************************************************************************************
     *
     * Returns the feasible values for the given property.
     *
     * @param   property    the property
     * @return              the values
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<String> getPropertyFeasibleValues (@Nonnull Property property);
    
    /*******************************************************************************************************************
     *
     * Checks to see whether a monitoring is already started.
     *
     * @return true when monitoring is started.
     *
     ******************************************************************************************************************/
    public boolean isRunning();

    /*******************************************************************************************************************
     *
     * Sets a listener object.
     *
     * @param listener
     *
     ******************************************************************************************************************/
    public void setListener (@Nonnull ChangeListener listener);

    /*******************************************************************************************************************
     *
     * Starts monitoring by continuously calling getEvent API.
     *
     * @return true if it successfully started, false if a monitoring is already started.
     *
     ******************************************************************************************************************/
    public boolean start();

    /*******************************************************************************************************************
     *
     * Requests to stop the monitoring.
     *
     ******************************************************************************************************************/
    public void stop();
  }
