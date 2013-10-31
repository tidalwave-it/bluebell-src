/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile.android;

import it.tidalwave.bluebell.cameraview.impl.android.CameraViewActivity;
import it.tidalwave.sony.SsdpDiscoverer;
import it.tidalwave.sony.CameraDevice;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import it.tidalwave.bluebell.mobile.R;
import it.tidalwave.sony.CameraDevice.ApiService;
import it.tidalwave.sony.impl.DefaultSsdpDiscoverer;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/**
 * An Activity class of Device Discovery screen.
 */
@Slf4j
public class CameraDiscoveryViewActivity extends Activity {

    private Handler mHandler;
    private SsdpDiscoverer mSsdpClient;
    private DeviceListAdapter mListAdapter;
    private boolean mActivityActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_discovery);
        setProgressBarIndeterminateVisibility(false);

        mHandler = new Handler();
        mSsdpClient = new DefaultSsdpDiscoverer();
        mListAdapter = new DeviceListAdapter(this);

        log.debug("onCreate() completed.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityActive = true;
        ListView listView = (ListView) findViewById(R.id.list_device);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                CameraDevice device = (CameraDevice) listView.getAdapter()
                        .getItem(position);
                launchSampleActivity(device);
            }
        });

        findViewById(R.id.button_search).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Button btn = (Button) v;
                        if (!mSsdpClient.isSearching()) {
                            searchDevices();
                            btn.setEnabled(false);
                        }
                    }
                });

        // Show Wi-Fi SSID.
        TextView textWifiSsid = (TextView) findViewById(R.id.text_wifi_ssid);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String htmlLabel = String.format("SSID: <b>%s</b>",
                    wifiInfo.getSSID());
            textWifiSsid.setText(Html.fromHtml(htmlLabel));
        } else {
            textWifiSsid.setText(R.string.msg_wifi_disconnect);
        }

        log.debug("onResume() completed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityActive = false;
        if (mSsdpClient != null && mSsdpClient.isSearching()) {
            mSsdpClient.cancelSearching();
        }

        log.debug("onPause() completed.");
    }

    // Start searching supported devices.
    private void searchDevices() {
        mListAdapter.clearDevices();
        setProgressBarIndeterminateVisibility(true);
        mSsdpClient.search(new SsdpDiscoverer.Callback() {

            @Override
            public void onDeviceFound(final CameraDevice device) {
                // Called by non-UI thread.
                log.info(">>>> Search device found: {}", device.getFriendlyName());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.addDevice(device);
                    }
                });
            }

            @Override
            public void onFinished() {
                log.info(">>>> Search finished.");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarIndeterminateVisibility(false);
                        findViewById(R.id.button_search).setEnabled(true);
                        if (mActivityActive) {
                            Toast.makeText(CameraDiscoveryViewActivity.this,
                                    R.string.msg_device_search_finish,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onErrorFinished() {
                log.info(">>>> Search Error finished.");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarIndeterminateVisibility(false);
                        findViewById(R.id.button_search).setEnabled(true);
                        if (mActivityActive) {
                            Toast.makeText(CameraDiscoveryViewActivity.this,
                                    R.string.msg_error_device_searching,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    // Launch a SampleCameraActivity.
    private void launchSampleActivity (final @Nonnull CameraDevice device)
      {
        if (device.hasApiService("camera"))
          {
            // Go to CameraSampleActivity.
            Toast.makeText(CameraDiscoveryViewActivity.this,
                    device.getFriendlyName(), Toast.LENGTH_SHORT).show();

            // Set target ServerDevice instance to control in Activity.
            BlueBellApplication app = (BlueBellApplication) getApplication();
            app.setCameraDevice(device);
            Intent intent = new Intent(this, CameraViewActivity.class);
            startActivity(intent);
          }
        else
          {
            Toast.makeText(this, R.string.msg_error_non_supported_device,
                    Toast.LENGTH_SHORT).show();
          }
      }

    // Adapter class for DeviceList
    private static class DeviceListAdapter extends BaseAdapter {

        private List<CameraDevice> mDeviceList;
        private LayoutInflater mInflater;

        public DeviceListAdapter(Context context) {
            mDeviceList = new ArrayList<CameraDevice>();
            mInflater = LayoutInflater.from(context);
        }

        public void addDevice(CameraDevice device) {
            mDeviceList.add(device);
            notifyDataSetChanged();
        }

        public void clearDevices() {
            mDeviceList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0; // not fine
        }

        @Override
        public View getView (final int position, final View convertView, final ViewGroup parent)
          {
            TextView textView = (TextView) convertView;

            if (textView == null)
              {
                textView = (TextView)mInflater.inflate(R.layout.device_list_item, null);
              }

            CameraDevice device = (CameraDevice) getItem(position);
            log.info(">>>> found {}, services: {}", device.getFriendlyName(), device.getApiServices());
            ApiService apiService = device.getApiService("camera");
            String endpointUrl = "?";

            if (apiService != null)
              {
                endpointUrl = apiService.getEndpointUrl();
              }

            final String htmlLabel = String.format("%s ", device.getFriendlyName())
                    + String.format("<br><small>Endpoint URL:  <font color=\"blue\">%s</font></small>", endpointUrl);
            textView.setText(Html.fromHtml(htmlLabel));

            return textView;
          }
      }
}
