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
 * 
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraService
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public CameraApi getApi();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public CameraObserver getObserver();

    /*******************************************************************************************************************
     *
     * Returns a ApiService object.
     *
     * @param serviceName category name
     * @return ApiService object
     *
     ******************************************************************************************************************/
    @CheckForNull
    public CameraDeviceDescriptor.ApiService getApiService (@Nullable String serviceName);

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<CameraDeviceDescriptor.ApiService> getApiServices();

    /*******************************************************************************************************************
     *
     * Checks to see whether the server supports the category.
     *
     * @param serviceName category name
     * @return true if it's supported.
     *
     ******************************************************************************************************************/
    public boolean hasApiService (@CheckForNull String serviceName);
  }
