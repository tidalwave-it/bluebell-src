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
package it.tidalwave.bluebell.mobile.utils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface HttpClient
  {
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10000; // msec
    
    public static final int DEFAULT_READ_TIMEOUT = 10000; // msec

    /**
     * Send HTTP GET request to the indicated url. Then returns response as
     * string.
     *
     * @param url request target
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Nonnull 
    public String get (@Nonnull String url)
      throws IOException;

    /**
     * Send HTTP GET request to the indicated url. Then returns response as
     * string.
     *
     * @param url request target
     * @param timeout Request timeout
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Nonnull 
    public String get (@Nonnull String url, @Nonnegative int timeout)
      throws IOException;

    /**
     * Send HTTP POST request to the indicated url. Then returns response as
     * string.
     *
     * @param url request target
     * @param postData POST body data as string (ex. JSON)
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    public String post (@Nonnull String url, @Nonnull String postData) 
      throws IOException;

    /**
     * Send HTTP POST request to the indicated url. Then returns response as
     * string.
     *
     * @param url request target
     * @param postData POST body data as string (ex. JSON)
     * @param timeout Request timeout
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Nonnull 
    public String post (@Nonnull String url, @Nonnull String postData, @Nonnegative int timeout)
      throws IOException; 
  }
