/*
 * Copyright 2013 Sony Corporation
 */

package it.tidalwave.bluebell.mobile.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import lombok.Cleanup;

/**
 * Simple HTTP Client for sample application.
 */
public class DefaultSimpleHttpClient implements SimpleHttpClient 
  {
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000; // [msec]
    private static final int DEFAULT_READ_TIMEOUT = 10000; // [msec]

    /**
     * Send HTTP GET request to the indicated url. Then returns response as
     * string.
     * 
     * @param url request target
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Override
    public String get(String url) throws IOException 
      {
        return get(url, DEFAULT_READ_TIMEOUT);
      }

    /**
     * Send HTTP GET request to the indicated url. Then returns response as
     * string.
     * 
     * @param urlAsString request target
     * @param timeout Request timeout
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Override
    public String get(String urlAsString, int timeout) throws IOException 
      {
        try 
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
        catch (SocketTimeoutException e) 
          {
            throw new IOException("httpGet: Timeout: " + urlAsString);
          }
        catch (MalformedURLException e) 
          {
            throw new IOException("httpGet: MalformedUrlException: " + urlAsString);
          }
      }

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
    @Override
    public String post (String url, String postData)
      throws IOException 
      {
        return post(url, postData, DEFAULT_READ_TIMEOUT);
      }

    /**
     * Send HTTP POST request to the indicated url. Then returns response as
     * string.
     * 
     * @param urlAsString request target
     * @param postData POST body data as string (ex. JSON)
     * @param timeout Request timeout
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Override
    public String post(String urlAsString, String postData, int timeout)
      throws IOException 
      {
        try 
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
        catch (SocketTimeoutException e) 
          {
            throw new IOException("httpPost: Timeout: " + urlAsString);
          } 
        catch (MalformedURLException e) 
          {
            throw new IOException("httpPost: MalformedUrlException: " + urlAsString);
          }
      }
    
    private static String readString (final InputStream is) 
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
