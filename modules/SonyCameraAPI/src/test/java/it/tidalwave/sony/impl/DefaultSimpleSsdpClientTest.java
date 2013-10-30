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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.SsdpDiscoverer;
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

    @Test(timeOut = 30000)
    public void test()
      throws Exception
      {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<CameraApi> cameraApiHolder = new AtomicReference<CameraApi>();

        fixture.search(new SsdpDiscoverer.Callback()
          {
            public void onDeviceFound (final @Nonnull CameraDevice device)
              {
                log.info("onDeviceFound({})", device);
                cameraApiHolder.set(new DefaultCameraApi(device));
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
        final CameraApi cameraApi = cameraApiHolder.get();
        assertThat(cameraApi, is(notNullValue()));

        for (int i = 0; i < 10; i++)
          {
            try
              {
                final CameraApi.EventResponse event = cameraApi.getEvent(i == 0);
                log.info("Event: {}", event);
                log.info(">>>> available APIs: {}", event.getAvailableApiList());
                log.info(">>>> camera status:  {}", event.getCameraStatus());
                log.info(">>>> shoot mode:     {}", event.getShootMode());
              }
            catch (RuntimeException e)
              {
                e.printStackTrace();
              }
          }
//        cameraApi.startRecMode();
//        cameraApi.actTakePicture();
      }
  }
