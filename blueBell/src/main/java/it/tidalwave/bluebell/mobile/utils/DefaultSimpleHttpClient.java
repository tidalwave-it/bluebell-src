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
    private static final String TAG = DefaultSimpleHttpClient.class.getSimpleName();

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
     * @param url request target
     * @param timeout Request timeout
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Override
    public String get(String url, int timeout) throws IOException 
      {
        @Cleanup("disconnect") HttpURLConnection httpConn = null;
        @Cleanup InputStream inputStream = null;

        // Open connection and input stream
        try 
          {
            final URL _url = new URL(url);
            httpConn = (HttpURLConnection) _url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            httpConn.setReadTimeout(timeout);
            httpConn.connect();

            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
            if (inputStream == null) {
                throw new IOException("Response Error:" + responseCode);
            }
          }
        catch (final SocketTimeoutException e) 
          {
            throw new IOException("httpGet: Timeout: " + url);
          }
        catch (final MalformedURLException e) 
          {
            throw new IOException("httpGet: MalformedUrlException: " + url);
          }

        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder responseBuf = new StringBuilder();
        int c;

        while ((c = reader.read()) != -1) 
          {
            responseBuf.append((char)c);
          }

        return responseBuf.toString();
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
     * @param url request target
     * @param postData POST body data as string (ex. JSON)
     * @param timeout Request timeout
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    @Override
    public String post(String url, String postData, int timeout)
      throws IOException 
      {
        @Cleanup("disconnect") HttpURLConnection httpConn = null;
        @Cleanup OutputStream outputStream = null;
        @Cleanup OutputStreamWriter writer = null;
        InputStream inputStream = null;

        // Open connection and input stream
        try 
          {
            final URL _url = new URL(url);
            httpConn = (HttpURLConnection) _url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            httpConn.setReadTimeout(timeout);
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);

            outputStream = httpConn.getOutputStream();
            writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(postData);
            writer.flush();
//            writer.close();
//            outputStream.close();

            httpConn.connect();
            int responseCode = httpConn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) 
              {
                inputStream = httpConn.getInputStream();
              }
            
            if (inputStream == null) 
              {
                throw new IOException("Response Error:" + responseCode);
              }
          } 
        catch (final SocketTimeoutException e) 
          {
            throw new IOException("httpPost: Timeout: " + url);
          } 
        catch (final MalformedURLException e) 
          {
            throw new IOException("httpPost: MalformedUrlException: " + url);
          }

        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder responseBuf = new StringBuilder();
        int c;

        while ((c = reader.read()) != -1) 
          {
            responseBuf.append((char) c);
          }
        
        return responseBuf.toString();
      }
  }
