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
package it.tidalwave.bluebell.mobile.android;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import android.os.Handler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AndroidUIThreadDecoratorFactory
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static <PRESENTATION> PRESENTATION createUIThreadDecorator (final @Nonnull PRESENTATION presentation,
                                                                       final Class<PRESENTATION> interfaceClass) 
      {
        return (PRESENTATION)Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(), 
                    new Class<?>[] {interfaceClass}, 
//                    new Class<?>[] {presentation.getClass().getInterfaces()[0]}, 
                    new InvocationHandler() 
          {
            private final Handler handler = new Handler();
            
            @Override 
            public Object invoke (final @Nonnull Object proxy, 
                                  final @Nonnull Method method, 
                                  final Object[] args)
              {
                log.debug("{}({})", method.getName(), args);
                handler.post(new Runnable() 
                  {
                    @Override
                    public void run() 
                      {
                        try 
                          {
                            method.invoke(presentation, args);
                          }
                        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                          {
                            throw new RuntimeException(e);
                          }
                      }
                  });
                
                return null;
              }
          });
      }
  }
