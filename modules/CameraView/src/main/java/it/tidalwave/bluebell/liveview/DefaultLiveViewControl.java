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
package it.tidalwave.bluebell.liveview;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.SimpleLiveviewSlicer;
import it.tidalwave.sony.SimpleLiveviewSlicer.Payload;
import lombok.Getter;
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

    private final SimpleLiveviewSlicer slicer = new SimpleLiveviewSlicer();

    @Getter
    private volatile boolean running;

    @Override
    public void start()
      {
        log.info("start()");
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
                    slicer.open(cameraApi.startLiveview().getUrl());
                    running = true;

                    while (running)
                      {
                        final Payload payload = slicer.readNextPayload();

                        if (!payload.isEmpty())
                          {
                            view.postPayload(payload);
                          }
                      }
                  }
                catch (Exception e)
                  {
                    log.warn("While reading liveView", e);
                  }
                finally
                  {
                    running = false;

                    try
                      {
                        slicer.close();
                      }
                    catch (IOException e)
                      {
                        log.warn("While closing slicer", e);
                      }

                    try
                      {
                        cameraApi.stopLiveview();
                      }
                    catch (IOException e)
                      {
                        log.warn("While stopping liveView", e);
                      }

                    view.stop();
                  }
              }
          }.start();
      }

    @Override
    public void stop()
      {
        log.info("stop()");
        running = false; // let the view be terminated by the thread
        // FIXME: wait for the thread? interrupt?
      }
  }
