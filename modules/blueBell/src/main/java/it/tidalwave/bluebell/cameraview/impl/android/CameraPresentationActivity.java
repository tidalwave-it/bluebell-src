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
import javax.annotation.Nullable;
import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import it.tidalwave.sony.CameraDescriptor;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.bluebell.cameraview.CameraPresentation;
import it.tidalwave.bluebell.cameraview.DefaultCameraPresentationControl;
import it.tidalwave.bluebell.mobile.R;
import it.tidalwave.bluebell.mobile.android.CameraDescriptorIntentHelper;
import it.tidalwave.bluebell.mobile.android.ThreadPools;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.bluebell.mobile.android.AndroidUIThreadDecoratorFactory.*;

/***********************************************************************************************************************
 *
 * @stereotype  presentation
 * 
 * An {@link Activity} that implements {@link CameraPresentation}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class CameraPresentationActivity extends Activity implements CameraPresentation
  {
    /** The controller of this presentation. */
    private DefaultCameraPresentationControl control;

    // Below are widget references
    private ImageView ivPhotoBox;

    private RadioGroup rbShootMode;

    private Button btTakePhoto;

    private Button btRecStartStop;

    private TextView tvCameraStatus;

    private LiveViewSurfaceView svLiveView;

    private boolean radioInitialChecked;
    
    private TextView tvFNumber;
    
    private TextView tvShutterSpeed;

    private TextView tvExposureCompensation;

    private TextView tvIsoSpeedRate;
    
    private TextView tvFocusMode;
    
    private TextView tvFlashMode;
    
    private TextView tvWhiteBalance;
    
    private ProgressBar pbWait;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyErrorWhileTakingPhoto()
      {
        Toast.makeText(this, R.string.msg_error_take_picture, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyRecStart()
      {
        Toast.makeText(this, R.string.msg_rec_start, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyRecStop()
      {
        Toast.makeText(this, R.string.msg_rec_stop, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyPropertyChanged (final @Nonnull String message) 
      {
        Toast.makeText(this, message, Toast.LENGTH_SHORT);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyErrorWhileRecordingMovie()
      {
        Toast.makeText(this, R.string.msg_error_api_calling, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyErrorWhileSettingProperty() 
      {
        Toast.makeText(this, "Could not set property", Toast.LENGTH_LONG); // FIXME: use a resource
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyGenericError()
      {
        Toast.makeText(this, R.string.msg_error_api_calling, Toast.LENGTH_SHORT).show();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void notifyConnectionError()
      {
        Toast.makeText(this, R.string.msg_error_connection, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, R.string.msg_error_non_supported_device, Toast.LENGTH_SHORT).show();
        finish(); 
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
        pbWait.setVisibility(View.VISIBLE);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void hideProgressBar()
      {
        pbWait.setVisibility(View.INVISIBLE);
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
    public void renderProperty (final @Nonnull CameraObserver.Property property, final @Nonnull String value) 
      {
        log.info("renderProperty({}, {})", property, value);
        // FIXME: formattation should be done by the controller
        
        switch (property) // FIXME: get rid of this switch
          {
            case F_NUMBER:
                tvFNumber.setText("F" + value);
                break;
                
            case SHUTTER_SPEED:
                tvShutterSpeed.setText(value);
                break;
                
            case EXPOSURE_COMPENSATION:
                tvExposureCompensation.setText("\u00B1" + value);
                break;
                
            case ISO_SPEED_RATE:
                tvIsoSpeedRate.setText("ISO " + value);
                break;

            case FOCUS_MODE:
                tvFocusMode.setText(value);
                break;
                
            case FLASH_MODE:
                tvFlashMode.setText("flash " + value);
                break;
                
            case WHITE_BALANCE:
                tvWhiteBalance.setText(value.equals("Auto WB") ? "AWB" : value);
                break;                
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void editProperty (final @Nonnull String value,
                              final @Nonnull List<String> values,
                              final EditCallback callback)
      {
        final int index = values.indexOf(value);
        // TODO: sanity check
        final Dialog dialog = new Dialog(this);
        final LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_edit_property_with_slider, 
                                             (ViewGroup)findViewById(R.id.la_root));
        dialog.setContentView(layout);        
        final SeekBar sbValue = (SeekBar)dialog.findViewById(R.id.sl_value);
        final TextView tvValue = (TextView)dialog.findViewById(R.id.tvValue);
        tvValue.setText(value);
        sbValue.setMax(values.size() - 1);
        sbValue.setProgress(index);
        
        sbValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
          {
            private String currentValue = "";
            
            @Override
            public void onProgressChanged (final @Nonnull SeekBar seekBar, int progress, boolean fromUser)
              {
                currentValue = values.get(progress);
                tvValue.setText(currentValue);
              }

            @Override
            public void onStartTrackingTouch (final @Nonnull SeekBar seekBar) 
              {
              }

            @Override
            public void onStopTrackingTouch (final @Nonnull SeekBar seekBar) 
              {
                callback.setValue(currentValue);
              }
          });

        tvValue.setOnClickListener(new View.OnClickListener() 
          {
            @Override
            public void onClick (final @Nonnull View view) 
              {
                dialog.hide();
              }
          });
        dialog.show();
      }
    
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onTakePhotoClickedClick (final @Nonnull View view)
      {
        control.takeAndFetchPicture();
      }

    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onRecStartStopClicked (final @Nonnull View view)
      {
        control.startOrStopMovieRecording();
      }
    
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onExitButtonClicked (final @Nonnull View view)
      {
        finish(); // TODO: should pass through the controller
      }
    
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onPhotoViewClicked (final @Nonnull View view)
      {
        ivPhotoBox.setVisibility(View.INVISIBLE);
      }
            
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onFNumberClicked (final @Nonnull View view)
      {
        control.editProperty(CameraObserver.Property.F_NUMBER);
      }
    
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onShutterSpeedClicked (final @Nonnull View view)
      {
        control.editProperty(CameraObserver.Property.SHUTTER_SPEED);
      }
    
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onIsoSpeedRateClicked (final @Nonnull View view)
      {
        control.editProperty(CameraObserver.Property.ISO_SPEED_RATE);
      }
    
    /*******************************************************************************************************************
     *
     * Button callback.
     *
     ******************************************************************************************************************/
    public void onFocusModeClicked (final @Nonnull View view)
      {
        control.editProperty(CameraObserver.Property.FOCUS_MODE);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void onCreate (final @Nullable Bundle savedInstanceState)
      {
        log.info("onCreate({})", savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_presentation);

        ivPhotoBox = (ImageView)findViewById(R.id.iv_photo_box);
        rbShootMode = (RadioGroup)findViewById(R.id.rg_shoot_mode);
        btTakePhoto = (Button)findViewById(R.id.bt_take_photo);
        btRecStartStop = (Button)findViewById(R.id.bt_rec_start_stop);
        tvCameraStatus = (TextView)findViewById(R.id.tv_camera_status);
        svLiveView = (LiveViewSurfaceView)findViewById(R.id.sv_live_view);
        tvFNumber = (TextView)findViewById(R.id.tv_f_number);
        tvShutterSpeed = (TextView)findViewById(R.id.tv_shutter_speed);
        tvExposureCompensation = (TextView)findViewById(R.id.tv_exposure_compensation);
        tvIsoSpeedRate = (TextView)findViewById(R.id.tv_iso_speed_rate);
        tvFocusMode = (TextView)findViewById(R.id.tv_focus_mode);
        tvFlashMode = (TextView)findViewById(R.id.tv_flash_mode);
        tvWhiteBalance = (TextView)findViewById(R.id.tv_white_balance);
        pbWait = (ProgressBar)findViewById(R.id.pb_wait);

        final CameraDescriptorIntentHelper intentHelper = new CameraDescriptorIntentHelper(getIntent());
        final CameraDescriptor cameraDescriptor = intentHelper.getCameraDescriptor();
        control = new AndroidCameraPresentationControl(createUIThreadDecorator(this, CameraPresentation.class),
                                                       this,
                                                       svLiveView,
                                                       cameraDescriptor,
                                                       ThreadPools.getInstance());
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
        pbWait.setVisibility(View.INVISIBLE);
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
     * FIXME: this code has been inherited from the original Sony example, but I frankly don't understand completely
     * what's its meaning - it should be used to enable movie recording, but it doesn't work on my NEX-6 camera.
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
