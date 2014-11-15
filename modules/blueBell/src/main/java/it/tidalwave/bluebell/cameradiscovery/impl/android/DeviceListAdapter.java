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
package it.tidalwave.bluebell.cameradiscovery.impl.android;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import it.tidalwave.bluebell.mobile.R;
import it.tidalwave.sony.CameraDescriptor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * An adapter for {@link ListView} that contains the discovered devices.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
class DeviceListAdapter extends BaseAdapter
  {
    private final List<CameraDescriptor> cameraDescriptors;

    private final LayoutInflater inflater;

    public DeviceListAdapter (final @Nonnull Context context,
                              final @Nonnull List<CameraDescriptor> cameraDescriptors)
      {
        this.cameraDescriptors = cameraDescriptors;
        inflater = LayoutInflater.from(context);
      }

    @Override
    public int getCount()
      {
        return cameraDescriptors.size();
      }

    @Override
    public Object getItem (final int position)
      {
        return cameraDescriptors.get(position);
      }

    @Override
    public long getItemId (final int position)
      {
        return position;
      }

    @Override
    public View getView (final int position, final @CheckForNull View convertView, final @Nonnull ViewGroup parent)
      {
        final ViewGroup layout = createLayoutIfItDoesntExist(convertView);

        final CameraDescriptor cameraDescriptor = cameraDescriptors.get(position);
        final String mainLabel = String.format("%s %s", cameraDescriptor.getModelName(),
                                                        cameraDescriptor.getFriendlyName());
        final TextView tvDeviceName = (TextView)layout.findViewById(R.id.tv_device_name);
        final TextView tvDeviceAddress = (TextView)layout.findViewById(R.id.tv_device_address);
        tvDeviceName.setText(mainLabel);
        tvDeviceAddress.setText(cameraDescriptor.getIpAddress());

        return layout;
      }

    @Nonnull
    protected ViewGroup createLayoutIfItDoesntExist (final @CheckForNull View convertView) 
      {
        return (convertView != null) ? (ViewGroup)convertView 
                                     : (ViewGroup)inflater.inflate(R.layout.device_list_item, null);
    }
  }
