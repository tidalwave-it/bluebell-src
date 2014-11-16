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
package it.tidalwave.bluebell.mobile.android;

import javax.annotation.Nonnull;
import android.content.Context;
import android.content.Intent;
import it.tidalwave.sony.CameraDescriptor;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * An {@code IntentHelper} is a wrapper around an {@link Intent} that allows to set and get parameters.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class CameraDescriptorIntentHelper 
  {
    private static final String KEY_CAMERA_DESCRIPTOR = "cameraDescriptor";
    
    private final Class<?> explicitTopic;
    
    @Getter @Nonnull
    private final CameraDescriptor cameraDescriptor;

    public CameraDescriptorIntentHelper (final @Nonnull Class<?> explicitTopic, 
                                         final @Nonnull CameraDescriptor cameraDescriptor) 
      {
        this.explicitTopic = explicitTopic;
        this.cameraDescriptor = cameraDescriptor;
      }
    
    public CameraDescriptorIntentHelper (final @Nonnull Intent intent) 
      {
        explicitTopic = null;
        cameraDescriptor = (CameraDescriptor)intent.getSerializableExtra(KEY_CAMERA_DESCRIPTOR);
      }
    
    @Nonnull
    public Intent createIntent (final @Nonnull Context context) 
      {
        final Intent intent = new Intent(context, explicitTopic);
        intent.putExtra(KEY_CAMERA_DESCRIPTOR, cameraDescriptor);
        return intent;
      }
  }
