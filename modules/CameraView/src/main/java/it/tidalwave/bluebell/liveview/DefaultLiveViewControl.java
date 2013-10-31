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
package it.tidalwave.bluebell.liveview;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.SimpleLiveviewSlicer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultLiveViewControl implements LiveViewControl
  {
    @Nonnull
    private final CameraApi cameraApi;

    @Nonnull
    private final LiveView view;

    private SimpleLiveviewSlicer slicer;

    private volatile boolean running;

    @Override
    public void start()
      {
        view.start();

        // A thread for retrieving liveview data from server.
        new Thread()
          {
            @Override
            public void run()
              {
                log.info("Starting liveView producer thread...");

                try
                  {
                    final URL liveViewUrl = cameraApi.startLiveview().getUrl();
                    slicer = new SimpleLiveviewSlicer();
                    slicer.open(liveViewUrl);
                    running = true;

                    while (running)
                      {
                        final SimpleLiveviewSlicer.Payload payload = slicer.nextPayload();

                        if (payload == null)
                          { // never occurs
                            log.warn("Liveview Payload is null.");
                            continue;
                          }

                        view.postPayload(payload);
                      }
                  }
                catch (IOException e)
                  {
                    log.warn("IOException while fetching: ", e);
                  }
                catch (RuntimeException e)
                  {
                    log.warn("JSONException while fetching", e);
                  }
                finally
                  {
                    try
                      {
                        if (slicer != null)
                          {
                            slicer.close();
                            slicer = null;
                          }

                        cameraApi.stopLiveview();
                      }
                    catch (IOException e)
                      {
                        log.warn("IOException while closing slicer: ", e);
                      }

                    view.stop();
                    running = false;
                  }
              }
          }.start();
      }

    @Override
    public void stop()
      {
        running = false; // let the view be terminated by the thread
      }
  }
