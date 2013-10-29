/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tidalwave.bluebell.mobile.utils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 *
 * @author fritz
 */
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
