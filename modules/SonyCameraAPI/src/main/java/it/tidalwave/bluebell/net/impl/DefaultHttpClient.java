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
package it.tidalwave.bluebell.net.impl;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import it.tidalwave.bluebell.net.HttpClient;
import lombok.Cleanup;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultHttpClient implements HttpClient 
  {
    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public String get (@Nonnull String url) 
      throws IOException 
      {
        return get(url, DEFAULT_READ_TIMEOUT);
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public String get (final @Nonnull String urlAsString, final @Nonnull int timeout)
      throws IOException 
      {
        final URL url = new URL(urlAsString);
        final @Cleanup("disconnect") HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        connection.setReadTimeout(timeout);
        connection.connect();

        final int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) 
          {
            throw new IOException("Response Error:" + responseCode);
          }

        final @Cleanup InputStream is = connection.getInputStream();
        return readString(is);
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public String post (final @Nonnull String url, final @Nonnull String postData)
      throws IOException 
      {
        return post(url, postData, DEFAULT_READ_TIMEOUT);
      }

    /*******************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public String post (final @Nonnull String urlAsString, final @Nonnull String postData, final @Nonnull int timeout)
      throws IOException 
      {
        final URL url = new URL(urlAsString);
        final @Cleanup("disconnect") HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        connection.setReadTimeout(timeout);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        final @Cleanup OutputStream os = connection.getOutputStream();
        final @Cleanup OutputStreamWriter w = new OutputStreamWriter(os, "UTF-8");
        w.write(postData);
        w.flush();
        w.close();
        os.close();

        connection.connect();
        final int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) 
          {
            throw new IOException("Response Error:" + responseCode);
          }

        final @Cleanup InputStream is = connection.getInputStream();
        return readString(is);
      }
    
    /*******************************************************************************************************************
     * 
     * 
     * 
     ******************************************************************************************************************/
    @Nonnull
    private static String readString (final @Nonnull InputStream is) 
      throws IOException 
      {
        final @Cleanup BufferedReader r = new BufferedReader(new InputStreamReader(is));
        final StringBuilder buffer = new StringBuilder();
        int c;
        
        while ((c = r.read()) != -1)
          {
            buffer.append((char)c);
          }
        
        return buffer.toString();
      }
  }
