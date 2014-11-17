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
package it.tidalwave.bluebell.cameraview.impl.android;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import it.tidalwave.sony.CameraDescriptor;
import it.tidalwave.bluebell.cameraview.CameraPresentation;
import it.tidalwave.bluebell.cameraview.DefaultCameraPresentationControl;
import it.tidalwave.bluebell.liveview.LiveViewPresentation;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import lombok.Cleanup;

/***********************************************************************************************************************
 *
 * The Android specialisation of {@link DefaultCameraPresentationControl}, which contains Android-specific code.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class AndroidCameraPresentationControl extends DefaultCameraPresentationControl
  {
    @Nonnull
    private final Context context;

    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param   presentation        the controlled presentation
     * @param   context             the Android {@link Context}
     * @param   liveView            the live view
     * @param   cameraDescriptor    the descriptor of the current device
     * @param   executorService     an {@link ExecutorService} for running background jobs
     * 
     ******************************************************************************************************************/
    public AndroidCameraPresentationControl (final @Nonnull CameraPresentation presentation,
                                             final @Nonnull Context context,
                                             final @Nonnull LiveViewPresentation liveView,
                                             final @Nonnull CameraDescriptor cameraDescriptor,
                                             final @Nonnull ExecutorService executorService)
      {
        super(presentation, liveView, cameraDescriptor, executorService);
        this.context = context;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected Drawable loadPicture (final @Nonnull URL url)
      throws IOException
      {
        final @Cleanup InputStream is = new BufferedInputStream(url.openStream());
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4; // FIXME
        final Drawable pictureDrawable = new BitmapDrawable(context.getResources(),
                                                            BitmapFactory.decodeStream(is, null, options));
        is.close();
        return pictureDrawable;
      }
  }
