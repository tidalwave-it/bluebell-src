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
package it.tidalwave.sony;

import javax.annotation.Nonnull;
import java.util.Set;

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
         * @param apis a list of available APIs
         *
         **************************************************************************************************************/
        public void onApisChanged (@Nonnull Set<String> apis);

        /***************************************************************************************************************
         *
         * Called when the value of "Camera Status" is changed.
         *
         * @param status camera status (ex."IDLE")
         *
         **************************************************************************************************************/
        public void onStatusChanged (@Nonnull String status);

        /***************************************************************************************************************
         *
         * Called when the value of "Shoot Mode" is changed.
         *
         * @param shootMode shoot mode (ex."still")
         *
         **************************************************************************************************************/
        public void onShootModeChanged (@Nonnull String shootMode);
      }

    /*******************************************************************************************************************
     *
     * Clears a listener object.
     *
     ******************************************************************************************************************/
    public void unsetListener();

    /*******************************************************************************************************************
     *
     * Returns the current Camera Status value.
     *
     * @return camera status
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getStatus();

    /*******************************************************************************************************************
     *
     * Returns the current Shoot Mode value.
     *
     * @return shoot mode
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getShootMode();

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