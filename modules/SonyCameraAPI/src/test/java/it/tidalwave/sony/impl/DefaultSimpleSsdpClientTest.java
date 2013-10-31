/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
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
package it.tidalwave.sony.impl;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.SimpleLiveviewSlicer;
import it.tidalwave.sony.SsdpDiscoverer;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultSimpleSsdpClientTest
  {
    private DefaultSsdpDiscoverer fixture;

    @BeforeMethod
    public void setupFixture()
      {
        fixture = new DefaultSsdpDiscoverer();
      }

    @Test(timeOut = 60000)
    public void test()
      throws Exception
      {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<CameraDevice> deviceHolder = new AtomicReference<CameraDevice>();

        fixture.search(new SsdpDiscoverer.Callback()
          {
            public void onDeviceFound (final @Nonnull CameraDevice device)
              {
                log.info("onDeviceFound({})", device);
                deviceHolder.set(device);
                latch.countDown();
              }

            public void onFinished()
              {
                log.info("onFinished()");
                latch.countDown();
              }

            public void onErrorFinished()
              {
                log.info("onErrorFinished()");
                latch.countDown();
              }
          });

        latch.await();
        final CameraDevice device = deviceHolder.get();
        assertThat(device, is(notNullValue()));
        final CameraApi cameraApi = device.getApi();

        final CameraObserver observer = device.getObserver();
        observer.setListener(new CameraObserver.ChangeListener()
          {
            public void onApisChanged (final @Nonnull Set<String> apis)
              {
                log.info("APIs changed: {}", apis);
              }

            public void onStatusChanged (final @Nonnull String status)
              {
                log.info("status changed: {}", status);
              }

            public void onShootModeChanged (final @Nonnull String shootMode)
              {
                log.info("shoot mode changed: {}", shootMode);
              }
          });

        observer.start();
        Thread.sleep(5000);
        cameraApi.startRecMode();
        final URL url = cameraApi.startLiveview().getUrl();
        log.info("LiveView URL: {}", url);
        final SimpleLiveviewSlicer slicer = new SimpleLiveviewSlicer();
        slicer.open(url);

        for (int i = 0; i < 100; i++)
          {
            log.info("payload: {}", slicer.nextPayload());
          }
//        Thread.sleep(30000);

//        cameraApi.actTakePicture();
      }
  }
