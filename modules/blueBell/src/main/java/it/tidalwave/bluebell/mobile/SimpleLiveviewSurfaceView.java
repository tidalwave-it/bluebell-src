/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile;

import it.tidalwave.bluebell.liveview.LiveView;
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
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/**
 * A SurfaceView based class to draw liveview frames serially.
 */
@Slf4j
public class SimpleLiveviewSurfaceView extends SurfaceView implements LiveView, SurfaceHolder.Callback
  {
    private volatile boolean running;

    private final BlockingQueue<byte[]> mJpegQueue = new ArrayBlockingQueue<byte[]>(2);

    private final boolean mInMutableAvailable = false;

//    private final boolean mInMutableAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private Thread consumerThread;

    private int mPreviousWidth = 0;

    private int mPreviousHeight = 0;

    private final Paint mFramePaint;

    /**
     * Contractor
     *
     * @param context
     */
    public SimpleLiveviewSurfaceView (Context context)
      {
        super(context);
        getHolder().addCallback(this);
        mFramePaint = new Paint();
        mFramePaint.setDither(true);
      }

    /**
     * Contractor
     *
     * @param context
     * @param attrs
     */
    public SimpleLiveviewSurfaceView (Context context, AttributeSet attrs)
      {
        super(context, attrs);
        getHolder().addCallback(this);
        mFramePaint = new Paint();
        mFramePaint.setDither(true);
      }

    /**
     * Contractor
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SimpleLiveviewSurfaceView (Context context, AttributeSet attrs, int defStyle)
      {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        mFramePaint = new Paint();
        mFramePaint.setDither(true);
      }

    @Override
    public void postPayload (final @Nonnull SimpleLiveviewSlicer.Payload payload)
      {
        log.info("postPayload({}) - {}", payload, running);

        if (running)
          {
            if (mJpegQueue.size() == 2)
              {
                mJpegQueue.remove();
              }

            mJpegQueue.add(payload.jpegData);
          }
      }

    @Override
    public void surfaceChanged (SurfaceHolder holder, int format, int width, int height)
      {
        // do nothing.
      }

    @Override
    public void surfaceCreated (SurfaceHolder holder)
      {
        // do nothing.
      }

    @Override
    public void surfaceDestroyed (SurfaceHolder holder)
      {
        running = false;
//        FIXME: stop the controller?
      }

    /**
     * Start retrieving and drawing liveview frame data by new threads.
     *
     * @return true if the starting is completed successfully, false otherwise.
     * @exception IllegalStateException when Remote API object is not set.
     * @see SimpleLiveviewSurfaceView#bindRemoteApi(SimpleRemoteApi)
     */
    @Override
    public void start()
      {
        if (running)
          {
            log.warn("start() already starting.");
            return;// false;
          }

        running = true;

        // A thread for drawing liveview frame fetched by above thread.
        consumerThread = new Thread()
          {
            @Override
            public void run()
              {
                log.debug("Starting drawing liveview frame.");
                Bitmap frameBitmap = null;

                BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
                factoryOptions.inSampleSize = 1;

                if (mInMutableAvailable)
                  {
//                    initInBitmap(factoryOptions);
                  }

                while (running)
                  {
                    try
                      {
                        byte[] jpegData = mJpegQueue.take();
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
        return; // true;
      }

    /**
     * Request to stop retrieving and drawing liveview data.
     */
    @Override
    public void stop()
      {
        if (consumerThread != null)
          {
            consumerThread.interrupt();
          }

        mJpegQueue.clear();
        running = false;
      }

    /**
     * Check to see whether start() is already called.
     *
     * @return true if start() is already called, false otherwise.
     */
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

    // Draw frame bitmap onto a canvas.
    private void drawFrame(Bitmap frame)
      {
        if (frame.getWidth() != mPreviousWidth || frame.getHeight() != mPreviousHeight)
          {
            onDetectedFrameSizeChanged(frame.getWidth(), frame.getHeight());
            return;
          }

        Canvas canvas = getHolder().lockCanvas();

        if (canvas == null)
          {
            return;
          }

        int w = frame.getWidth();
        int h = frame.getHeight();
        Rect src = new Rect(0, 0, w, h);

        float by = Math.min((float) getWidth() / w, (float) getHeight() / h);
        int offsetX = (getWidth() - (int) (w * by)) / 2;
        int offsetY = (getHeight() - (int) (h * by)) / 2;
        Rect dst = new Rect(offsetX, offsetY, getWidth() - offsetX, getHeight() - offsetY);
        canvas.drawBitmap(frame, src, dst, mFramePaint);
        getHolder().unlockCanvasAndPost(canvas);
      }

    // Called when the width or height of liveview frame image is changed.
    private void onDetectedFrameSizeChanged(int width, int height)
      {
        log.debug("Change of aspect ratio detected");
        mPreviousWidth = width;
        mPreviousHeight = height;
        drawBlackFrame();
        drawBlackFrame();
        drawBlackFrame(); // delete triple buffers
     }

    // Draw black screen.
    private void drawBlackFrame()
      {
        Canvas canvas = getHolder().lockCanvas();

        if (canvas == null)
          {
            return;
          }

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);
        getHolder().unlockCanvasAndPost(canvas);
      }
  }
