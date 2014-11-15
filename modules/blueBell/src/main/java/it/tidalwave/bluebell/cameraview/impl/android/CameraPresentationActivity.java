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
package it.tidalwave.bluebell.cameraview.impl.android;

import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.bluebell.cameraview.CameraPresentation;
import it.tidalwave.bluebell.cameraview.DefaultCameraPresentationControl;
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
import it.tidalwave.bluebell.mobile.R;
import it.tidalwave.bluebell.mobile.android.BlueBellApplication;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class CameraPresentationActivity extends Activity implements CameraPresentation
  {
    private Handler handler;

    private ImageView ivPhotoBox;

    private RadioGroup rbShootModeSelector;

    private Button btTakePhoto;

    private Button btRecStartStop;

    private TextView tvCameraStatus;

    private LiveViewSurfaceView liveviewSurface;

    private DefaultCameraPresentationControl control;

    private boolean radioInitialChecked;

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
                Toast.makeText(CameraPresentationActivity.this, R.string.msg_error_connection, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CameraPresentationActivity.this, 
                               R.string.msg_error_non_supported_device,
                               Toast.LENGTH_SHORT).show();
                CameraPresentationActivity.this.finish(); // FIXME: split
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
                Toast.makeText(CameraPresentationActivity.this,
                               R.string.msg_error_take_picture, 
                               Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void notifyRecStart()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(CameraPresentationActivity.this, R.string.msg_rec_start, Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyRecStop()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(CameraPresentationActivity.this, R.string.msg_rec_stop, Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyErrorWhileRecordingMovie()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(CameraPresentationActivity.this,
                               R.string.msg_error_api_calling,
                               Toast.LENGTH_SHORT).show();
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyGenericError()
      {
        handler.post(new Runnable()
          {
            @Override
            public void run()
              {
                Toast.makeText(CameraPresentationActivity.this,
                               R.string.msg_error_api_calling,
                               Toast.LENGTH_SHORT).show();
              }
          });
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public void onTakePhotoClickedClick (final @Nonnull View view)
      {
        control.takeAndFetchPicture();
      }

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public void onRecStartStopClicked (final @Nonnull View view)
      {
        control.startOrStopMovieRecording();
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public void onPhotoViewClicked (final @Nonnull View view)
      {
        ivPhotoBox.setVisibility(View.INVISIBLE);
      }
            
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onCreate (final @Nonnull Bundle savedInstanceState)
      {
        log.info("onCreate({})", savedInstanceState);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_camera_presentation);

        ivPhotoBox = (ImageView)findViewById(R.id.image_picture_wipe);
        rbShootModeSelector = (RadioGroup)findViewById(R.id.radio_group_shoot_mode);
        btTakePhoto = (Button)findViewById(R.id.button_take_picture);
        btRecStartStop = (Button)findViewById(R.id.button_rec_start_stop);
        tvCameraStatus = (TextView)findViewById(R.id.text_camera_status);
        liveviewSurface = (LiveViewSurfaceView)findViewById(R.id.surfaceview_liveview);

        handler = new Handler();
        final BlueBellApplication application = (BlueBellApplication)getApplication();
        control = new AndroidCameraPresentationControl(this, liveviewSurface, application.getCameraDevice(), this);

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
        log.info("onResume()");
        super.onResume();
        setProgressBarIndeterminateVisibility(true);
        control.start();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onPause()
      {
        log.info("onPause()");
        super.onPause();
        control.stop();
      }

    /*******************************************************************************************************************
     *
     * Creates the selector for the shoot modes.
     *
     ******************************************************************************************************************/
    private void prepareShootModeRadioButtonsUi (final @Nonnull String[] availableShootModes,
                                                 final @Nonnull String currentMode)
      {
        rbShootModeSelector.clearCheck();
        rbShootModeSelector.removeAllViews();

        for (int i = 0; i < availableShootModes.length; i++)
          {
            final String mode = availableShootModes[i];
            final RadioButton radioBtn = new RadioButton(CameraPresentationActivity.this);
            final int viewId = 10000 + i;
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
                        if (radioInitialChecked)
                          {
                            // ignore because this callback is invoked by
                            // initializing.
                            radioInitialChecked = false;
                          }
                        else
                          {
                            final String mode = buttonView.getText().toString();
                            control.setShootMode(mode);
                          }
                      }
                  }
              });

            rbShootModeSelector.addView(radioBtn);

            if (mode.equals(currentMode))
              {
                // Set the flag true to suppress unnecessary API calling.
                radioInitialChecked = true;
                rbShootModeSelector.check(viewId);
              }
          }
      }
  }
