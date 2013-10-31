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

    private ImageView ivPhotoBox;

    private RadioGroup rbShootModeSelector;

    private Button btTakePhoto;

    private Button btRecStartStop;

    private TextView tvCameraStatus;

    private SimpleLiveviewSurfaceView mLiveviewSurface;

    private final DefaultCameraViewControl control = new AndroidCameraViewControl(this, this);

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
    public void renderCameraStatus (final @Nonnull String cameraStatus)
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                tvCameraStatus.setText(cameraStatus);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void renderRecStartStopButtonAsStop()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                btRecStartStop.setEnabled(true);
                btRecStartStop.setText(R.string.button_rec_stop);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void renderRecStartStopButtonAsStart()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                btRecStartStop.setEnabled(true);
                btRecStartStop.setText(R.string.button_rec_start);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void disableRecStartStopButton()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                btRecStartStop.setEnabled(false);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void enableTakePhotoButton (final boolean enabled)
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                btTakePhoto.setEnabled(enabled);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void hidePhotoBox()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                ivPhotoBox.setVisibility(View.INVISIBLE);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void enableShootModeSelector (final @Nonnull String shootMode)
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                for (int i = 0; i < rbShootModeSelector.getChildCount(); i++)
                  {
                    rbShootModeSelector.getChildAt(i).setEnabled(true);
                  }

                View radioButton = rbShootModeSelector.findViewWithTag(shootMode);

                if (radioButton != null)
                  {
                    rbShootModeSelector.check(radioButton.getId());
                  }
                else
                  {
                    rbShootModeSelector.clearCheck();
                  }
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void disableShootModeSelector()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                for (int i = 0; i < rbShootModeSelector.getChildCount(); i++)
                  {
                    rbShootModeSelector.getChildAt(i).setEnabled(false);
                  }
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
                ivPhotoBox.setVisibility(View.VISIBLE);
                ivPhotoBox.setImageDrawable((Drawable)picture);
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void notifyRecStart()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(SampleCameraActivity.this, R.string.msg_rec_start, Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void notifyRecStop()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(SampleCameraActivity.this, R.string.msg_rec_stop, Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void notifyErrorWhileRecordingMovie()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(SampleCameraActivity.this, R.string.msg_error_api_calling, Toast.LENGTH_SHORT).show();
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

        ivPhotoBox = (ImageView) findViewById(R.id.image_picture_wipe);
        rbShootModeSelector = (RadioGroup) findViewById(R.id.radio_group_shoot_mode);
        btTakePhoto = (Button) findViewById(R.id.button_take_picture);
        btRecStartStop = (Button) findViewById(R.id.button_rec_start_stop);
        tvCameraStatus = (TextView) findViewById(R.id.text_camera_status);
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

        btTakePhoto.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v)
              {
                control.takeAndFetchPicture();
              }
          });

        btRecStartStop.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v)
              {
                if ("MovieRecording".equals(cameraObserver.getStatus()))
                  {
                    control.stopMovieRec();
                  }
                else if ("IDLE".equals(cameraObserver.getStatus()))
                  {
                    control.startMovieRec();
                  }
              }
          });

        ivPhotoBox.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v)
              {
                ivPhotoBox.setVisibility(View.INVISIBLE);
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
    // Prepare for Radio Button UI of Shoot Mode.
    private void prepareShootModeRadioButtonsUi (final @Nonnull String[] availableShootModes,
                                                 final @Nonnull String currentMode)
      {
        rbShootModeSelector.clearCheck();
        rbShootModeSelector.removeAllViews();

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

            rbShootModeSelector.addView(radioBtn);

            if (mode.equals(currentMode))
              {
                // Set the flag true to suppress unnecessary API calling.
                mRadioInitialChecked = true;
                rbShootModeSelector.check(viewId);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
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
  }
