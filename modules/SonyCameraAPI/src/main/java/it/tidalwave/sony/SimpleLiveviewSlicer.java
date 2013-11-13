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
package it.tidalwave.sony;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A parser class for Liveview data Packet defined by Camera Remote API
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class SimpleLiveviewSlicer
  {
    /**
     * Payload data class. See also Camera Remote API specification document to
     * know the data structure.
     */
    public static class Payload
      {
        public final static Payload EMPTY = new Payload(new byte[0], new byte[0]);

        @Nonnull
        public final byte[] jpegData;

        @Nonnull
        public final byte[] paddingData;

        /**
         * Constructor
         */
        private Payload (byte[] jpegData, byte[] paddingData)
          {
            this.jpegData = jpegData;
            this.paddingData = paddingData;
          }

        public boolean isEmpty()
          {
            return jpegData.length == 0;
          }

        @Override @Nonnull
        public String toString()
          {
            return String.format("Payload[%d bytes]", jpegData.length);
          }
      }

    private static final int CONNECTION_TIMEOUT = 2000; // [msec]

    private HttpURLConnection mHttpConn;
    private InputStream is;

    /**
     * Opens Liveview HTTP GET connection and prepares for reading Packet data.
     *
     * @param liveviewUrl Liveview data url that is obtained by DD.xml or result
     *            of startLiveview API.
     * @throws IOException generic errors or exception.
     */
    public void open (URL url)
      throws IOException
      {
        if (is != null || mHttpConn != null)
          {
            throw new IllegalStateException("Slicer is already open.");
          }

        mHttpConn = (HttpURLConnection)url.openConnection();
        mHttpConn.setRequestMethod("GET");
        mHttpConn.setConnectTimeout(CONNECTION_TIMEOUT);
        mHttpConn.connect();

        if (mHttpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
          {
            is = mHttpConn.getInputStream();
          }

        if (is == null)
          {
            throw new IOException("open error: " + url);
          }
      }

    /**
     * Closes the connection.
     *
     * @throws IOException generic errors or exception.
     */
    public void close()
      throws IOException
      {
        if (is != null)
          {
            is.close();
            is = null;
          }

        if (mHttpConn != null)
          {
            mHttpConn.disconnect();
            mHttpConn = null;
          }
      }

    /**
     * Reads liveview stream and slice one Packet. If server is not ready for
     * liveview data, this API calling will be blocked until server returns next
     * data.
     *
     * @return Payload data of sliced Packet
     * @throws IOException generic errors or exception.
     */
    public Payload readNextPayload()
       throws IOException
      {
        if (is != null)
          {
            // Common Header
            int readLength = 1 + 1 + 2 + 4;
            byte[] commonHeader = readBytes(is, readLength);

            if (commonHeader == null || commonHeader.length != readLength)
              {
                throw new IOException("Cannot read stream for common header.");
              }

            if (commonHeader[0] != (byte) 0xFF)
              {
                throw new IOException("Unexpected data format. (Start byte)");
              }

            if (commonHeader[1] != (byte) 0x01)
              {
                throw new IOException("Unexpected data format. (Payload byte)");
              }

            // Payload Header
            readLength = 4 + 3 + 1 + 4 + 1 + 115;
            byte[] payloadHeader = readBytes(is, readLength);

            if (payloadHeader == null || payloadHeader.length != readLength)
              {
                throw new IOException("Cannot read stream for payload header.");
              }

            if (payloadHeader[0] != (byte) 0x24
             || payloadHeader[1] != (byte) 0x35
             || payloadHeader[2] != (byte) 0x68
             || payloadHeader[3] != (byte) 0x79)
              {
                throw new IOException("Unexpected data format. (Start code)");
              }

            final int jpegSize = bytesToInt(payloadHeader, 4, 3);
            final int paddingSize = bytesToInt(payloadHeader, 7, 1);

            final byte[] jpegData = readBytes(is, jpegSize);
            final byte[] paddingData = readBytes(is, paddingSize);
            final Payload payload = new Payload(jpegData, paddingData);
            log.info(">>>> {}", payload);

            return payload;
          }

        return Payload.EMPTY;
      }

    // Converts byte array to int.
    private static int bytesToInt (byte[] byteData, int startIndex, int count)
      {
        int ret = 0;

        for (int i = startIndex; i < startIndex + count; i++)
          {
            ret = (ret << 8) | (byteData[i] & 0xff);
          }

        return ret;
      }

    // Reads byte array from the indicated input stream.
    private static byte[] readBytes(InputStream in, int length)
      throws IOException
      {
        ByteArrayOutputStream tmpByteArray = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (true)
          {
            int trialReadlen = Math.min(buffer.length, length - tmpByteArray.size());
            int readlen = in.read(buffer, 0, trialReadlen);

            if (readlen < 0)
              {
                break;
              }

            tmpByteArray.write(buffer, 0, readlen);

            if (length <= tmpByteArray.size())
              {
                break;
              }
          }

        byte[] ret = tmpByteArray.toByteArray();
        tmpByteArray.close();
        return ret;
      }
  }
