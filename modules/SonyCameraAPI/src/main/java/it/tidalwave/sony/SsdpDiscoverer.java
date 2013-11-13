/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
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

/***********************************************************************************************************************
 *
 * A SSDP client class for this sample application. This implementation keeps simple so that many developers understand 
 * quickly.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface SsdpDiscoverer
  {
    /*******************************************************************************************************************
     * 
     * Handler interface for SSDP search result.
     * 
     ******************************************************************************************************************/
    public static interface Callback 
      {
        /***************************************************************************************************************
         * 
         * Called when API server device is found.
         * 
         * @param device API server device that is found by searching
         * 
         **************************************************************************************************************/
        public void onDeviceFound (@Nonnull CameraDevice device);

        /***************************************************************************************************************
         * 
         * Called when searching completes successfully. 
         * 
         **************************************************************************************************************/
        public void onFinished();

        /***************************************************************************************************************
         * 
         * Called when searching completes with some errors.
         * 
         **************************************************************************************************************/
        public void onErrorFinished();
      }

    /*******************************************************************************************************************
     * 
     * Cancels searching. Note that it cannot stop the operation immediately.
     *
     * @return true: now searching, false: otherwise
     * 
     ******************************************************************************************************************/
    public void cancelSearching();

    /*******************************************************************************************************************
     * 
     * Checks whether searching is in progress or not.
     *
     * @return true: now searching, false: otherwise
     * 
     ******************************************************************************************************************/
    public boolean isSearching();

    /*******************************************************************************************************************
     * 
     * Search API server device.
     *
     * @param calback result handler
     * @return true: start successfully, false: already searching now
     * 
     ******************************************************************************************************************/
    public boolean search (@Nonnull Callback calback);  
  }