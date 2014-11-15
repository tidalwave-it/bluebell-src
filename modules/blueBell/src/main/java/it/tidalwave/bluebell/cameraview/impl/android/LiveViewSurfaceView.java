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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
//import android.annotation.TargetApi;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import it.tidalwave.sony.SimpleLiveviewSlicer;
import it.tidalwave.bluebell.liveview.LiveViewPresentation;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A SurfaceView based class to draw liveview frames serially.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class LiveViewSurfaceView extends SurfaceView implements LiveViewPresentation, SurfaceHolder.Callback
  {
    private volatile boolean running;

    private final BlockingQueue<byte[]> imageQueue = new ArrayBlockingQueue<byte[]>(2);

    private final boolean mInMutableAvailable = false;

//    private final boolean mInMutableAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private Thread consumerThread;

    private int prevoiusWidth = 0;

    private int previousHeight = 0;

    private final Paint imagePaint = new Paint();

    /*******************************************************************************************************************
     *
     * @param context
     *
     ******************************************************************************************************************/
    public LiveViewSurfaceView (final @Nonnull Context context)
      {
        super(context);
        getHolder().addCallback(this);
        imagePaint.setDither(true);
      }

    /*******************************************************************************************************************
     *
     * @param context
     * @param attrs
     *
     ******************************************************************************************************************/
    public LiveViewSurfaceView (final @Nonnull Context context, final @Nonnull AttributeSet attrs)
      {
        super(context, attrs);
        getHolder().addCallback(this);
        imagePaint.setDither(true);
      }

    /*******************************************************************************************************************
     *
     * @param context
     * @param attrs
     * @param defStyle
     *
     ******************************************************************************************************************/
    public LiveViewSurfaceView (final @Nonnull Context context, final @Nonnull AttributeSet attrs, final int defStyle)
      {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        imagePaint.setDither(true);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override
    public void postPayload (final @Nonnull SimpleLiveviewSlicer.Payload payload)
      {
        log.debug("postPayload({}) - {}", payload, running);

        if (running)
          {
            if (imageQueue.size() == 2)
              {
                imageQueue.remove();
              }

            imageQueue.add(payload.jpegData);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override
    public void surfaceChanged (final @Nonnull SurfaceHolder holder, final int format, final int width, final int height)
      {
        // do nothing.
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override
    public void surfaceCreated (final @Nonnull SurfaceHolder holder)
      {
        // do nothing.
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override
    public void surfaceDestroyed (final @Nonnull SurfaceHolder holder)
      {
        running = false;
//        FIXME: stop the controller?
      }

    /*******************************************************************************************************************
     *
     * Start the consumer thread that refresh the view pulling images from the qeueu.
     *
     ******************************************************************************************************************/
    @Override
    public void start()
      {
        if (running)
          {
            log.warn("start() already starting.");
            return;// false;
          }

        running = true;

        consumerThread = new Thread()
          {
            @Override
            public void run()
              {
                log.debug("Starting drawing liveview frame.");
                Bitmap frameBitmap = null;

                final BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
                factoryOptions.inSampleSize = 1;

                if (mInMutableAvailable)
                  {
//                    initInBitmap(factoryOptions);
                  }

                while (running)
                  {
                    try
                      {
                        final byte[] jpegData = imageQueue.take();
                        frameBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, factoryOptions);
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (mInMutableAvailable)
                          {
//                            clearInBitmap(factoryOptions);
                          }
                        continue;
                      }
                    catch (InterruptedException e)
                      {
                        log.info("Drawer thread is Interrupted.");
                        break;
                      }

                    if (mInMutableAvailable)
                      {
//                        setInBitmap(factoryOptions, frameBitmap);
                      }

                    drawFrame(frameBitmap);
                  }

                if (frameBitmap != null)
                  {
                    frameBitmap.recycle();
                  }

                running = false;
              }
          };

        consumerThread.start();
      }

    /*******************************************************************************************************************
     *
     * Stops the refreshing of the view.
     *
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        if (consumerThread != null)
          {
            consumerThread.interrupt();
          }

        imageQueue.clear();
        running = false;
      }

    /*******************************************************************************************************************
     *
     *
     * @return  true if started
     *
     ******************************************************************************************************************/
    public boolean isStarted()
      {
        return running;
      }

//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    private void initInBitmap(BitmapFactory.Options options) {
//        options.inBitmap = null;
//        options.inMutable = true;
//    }
//
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    private void clearInBitmap(BitmapFactory.Options options) {
//        if (options.inBitmap != null) {
//            options.inBitmap.recycle();
//            options.inBitmap = null;
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    private void setInBitmap(BitmapFactory.Options options, Bitmap bitmap) {
//        options.inBitmap = bitmap;
//    }

    /*******************************************************************************************************************
     *
     * Renders a frame.
     *
     ******************************************************************************************************************/
    private void drawFrame (final @Nonnull Bitmap frame)
      {
        if ((frame.getWidth() != prevoiusWidth) || (frame.getHeight() != previousHeight))
          {
            onDetectedFrameSizeChanged(frame.getWidth(), frame.getHeight());
          }
        else
          {
            final Canvas canvas = getHolder().lockCanvas();

            if (canvas != null)
              {
                final int width = frame.getWidth();
                final int height = frame.getHeight();
                final Rect source = new Rect(0, 0, width, height);
                final float aspectRatio = Math.min((float)getWidth() / width, (float)getHeight() / height);
                final int offsetX = (getWidth() - (int)(width * aspectRatio)) / 2;
                final int offsetY = (getHeight() - (int)(height * aspectRatio)) / 2;
                final Rect destination = new Rect(offsetX, offsetY, getWidth() - offsetX, getHeight() - offsetY);
                canvas.drawBitmap(frame, source, destination, imagePaint);
                getHolder().unlockCanvasAndPost(canvas);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void onDetectedFrameSizeChanged (final int width, final int height)
      {
        log.debug("Change of aspect ratio detected");
        prevoiusWidth = width;
        previousHeight = height;
        drawBlackFrame();
        drawBlackFrame();
        drawBlackFrame(); // delete triple buffers
     }

    /*******************************************************************************************************************
     *
     * Renders black on the view.
     *
     ******************************************************************************************************************/
    private void drawBlackFrame()
      {
        final Canvas canvas = getHolder().lockCanvas();

        if (canvas != null)
          {
            final Paint blackPaint = new Paint();
            blackPaint.setColor(Color.BLACK);
            blackPaint.setStyle(Paint.Style.FILL);

            canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), blackPaint);
            getHolder().unlockCanvasAndPost(canvas);
          }
      }
  }
