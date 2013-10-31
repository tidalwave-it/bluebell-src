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
import it.tidalwave.sony.CameraDevice;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CameraViewControl
  {
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public void bind (@Nonnull CameraDevice cameraDevice);

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public void initialize();

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public void stop();
  }
