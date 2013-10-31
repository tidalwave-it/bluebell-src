/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile;

import it.tidalwave.bluebell.cameraview.impl.android.AndroidCameraViewControl;
import it.tidalwave.bluebell.cameraview.CameraView;
import it.tidalwave.bluebell.cameraview.DefaultCameraViewControl;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.CameraApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/**
 * An Activity class of Sample Camera screen.
 */
@Slf4j
public class SampleCameraActivity extends Activity implements CameraView
// TODO: rename to AndroidCameraViewActivity and move to the it.tidalwave.bluebill.cameraview.impl.android package
  {
    private Handler handler;
    private ImageView mImagePictureWipe;
    private RadioGroup mRadiosShootMode;
    private Button mButtonTakePicture;
    private Button mButtonRecStartStop;
    private TextView mTextCameraStatus;

    private final DefaultCameraViewControl control = new AndroidCameraViewControl(this, this);

    private SimpleLiveviewSurfaceView mLiveviewSurface;

    private boolean mRadioInitialChecked;

    private CameraApi cameraApi;// FIXME: temporary
    private CameraObserver cameraObserver; // FIXME: temporary

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyConnectionError()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(SampleCameraActivity.this, R.string.msg_error_connection, Toast.LENGTH_SHORT).show();
                setProgressBarIndeterminateVisibility(false); // FIXME: split
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyDeviceNotSupportedAndQuit()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(SampleCameraActivity.this, R.string.msg_error_non_supported_device, Toast.LENGTH_SHORT).show();
                SampleCameraActivity.this.finish(); // FIXME: split
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void startLiveView()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                mLiveviewSurface.start();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stopLiveView()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                mLiveviewSurface.stop();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void refreshUi()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                refreshUi2();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setShootModeControl (final @Nonnull List<String> availableModes, final @Nonnull String currentMode)
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                prepareShootModeRadioButtonsUi(availableModes.toArray(new String[0]), currentMode);
                setProgressBarIndeterminateVisibility(false); // FIXME: split?
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void showProgressBar()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                setProgressBarIndeterminateVisibility(true);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void hideProgressBar()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                setProgressBarIndeterminateVisibility(false);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyErrorWhileTakingPhoto()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(SampleCameraActivity.this, R.string.msg_error_take_picture, Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void showPhoto (final @Nonnull Object picture)
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                mImagePictureWipe.setVisibility(View.VISIBLE);
                mImagePictureWipe.setImageDrawable((Drawable)picture);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onCreate (Bundle savedInstanceState)
      {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_sample_camera);

        handler = new Handler();
        final SampleApplication app = (SampleApplication)getApplication();
        control.bind(app.getCameraDevice());

        cameraApi = control.getCameraApi();
        cameraObserver = control.getCameraObserver();

        mImagePictureWipe = (ImageView) findViewById(R.id.image_picture_wipe);
        mRadiosShootMode = (RadioGroup) findViewById(R.id.radio_group_shoot_mode);
        mButtonTakePicture = (Button) findViewById(R.id.button_take_picture);
        mButtonRecStartStop = (Button) findViewById(R.id.button_rec_start_stop);
        mTextCameraStatus = (TextView) findViewById(R.id.text_camera_status);
        mLiveviewSurface = (SimpleLiveviewSurfaceView) findViewById(R.id.surfaceview_liveview);
        mLiveviewSurface.bindRemoteApi(cameraApi);

        log.info("onCreate() completed.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onResume()
      {
        super.onResume();

        mButtonTakePicture.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v)
              {
                control.takeAndFetchPicture();
              }
          });

        mButtonRecStartStop.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v)
              {
                if ("MovieRecording".equals(cameraObserver.getStatus()))
                  {
                    stopMovieRec();
                  }
                else if ("IDLE".equals(cameraObserver.getStatus()))
                  {
                    startMovieRec();
                  }
              }
          });

        mImagePictureWipe.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v)
              {
                mImagePictureWipe.setVisibility(View.INVISIBLE);
              }
          });

        setProgressBarIndeterminateVisibility(true);
        control.initialize();
        log.info("onResume() completed.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onPause()
      {
        super.onPause();
        control.stop();
        log.info("onPause() completed.");
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Refresh UI appearance along current "cameraStatus" and "shootMode".
    private void refreshUi2()
      {
        final String cameraStatus = cameraObserver.getStatus();
        final String shootMode = cameraObserver.getShootMode();

        // CameraStatus TextView
        mTextCameraStatus.setText(cameraStatus);

        // Recording Start/Stop Button
        if ("MovieRecording".equals(cameraStatus))
          {
            mButtonRecStartStop.setEnabled(true);
            mButtonRecStartStop.setText(R.string.button_rec_stop);
          }
        else if ("IDLE".equals(cameraStatus) && "movie".equals(shootMode))
          {
            mButtonRecStartStop.setEnabled(true);
            mButtonRecStartStop.setText(R.string.button_rec_start);
          }
        else
          {
            mButtonRecStartStop.setEnabled(false);
          }

        mButtonTakePicture.setEnabled("still".equals(shootMode) && "IDLE".equals(cameraStatus));

        // Picture wipe Image
        if (!"still".equals(shootMode))
          {
            mImagePictureWipe.setVisibility(View.INVISIBLE);
          }

        // Shoot Mode Buttons
        if ("IDLE".equals(cameraStatus))
          {
            for (int i = 0; i < mRadiosShootMode.getChildCount(); i++)
              {
                mRadiosShootMode.getChildAt(i).setEnabled(true);
              }

            View radioButton = mRadiosShootMode.findViewWithTag(shootMode);

            if (radioButton != null)
              {
                mRadiosShootMode.check(radioButton.getId());
              }
            else
              {
                mRadiosShootMode.clearCheck();
              }

          }
        else
          {
            for (int i = 0; i < mRadiosShootMode.getChildCount(); i++)
              {
                mRadiosShootMode.getChildAt(i).setEnabled(false);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Prepare for Radio Button UI of Shoot Mode.
    private void prepareShootModeRadioButtonsUi (String[] availableShootModes, String currentMode)
      {
        mRadiosShootMode.clearCheck();
        mRadiosShootMode.removeAllViews();

        for (int i = 0; i < availableShootModes.length; i++)
          {
            final String mode = availableShootModes[i];
            final RadioButton radioBtn = new RadioButton(SampleCameraActivity.this);
            final int viewId = 123456 + i; // workaround
            radioBtn.setId(viewId);
            radioBtn.setText(mode);
            radioBtn.setTag(mode);
            radioBtn.setTextColor(0xFFFFFFFF);
            radioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
              {
                @Override
                public void onCheckedChanged (CompoundButton buttonView,  boolean isChecked)
                  {
                    if (isChecked)
                      {
                        if (mRadioInitialChecked)
                          {
                            // ignore because this callback is invoked by
                            // initializing.
                            mRadioInitialChecked = false;
                          }
                        else
                          {
                            final String mode = buttonView.getText().toString();
                            setShootMode(mode);
                          }
                      }
                  }
              });

            mRadiosShootMode.addView(radioBtn);

            if (mode.equals(currentMode))
              {
                // Set the flag true to suppress unnecessary API calling.
                mRadioInitialChecked = true;
                mRadiosShootMode.check(viewId);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Call setShootMode
    private void setShootMode (final String mode)
      {
        new Thread()
          {
            @Override
            public void run()
              {
                try
                  {
                    final JSONObject replyJson = cameraApi.setShootMode(mode).getJsonObject();
                    final JSONArray resultsObj = replyJson.getJSONArray("result");
                    final int resultCode = resultsObj.getInt(0);

                    if (resultCode == 0)
                      {
                        // Success, but no refresh UI at the point.
                      }
                    else
                      {
                        log.warn("setShootMode: error: {}", resultCode);
                        Toast.makeText(SampleCameraActivity.this, R.string.msg_error_api_calling, Toast.LENGTH_SHORT).show();
                      }
                  }
                catch (IOException e)
                  {
                    log.warn("setShootMode: IOException: ", e);
                  }
                catch (JSONException e)
                  {
                    log.warn("setShootMode: JSON format error.");
                  }
              }
          }.start();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Call startMovieRec
    private void startMovieRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    log.info("startMovieRec: exec.");
                    JSONObject replyJson = cameraApi.startMovieRec().getJsonObject();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);

                    if (resultCode == 0)
                      {
                        Toast.makeText(SampleCameraActivity.this, R.string.msg_rec_start, Toast.LENGTH_SHORT).show();
                      }
                    else
                      {
                        log.warn("startMovieRec: error: {}", resultCode);
                        Toast.makeText(SampleCameraActivity.this, R.string.msg_error_api_calling, Toast.LENGTH_SHORT).show();
                      }
                } catch (IOException e) {
                    log.warn("startMovieRec: IOException: ", e);
                } catch (JSONException e) {
                    log.warn("startMovieRec: JSON format error.");
                }
            }
        }.start();
    }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Call stopMovieRec
    private void stopMovieRec() {
        new Thread() {

            @Override
            public void run() {
                try {
                    log.info("stopMovieRec: exec.");
                    JSONObject replyJson = cameraApi.stopMovieRec().getJsonObject();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    String thumbnailUrl = resultsObj.getString(0);

                    if (thumbnailUrl != null)
                      {
                        Toast.makeText(SampleCameraActivity.this, R.string.msg_rec_stop, Toast.LENGTH_SHORT).show();
                      }
                    else
                      {
                        log.warn("stopMovieRec: error");
                        Toast.makeText(SampleCameraActivity.this, R.string.msg_error_api_calling, Toast.LENGTH_SHORT).show();
                      }
                } catch (IOException e) {
                    log.warn("stopMovieRec: IOException: ", e);
                } catch (JSONException e) {
                    log.warn("stopMovieRec: JSON format error.");
                }
            }
        }.start();
    }
  }
