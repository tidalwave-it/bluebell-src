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
 * A default implementation of {@link LiveViewPresentationControl}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultLiveViewPresentationControl implements LiveViewPresentationControl
  {
    @Nonnull
    private final CameraApi cameraApi;

    @Nonnull
    private final LiveViewPresentation presentation;

    private final SimpleLiveviewSlicer slicer = new SimpleLiveviewSlicer();

    @Getter
    private volatile boolean running;
    
    private Thread liveViewThread;

    /*******************************************************************************************************************
     * 
     * This Runnable consumes incoming images to be rendered in the live view.
     * In Android, it could be implemented by means of Handler, since it's basically a message-consuming loop. It's 
     * implemented like this to be independent of Android.
     * 
     ******************************************************************************************************************/
    private final Runnable liveViewConsumer = new Runnable()
      {
        @Override
        public void run()
          {
            log.info("Starting liveView producer thread...");

            try
              {
                slicer.open(cameraApi.startLiveview().getUrl());

                while (running)
                  {
                    final Payload payload = slicer.readNextPayload();

                    if (!payload.isEmpty())
                      {
                        presentation.postPayload(payload);
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

                presentation.stop();
              }
          }
      };

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override
    public void start()
      {
        log.info("start()");
        presentation.start();

        if (liveViewThread == null) 
          {
            running = true;
            liveViewThread = new Thread(liveViewConsumer); // FIXME: use an ExecutorService
            liveViewThread.start();
          }
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        log.info("stop()");
        
        if (liveViewThread != null)
          {
            running = false; 
            liveViewThread.interrupt();
            
            try
              {
                liveViewThread.join();
              } 
            catch (InterruptedException e) 
              {
                log.warn("", e);
              }
            
            liveViewThread = null;
          }
      }
  }
