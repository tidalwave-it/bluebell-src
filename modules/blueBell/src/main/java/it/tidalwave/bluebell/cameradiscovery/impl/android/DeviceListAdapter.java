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
package it.tidalwave.bluebell.cameradiscovery.impl.android;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import it.tidalwave.bluebell.mobile.R;
import it.tidalwave.sony.CameraDevice;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Adapter class for DeviceList
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/

@Slf4j
class DeviceListAdapter extends BaseAdapter
  {
    private List<CameraDevice> mDeviceList;
    private LayoutInflater mInflater;

    public DeviceListAdapter (Context context)
      {
        mDeviceList = new ArrayList<CameraDevice>();
        mInflater = LayoutInflater.from(context);
      }

    public void addDevice (CameraDevice device)
      {
        mDeviceList.add(device);
        notifyDataSetChanged();
      }

    public void clearDevices()
      {
        mDeviceList.clear();
        notifyDataSetChanged();
      }

    @Override
    public int getCount()
      {
        return mDeviceList.size();
      }

    @Override
    public Object getItem (int position)
      {
        return mDeviceList.get(position);
      }

    @Override
    public long getItemId (int position)
      {
        return 0; // not fine
      }

    @Override
    public View getView (final int position, final View convertView, final ViewGroup parent)
      {
        TextView textView = (TextView) convertView;

        if (textView == null)
          {
            textView = (TextView) mInflater.inflate(R.layout.device_list_item, null);
          }

        CameraDevice device = (CameraDevice) getItem(position);
        log.info(">>>> found {}, services: {}", device.getFriendlyName(), device.getApiServices());
        CameraDevice.ApiService apiService = device.getApiService("camera");
        String endpointUrl = "?";

        if (apiService != null)
          {
            endpointUrl = apiService.getEndpointUrl();
          }

        final String htmlLabel = String.format("%s ", device.getFriendlyName()) + String.format("<br><small>Endpoint URL:  <font color=\"blue\">%s</font></small>", endpointUrl);
        textView.setText(Html.fromHtml(htmlLabel));

        return textView;
      }
  }
