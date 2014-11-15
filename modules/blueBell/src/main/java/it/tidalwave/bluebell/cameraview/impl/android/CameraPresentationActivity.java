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
import java.util.List;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import it.tidalwave.sony.CameraDeviceDescriptor;
import it.tidalwave.bluebell.cameraview.CameraPresentation;
import it.tidalwave.bluebell.cameraview.DefaultCameraPresentationControl;
import it.tidalwave.bluebell.mobile.R;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.bluebell.mobile.android.AndroidUIThreadDecoratorFactory.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class CameraPresentationActivity extends Activity implements CameraPresentation
  {
    private ImageView ivPhotoBox;

    private RadioGroup rbShootMode;

    private Button btTakePhoto;

    private Button btRecStartStop;

    private TextView tvCameraStatus;

    private LiveViewSurfaceView svLiveView;

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
        Toast.makeText(CameraPresentationActivity.this, R.string.msg_error_connection, Toast.LENGTH_SHORT).show();
        setProgressBarIndeterminateVisibility(false); // FIXME: split
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyDeviceNotSupportedAndQuit()
      {
        Toast.makeText(CameraPresentationActivity.this, 
                       R.string.msg_error_non_supported_device,
                       Toast.LENGTH_SHORT).show();
        finish(); // FIXME: split
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setShootModeControl (final @Nonnull List<String> availableModes, final @Nonnull String currentMode)
      {
        prepareShootModeRadioButtonsUi(availableModes.toArray(new String[0]), currentMode);
        setProgressBarIndeterminateVisibility(false); // FIXME: split?
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void showProgressBar()
      {
        setProgressBarIndeterminateVisibility(true);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void hideProgressBar()
      {
        setProgressBarIndeterminateVisibility(false);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyErrorWhileTakingPhoto()
      {
        Toast.makeText(CameraPresentationActivity.this,
                       R.string.msg_error_take_picture, 
                       Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderCameraStatus (final @Nonnull String cameraStatus)
      {
        tvCameraStatus.setText(cameraStatus);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderRecStartStopButtonAsStop()
      {
        btRecStartStop.setEnabled(true);
        btRecStartStop.setText(R.string.button_rec_stop);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderRecStartStopButtonAsStart()
      {
        btRecStartStop.setEnabled(true);
        btRecStartStop.setText(R.string.button_rec_start);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void disableRecStartStopButton()
      {
        btRecStartStop.setEnabled(false);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void enableTakePhotoButton (final boolean enabled)
      {
        btTakePhoto.setEnabled(enabled);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void hidePhotoBox()
      {
        ivPhotoBox.setVisibility(View.INVISIBLE);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void enableShootModeSelector (final @Nonnull String shootMode)
      {
        for (int i = 0; i < rbShootMode.getChildCount(); i++)
          {
            rbShootMode.getChildAt(i).setEnabled(true);
          }

        View radioButton = rbShootMode.findViewWithTag(shootMode);

        if (radioButton != null)
          {
            rbShootMode.check(radioButton.getId());
          }
        else
          {
            rbShootMode.clearCheck();
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void disableShootModeSelector()
      {
        for (int i = 0; i < rbShootMode.getChildCount(); i++)
          {
            rbShootMode.getChildAt(i).setEnabled(false);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void showPhoto (final @Nonnull Object picture)
      {
        ivPhotoBox.setVisibility(View.VISIBLE);
        ivPhotoBox.setImageDrawable((Drawable)picture);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyRecStart()
      {
        Toast.makeText(CameraPresentationActivity.this, R.string.msg_rec_start, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyRecStop()
      {
        Toast.makeText(CameraPresentationActivity.this, R.string.msg_rec_stop, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyErrorWhileRecordingMovie()
      {
        Toast.makeText(CameraPresentationActivity.this, R.string.msg_error_api_calling, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyGenericError()
      {
        Toast.makeText(CameraPresentationActivity.this,
                       R.string.msg_error_api_calling,
                       Toast.LENGTH_SHORT).show();
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

        ivPhotoBox = (ImageView)findViewById(R.id.iv_photo_box);
        rbShootMode = (RadioGroup)findViewById(R.id.rg_shoot_mode);
        btTakePhoto = (Button)findViewById(R.id.bt_take_photo);
        btRecStartStop = (Button)findViewById(R.id.bt_rec_start_stop);
        tvCameraStatus = (TextView)findViewById(R.id.tv_camera_status);
        svLiveView = (LiveViewSurfaceView)findViewById(R.id.sv_live_view);

        final CameraDeviceDescriptor cameraDeviceDescriptor =
                (CameraDeviceDescriptor)getIntent().getSerializableExtra("cameraDeviceDescriptor");
        control = new AndroidCameraPresentationControl(createUIThreadDecorator(this, CameraPresentation.class),
                                                       this,
                                                       svLiveView, 
                                                       cameraDeviceDescriptor);

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
        rbShootMode.clearCheck();
        rbShootMode.removeAllViews();

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

            rbShootMode.addView(radioBtn);

            if (mode.equals(currentMode))
              {
                // Set the flag true to suppress unnecessary API calling.
                radioInitialChecked = true;
                rbShootMode.check(viewId);
              }
          }
      }
  }
