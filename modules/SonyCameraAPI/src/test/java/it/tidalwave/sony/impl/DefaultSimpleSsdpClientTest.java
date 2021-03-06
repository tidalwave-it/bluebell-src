/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
 * %%
 * Copyright (C) 2013 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.net.URL;
import it.tidalwave.sony.CameraDescriptor;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.SimpleLiveviewSlicer;
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
        fixture = new DefaultSsdpDiscoverer(Executors.newFixedThreadPool(4));
      }

    @Test(timeOut = 60000)
    public void test()
      throws Exception
      {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<CameraDescriptor> deviceHolder = new AtomicReference<>();

        fixture.search(new SsdpDiscoverer.Callback()
          {
            @Override
            public void onDeviceFound (final @Nonnull CameraDescriptor device)
              {
                log.info("onDeviceFound({})", device);
                deviceHolder.set(device);
                latch.countDown();
              }

            @Override
            public void onFinished()
              {
                log.info("onFinished()");
                latch.countDown();
              }

            @Override
            public void onErrorFinished()
              {
                log.info("onErrorFinished()");
                latch.countDown();
              }
          });

        latch.await();
        final CameraDescriptor cameraDescriptor = deviceHolder.get();
        assertThat(cameraDescriptor, is(notNullValue()));
        final CameraDevice service = cameraDescriptor.createDevice();
        assertThat(service, is(notNullValue()));
        final CameraApi cameraApi = service.getApi();
        final CameraObserver observer = service.getObserver();
        
        observer.setListener(new CameraObserver.ChangeListener()
          {
            @Override
            public void onApisChanged (final @Nonnull Set<String> apis,
                                       final @Nonnull Set<String> addedApis, 
                                       final @Nonnull Set<String> removedApis)
              {
                log.info("APIs changed: all: {} added: {} removed: {}", apis, addedApis, removedApis);
              }

            @Override
            public void onPropertyChanged (final CameraObserver.Property property, final String value) 
              {
                log.info("property changed: {}={}", property, value);
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
            log.info("payload: {}", slicer.readNextPayload());
          }
//        Thread.sleep(30000);

//        cameraApi.actTakePicture();
      }
  }
