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
package it.tidalwave.sony.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import it.tidalwave.sony.CameraApi;
import it.tidalwave.sony.CameraApi.EventResponse;
import it.tidalwave.sony.CameraObserver;
import it.tidalwave.sony.CameraObserver.Property;
import it.tidalwave.sony.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.sony.CameraApi.Polling.*;
import java.util.TreeSet;

/***********************************************************************************************************************
 *
 * A simple observer class for some status values in Camera. This class supports only a few of values of getEvent
 * result, so please add implementation for the rest of values you want to handle.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
/* package */ class DefaultCameraObserver implements CameraObserver
  {
    @Nonnull
    private final CameraApi cameraApi;

    @CheckForNull
    private ChangeListener listener;

    @Getter
    private volatile boolean running = false;

    @Getter @Nonnull
    private String status = "";

    @Getter @Nonnull
    private String shootMode = "";
    
    private final Map<Property, String> valueMap = Collections.synchronizedMap(new EnumMap<Property, String>(Property.class));

    private final Set<String> currentApis = new TreeSet<>();
            
    /** To run background jobs. */
    @Nonnull
    private final ExecutorService executorService;
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    // FIXME: should also make the Camera API dynamic, so we don't need those adapters
    @RequiredArgsConstructor
    enum PropertyFetcher
      {
        FETCHER_F_NUMBER(Property.F_NUMBER, CameraApi.Property.F_NUMBER),
        FETCHER_SHUTTER(Property.SHUTTER_SPEED, CameraApi.Property.SHUTTER_SPEED),
        FETCHER_ISO(Property.ISO_SPEED_RATE, CameraApi.Property.ISO_SPEED_RATE),
        FETCHER_EXPOSURE_COMPENSATION(Property.EXPOSURE_COMPENSATION, CameraApi.Property.EXPOSURE_COMPENSATION),
        FETCHER_FLASH_MODE(Property.FLASH_MODE, CameraApi.Property.FLASH_MODE),
        FETCHER_FOCUS_MODE(Property.FOCUS_MODE, CameraApi.Property.FOCUS_MODE),
        FETCHER_WHITE_BALANCE(Property.WHITE_BALANCE, CameraApi.Property.WHITE_BALANCE);
        
        @Nonnull
        public String fetch (final @Nonnull EventResponse response)
          {
            return response.getProperty(apiProperty);
          }
        
        @Getter
        private final Property property;
        
        @Getter
        private final CameraApi.Property apiProperty;
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
      {
        for (final PropertyFetcher fetcher : PropertyFetcher.values())
          {
            valueMap.put(fetcher.getProperty(), "");
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean start()
      {
        if (running)
          {
            log.warn("start() already starting.");
            return false;
          }

        running = true;

        executorService.submit(new Runnable()
          {
            @Override
            public void run()
              {
                log.debug("start() exec.");
                boolean firstCall = true;

                MONITORLOOP: while (running)
                  {
                    try
                      {
                        final EventResponse response = cameraApi.getEvent(firstCall ? LONG_POLLING : SHORT_POLLING);
                        final StatusCode statusCode = response.getStatusCode();
                        firstCall = false;
                        log.debug(">>>> statusCode {}", statusCode);

                        switch (statusCode)
                          {
                            case OK:
                                break;

                            case ANY:
                            case NO_SUCH_METHOD:
                                break MONITORLOOP;

                            case TIMEOUT:
                                continue MONITORLOOP;

                            case ALREADY_POLLING:
                                try
                                  {
                                    log.warn("ALREADY_POLLING received - sleeping for 5 sec");
                                    Thread.sleep(5000);
                                  }
                                catch (InterruptedException e)
                                  {
                                    // do nothing.
                                  }
                                continue MONITORLOOP;

                            default:
                                log.warn("Unexpected error: {}", statusCode);
                                break MONITORLOOP;
                          }

                        final Set<String> apis = response.getApis();
                        final Set<String> addedApis = new TreeSet<>(apis);
                        addedApis.removeAll(currentApis);
                        final Set<String> removedApis = new TreeSet<>(currentApis);
                        removedApis.removeAll(apis);
                        
                        currentApis.clear();
                        currentApis.addAll(apis);
                                          
                        fireApisChanged(currentApis, addedApis, removedApis);

                        final String newStatus = response.getCameraStatus();
                        log.debug("getEvent status: {}", newStatus);

                        if (!status.equals(newStatus))
                          {
                            status = newStatus;
                            fireStatusChanged(status);
                          }

                        final String newShootMode = response.getShootMode();
                        log.debug("getEvent shootMode: {}", newShootMode);

                        if (!shootMode.equals(newShootMode))
                          {
                            shootMode = newShootMode;
                            fireShootModeChanged(shootMode);
                          }
                        
                        for (final PropertyFetcher fetcher : PropertyFetcher.values())
                          {
                            final Property property = fetcher.getProperty();
                            final String oldValue = valueMap.get(property);
                            final String newValue = fetcher.fetch(response);
                            
                            if (!oldValue.equals(newValue))
                              {
                                valueMap.put(property, newValue);
                                firePropertyChanged(property, newValue);
                              }
                          }
                      }
                    catch (IOException e) // Occurs when the server is not available now.
                      {
                        log.debug("getEvent timeout by client trigger.");
                        break MONITORLOOP;
                      }
                    catch (RuntimeException e)
                      {
                        log.warn("getEvent: JSON format error. ", e);
                        break MONITORLOOP;
                      }
                  }

                running = false;
              }
          });

        return true;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void stop()
      {
        running = false;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setListener (final @Nonnull ChangeListener listener)
      {
        this.listener = listener;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void unsetListener()
      {
        listener = null;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getProperty (final @Nonnull Property property)
      {
        return valueMap.get(property);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setProperty (final @Nonnull Property property, final @Nonnull String value) 
      throws IOException
      {
        for (final PropertyFetcher p : PropertyFetcher.values())
          {
            if (p.getProperty().equals(property))
              {
                cameraApi.setProperty(p.getApiProperty(), value);
                break;
              }
          }
        
        throw new IllegalArgumentException("Cannot set property " + property);
      }
    
    private static final List<String> F_VALUES = Arrays.asList("4.0","4.5","5.0","5.6","6.3","7.1","8.0","9.0","10","11","13","14","16","18","20","22");
    private static final List<String> SHUTTER_VALUES = Arrays.asList("30\"","25\"","20\"","15\"","13\"","10\"","8\"","6\"","5\"","4\"","3.2\"","2.5\"","2\"","1.6\"","1.3\"","1\"","0.8\"","0.6\"","0.5\"","0.4\"","1/3","1/4","1/5","1/6","1/8","1/10","1/13","1/15","1/20","1/25","1/30","1/40","1/50","1/60","1/80","1/100","1/125","1/160","1/200","1/250","1/320","1/400","1/500","1/640","1/800","1/1000","1/1250","1/1600","1/2000","1/2500","1/3200","1/4000");
    private static final List<String> ISO_VALUES = Arrays.asList("100","200","400","800","1600","3200","6400","12800","25600");
    private static final List<String> FOCUS_MODE_VALUES = Arrays.asList("AF-S","AF-C","DMF","MF");
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<String> getPropertyFeasibleValues (final @Nonnull Property property)
      {
        // FIXME: retrieve valid values from the camera
        switch (property)
          {
            case F_NUMBER:
                return F_VALUES;
            
            case SHUTTER_SPEED:
                return SHUTTER_VALUES;
                
            case ISO_SPEED_RATE:
                return ISO_VALUES;
                
            case FOCUS_MODE:
                return FOCUS_MODE_VALUES;
                
            default:
                throw new IllegalArgumentException("Cannot get feasible values for " + property);
          }
        // END FIXME
      }
    
    /*******************************************************************************************************************
     *
     * Notifies the listener of available APIs change.
     *
     ******************************************************************************************************************/
    private void fireApisChanged (final @Nonnull Set<String> apis,
                                  final @Nonnull Set<String> addedApis, 
                                  final @Nonnull Set<String> removedApis)
      {
        if (listener != null)
          {
            listener.onApisChanged(apis, addedApis, removedApis);
          }
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of Camera Status change.
     *
     ******************************************************************************************************************/
    private void fireStatusChanged (final @Nonnull String status)
      {
        if (listener != null)
          {
            listener.onStatusChanged(status);
          }
      }

    /*******************************************************************************************************************
     *
     * Notifies the listener of Shoot Mode change.
     *
     ******************************************************************************************************************/
    private void fireShootModeChanged (final @Nonnull String shootMode)
      {
        if (listener != null)
          {
            listener.onShootModeChanged(shootMode);
          }
      }
    
    private void firePropertyChanged (final @Nonnull Property property, final String value)
      {
        if (listener != null)
          {
            listener.onPropertyChanged(property, value);
          }
      }
  }
