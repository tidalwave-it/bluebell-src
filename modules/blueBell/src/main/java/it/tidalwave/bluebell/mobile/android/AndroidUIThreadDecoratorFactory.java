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
 * This is a facility to simplify code in an {@link Activity}, for what concerns methods that are invoked from a 
 * controller to its controlled {@code Activity}.
 * 
 * Supposing we have an interface:
 * 
 * <pre>
 * public interface MyPresentation
 *   {
 *     public void myMethod (String text);
 *   }
 * </pre>
 * 
 * and an {@code Activity} implementing the interface. If the method touches a UI widget, Android requires that this
 * operation is performed in the same thread that created the {@code Activity}. In order to decouple the controllers 
 * and have them to depend in a minimal way on Android (or not to depend on it at all), the UI threading logic must be
 * implemented in the {@code Activity} as follows:
 * 
 * <pre>
 * public class MyActivity extends Activity implements MyPresentation
 *   {
 *     private final Handler handler = new Handler();
 * 
 *     ...
 * 
 *     public void myMethod (final String text)
 *       {
 *         handler.post(new Runnable()
 *          {
 *            public void run()
 *              {
 *                // e.g. textView.setText(text);
 *              }
 *          });
 *       }
 *   }
 * </pre>
 * 
 * The wrapping code is cumbersome. By means of this facility, it can be simplified to:
 * 
 * <pre>
 * public class MyActivity extends Activity implements MyInterface
 *   {
 *     ...
 * 
 *     public void myMethod (final String text)
 *       {
 *         // e.g. textView.setText(text);
 *       }  
 *   }
 * </pre>
 * 
 * In order to have this simplification, each controller having a reference to the {@link Activity} must access it by
 * means of a decorator created by the method {@link #createUIThreadDecorator(java.lang.Object, java.lang.Class)}:
 * 
 * <pre>
 *   MyActivity activity = ...;
 *   MyPresentation decoratedPresentation = createUIThreadDecorator(myActivity, MyPresentation.class);
 * </pre>
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
     * Creates a new decorator which executes methods in the proper Android UI thread. Please look at the javadoc of
     * this class for further information.
     * 
     * @param       the presentation to decorate
     * @param       the metaclass of the presentation interface
     * @return      the decorator
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
