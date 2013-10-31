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
package it.tidalwave.bluebell.cameraview.impl.android;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.bluebell.cameraview.CameraView;
import it.tidalwave.bluebell.cameraview.DefaultCameraViewControl;
import it.tidalwave.bluebell.liveview.LiveView;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import lombok.Cleanup;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class AndroidCameraViewControl extends DefaultCameraViewControl
  {
    @Nonnull
    private final Activity activity;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public AndroidCameraViewControl (final @Nonnull CameraView view,
                                     final @Nonnull LiveView liveView,
                                     final @Nonnull CameraDevice cameraDevice,
                                     final @Nonnull Activity activity)
      {
        super(view, liveView, cameraDevice);
        this.activity = activity;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected Object loadPicture (final @Nonnull URL url)
      throws IOException
      {
        final @Cleanup InputStream is = new BufferedInputStream(url.openStream());
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4; // irresponsible value
        final Drawable pictureDrawable = new BitmapDrawable(activity.getResources(),
                                                            BitmapFactory.decodeStream(is, null, options));
        is.close();
        return pictureDrawable;
      }
  }
