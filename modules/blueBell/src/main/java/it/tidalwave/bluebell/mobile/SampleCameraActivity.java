/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraDevice;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import lombok.extern.slf4j.Slf4j;

/**
 * An Activity class of Sample Camera screen.
 */
@Slf4j
public class SampleCameraActivity extends Activity
  {
//    private static final String TAG = SampleCameraActivity.class.getSimpleName();

    private Handler mHandler;
    private ImageView mImagePictureWipe;
    private RadioGroup mRadiosShootMode;
    private Button mButtonTakePicture;
    private Button mButtonRecStartStop;
    private TextView mTextCameraStatus;

    private CameraDevice cameraDevice;

    private CameraApi cameraApi;

    private CameraObserver cameraObserver;

    private SimpleLiveviewSurfaceView mLiveviewSurface;

    private final Set<String> availableApis = new TreeSet<String>();

    private boolean mRadioInitialChecked;

    @Override
    protected void onCreate (Bundle savedInstanceState)
      {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_sample_camera);

        mHandler = new Handler();
        final SampleApplication app = (SampleApplication)getApplication();
        cameraDevice = app.getTargetServerDevice();
        cameraApi = cameraDevice.getApi();
        cameraObserver = cameraDevice.getObserver();

        mImagePictureWipe = (ImageView) findViewById(R.id.image_picture_wipe);
        mRadiosShootMode = (RadioGroup) findViewById(R.id.radio_group_shoot_mode);
        mButtonTakePicture = (Button) findViewById(R.id.button_take_picture);
        mButtonRecStartStop = (Button) findViewById(R.id.button_rec_start_stop);
        mTextCameraStatus = (TextView) findViewById(R.id.text_camera_status);
        mLiveviewSurface = (SimpleLiveviewSurfaceView) findViewById(R.id.surfaceview_liveview);
        mLiveviewSurface.bindRemoteApi(cameraApi);

        log.info("onCreate() completed.");
      }

    @Override
    protected void onResume()
      {
        super.onResume();

        mButtonTakePicture.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v)
              {
                takeAndFetchPicture();
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

        cameraObserver.setListener(new CameraObserver.ChangeListener()
          {
            @Override
            public void onShootModeChanged (final @Nonnull String shootMode)
              {
                log.info("onShootModeChanged() called: {}", shootMode);
                mHandler.post(new Runnable()
                  {
                    @Override
                    public void run()
                      {
                        refreshUi();
                      }
                  });
              }

            @Override
            public void onStatusChanged (final @Nonnull String status)
              {
                log.info("onCameraStatusChanged() called: {}", status);
                mHandler.post(new Runnable()
                  {
                    @Override
                    public void run()
                      {
                        refreshUi();
                      }
                  });
              }

            @Override
            public void onApisChanged (final @Nonnull List<String> apis)
              {
                log.info("onApiListModified() called");
                synchronized (availableApis)
                  {
                    availableApis.clear();

                    for (final String api : apis)
                      {
                        availableApis.add(api);
                      }
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

        openConnection();

        log.info("onResume() completed.");
      }

    @Override
    protected void onPause()
      {
        super.onPause();
        closeConnection();

        log.info("onPause() completed.");
      }

    // Open connection to the camera device to start monitoring Camera events
    // and showing liveview.
    private void openConnection()
      {
        setProgressBarIndeterminateVisibility(true);
        new Thread()
          {
            @Override
            public void run()
              {
                log.info("openConnection(): exec.");

                try
                  {
                    JSONObject replyJson = cameraApi.getAvailableApiList().getJsonObject();
                    loadAvailableApiList(replyJson);

                    // check version of the server device
                    if (isApiAvailable("getApplicationInfo"))
                      {
                        log.info("openConnection(): getApplicationInfo()");
                        replyJson = cameraApi.getApplicationInfo().getJsonObject();

                        if (!isSupportedServerVersion(replyJson))
                          {
                            toast(R.string.msg_error_non_supported_device);
                            SampleCameraActivity.this.finish();
                            return;
                          }
                      }
                    else
                      {
                        // never happens;
                        return;
                      }

                    // startRecMode if necessary.
                    if (isApiAvailable("startRecMode"))
                      {
                        log.info("openConnection(): startRecMode()");
                        cameraApi.startRecMode().getJsonObject();

                        // Call again.
                        replyJson = cameraApi.getAvailableApiList().getJsonObject();
                        loadAvailableApiList(replyJson);
                      }

                    // getEvent start
                    if (isApiAvailable("getEvent"))
                      {
                        log.info("openConnection(): EventObserver.start()");
                        cameraObserver.start();
                      }

                    // Liveview start
                    if (isApiAvailable("startLiveview"))
                      {
                        log.info("openConnection(): LiveviewSurface.start()");
                        mLiveviewSurface.start();
                      }

                    // prepare UIs
                    if (isApiAvailable("getAvailableShootMode"))
                      {
                        log.info("openConnection(): prepareShootModeRadioButtons()");
                        prepareShootModeRadioButtons();
                        // Note: hide progress bar on title after this calling.
                      }

                    log.info("openConnection(): completed.");
                  }
                catch (IOException e)
                  {
                    log.warn("openConnection: IOException: ", e);
                    setProgressIndicator(false);
                    toast(R.string.msg_error_connection);
                  }
              }
          }.start();
      }

    // Close connection to stop monitoring Camera events and showing liveview.
    private void closeConnection() {
        new Thread() {

            @Override
            public void run() {
                log.info("closeConnection(): exec.");
                try {
                    // Liveview stop
                    log.info("closeConnection(): LiveviewSurface.stop()");
                    mLiveviewSurface.stop();

                    // getEvent stop
                    log.info("closeConnection(): EventObserver.stop()");
                    cameraObserver.stop();

                    // stopRecMode if necessary.
                    if (isApiAvailable("stopRecMode")) {
                        log.info("closeConnection(): stopRecMode()");
                        cameraApi.stopRecMode();
                    }

                    log.info("closeConnection(): completed.");
                } catch (IOException e) {
                    log.warn("closeConnection: IOException: ", e);
                }
            }
        }.start();
    }

    // Refresh UI appearance along current "cameraStatus" and "shootMode".
    private void refreshUi()
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

        // Take picture Button
        if ("still".equals(shootMode) && "IDLE".equals(cameraStatus))
          {
            mButtonTakePicture.setEnabled(true);
          }
        else
          {
            mButtonTakePicture.setEnabled(false);
          }

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

    // Retrieve a list of APIs that are available at present.
    private void loadAvailableApiList (final JSONObject replyJson)
      {
        synchronized (availableApis)
          {
            availableApis.clear();

            try
              {
                final JSONArray resultArrayJson = replyJson.getJSONArray("result");
                final JSONArray apiListJson = resultArrayJson.getJSONArray(0);

                for (int i = 0; i < apiListJson.length(); i++)
                  {
                    availableApis.add(apiListJson.getString(i));
                  }

                log.info(">>>> available APIs: {}", availableApis);
              }
            catch (JSONException e)
              {
                log.warn("loadAvailableApiList: JSON format error.");
              }
          }
      }

    // Check if the indicated API is available at present.
    private boolean isApiAvailable(String apiName) {
        boolean isAvailable = false;
        synchronized (availableApis) {
            isAvailable = availableApis.contains(apiName);
        }
        return isAvailable;
    }

    // Check if the version of the server is supported in this application.
    private boolean isSupportedServerVersion(JSONObject replyJson) {
        try {
            JSONArray resultArrayJson = replyJson.getJSONArray("result");
            String version = resultArrayJson.getString(1);
            String[] separated = version.split("\\.");
            int major = Integer.valueOf(separated[0]);
            if (2 <= major) {
                return true;
            }
        } catch (JSONException e) {
            log.warn("isSupportedServerVersion: JSON format error.");
        } catch (NumberFormatException e) {
            log.warn("isSupportedServerVersion: Number format error.");
        }
        return false;
    }

    // Prepare for RadioButton to select "shootMode" by user.
    private void prepareShootModeRadioButtons()
      {
        new Thread()
          {
            @Override
            public void run()
              {
                log.info("prepareShootModeRadioButtons(): exec.");

                try
                  {
                    final JSONObject replyJson = cameraApi.getAvailableShootMode().getJsonObject();
                    final JSONArray resultsObj = replyJson.getJSONArray("result");
                    final String currentMode = resultsObj.getString(0);
                    final JSONArray availableModesJson = resultsObj.getJSONArray(1);
                    final ArrayList<String> availableModes = new ArrayList<String>();

                    for (int i = 0; i < availableModesJson.length(); i++)
                      {
                        final String mode = availableModesJson.getString(i);

                        if (!"still".equals(mode) && !"movie".equals(mode))
                          {
                            continue;
                          }

                        availableModes.add(mode);
                      }

                    mHandler.post(new Runnable()
                      {
                        @Override
                        public void run()
                          {
                            prepareShootModeRadioButtonsUi(availableModes.toArray(new String[0]), currentMode);
                            // Hide progress indeterminate on title bar.
                            setProgressBarIndeterminateVisibility(false);
                        }
                      });
                  }
                catch (IOException e)
                  {
                    log.warn("prepareShootModeRadioButtons: IOException: ", e);
                  }
                catch (JSONException e)
                  {
                    log.warn("prepareShootModeRadioButtons: JSON format error.");
                  }
              };
          }.start();
      }

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
                        toast(R.string.msg_error_api_calling);
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

    // Take a picture and retrieve the image data.
    private void takeAndFetchPicture() {
        if (!mLiveviewSurface.isStarted()) {
            toast(R.string.msg_error_take_picture);
            return;
        }

        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = cameraApi.actTakePicture().getJsonObject();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                    String postImageUrl = null;
                    if (1 <= imageUrlsObj.length()) {
                        postImageUrl = imageUrlsObj.getString(0);
                    }
                    if (postImageUrl == null) {
                        log.warn("takeAndFetchPicture: post image URL is null.");
                        toast(R.string.msg_error_take_picture);
                        return;
                    }
                    setProgressIndicator(true); // Show progress indicator
                    URL url = new URL(postImageUrl);
                    InputStream istream = new BufferedInputStream(
                            url.openStream());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4; // irresponsible value
                    final Drawable pictureDrawable = new BitmapDrawable(
                            getResources(), BitmapFactory.decodeStream(istream,
                                    null, options));
                    istream.close();
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mImagePictureWipe.setVisibility(View.VISIBLE);
                            mImagePictureWipe.setImageDrawable(pictureDrawable);
                        }
                    });

                } catch (IOException e) {
                    log.warn("IOException while closing slicer: ", e);
                    toast(R.string.msg_error_take_picture);
                } catch (JSONException e) {
                    log.warn("JSONException while closing slicer");
                    toast(R.string.msg_error_take_picture);
                } finally {
                    setProgressIndicator(false);
                }
            }
        }.start();
    }

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
                    if (resultCode == 0) {
                        toast(R.string.msg_rec_start);
                    } else {
                        log.warn("startMovieRec: error: {}", resultCode);
                        toast(R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    log.warn("startMovieRec: IOException: ", e);
                } catch (JSONException e) {
                    log.warn("startMovieRec: JSON format error.");
                }
            }
        }.start();
    }

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
                    if (thumbnailUrl != null) {
                        toast(R.string.msg_rec_stop);
                    } else {
                        log.warn("stopMovieRec: error");
                        toast(R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    log.warn("stopMovieRec: IOException: ", e);
                } catch (JSONException e) {
                    log.warn("stopMovieRec: JSON format error.");
                }
            }
        }.start();
    }

    // Show or hide progress indicator on title bar
    private void setProgressIndicator(final boolean visible) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(visible);
            }
        });
    }

    // show toast
    private void toast(final int msgId) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(SampleCameraActivity.this, msgId,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
