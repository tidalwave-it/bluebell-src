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

import it.tidalwave.sony.CameraDevice;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.SimpleSsdpClient;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author fritz
 */
@Slf4j
public class DefaultSimpleSsdpClientTest
  {
    private DefaultSimpleSsdpClient fixture;

    @BeforeMethod
    public void setupFixture()
      {
        fixture = new DefaultSimpleSsdpClient();
      }

    @Test
    public void test()
      throws InterruptedException
      {
        fixture.search(new SimpleSsdpClient.Callback()
          {
            public void onDeviceFound (final @Nonnull CameraDevice device)
              {
                log.info("onDeviceFound({})", device);
                final CameraApi remoteApi = new DefaultCameraApi(device);

                try
                  {
                    remoteApi.startRecMode();
                    remoteApi.actTakePicture();
                  }
                catch (IOException ex)
                  {
                    ex.printStackTrace();
                  }
              }

            public void onFinished()
              {
                log.info("onFinished()");
              }

            public void onErrorFinished()
              {
                log.info("onErrorFinished()");
              }
          });

        Thread.sleep(20000);
      }
  }
