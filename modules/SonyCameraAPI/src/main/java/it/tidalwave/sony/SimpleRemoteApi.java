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
package it.tidalwave.sony;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.json.JSONObject;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface SimpleRemoteApi 
  {
    /*******************************************************************************************************************
     * 
     * Calls actTakePicture API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "actTakePicture",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject actTakePicture() 
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls getApplicationInfo API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "getApplicationInfo",
     *   "params": [""],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject getApplicationInfo()
      throws IOException;

    // Camera Service APIs
    /*******************************************************************************************************************
     * 
     * Calls getAvailableApiList API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "getAvailableApiList",
     *   "params": [""],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject getAvailableApiList()
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls getAvailableShootMode API to the target server. Request JSON data
     * is such like as below.
     *
     * <pre>
     * {
     *   "method": "getAvailableShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject getAvailableShootMode()
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls getEvent API to the target server. Request JSON data is such like
     * as below.
     *
     * <pre>
     * {
     *   "method": "getEvent",
     *   "params": [true],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param longPollingFlag true means long polling request.
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject getEvent (boolean longPollingFlag)
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls getShootMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "getShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject getShootMode() 
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls getSupportedShootMode API to the target server. Request JSON data
     * is such like as below.
     *
     * <pre>
     * {
     *   "method": "getSupportedShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject getSupportedShootMode()
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls setShootMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "setShootMode",
     *   "params": ["still"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param shootMode shoot mode (ex. "still")
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject setShootMode (@Nonnull String shootMode) 
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls startLiveview API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "startLiveview",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject startLiveview() 
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls startMovieRec API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "startMovieRec",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject startMovieRec()
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls startRecMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "startRecMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject startRecMode() 
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls stopLiveview API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "stopLiveview",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject stopLiveview()
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls stopMovieRec API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "stopMovieRec",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject stopMovieRec() 
      throws IOException;

    /*******************************************************************************************************************
     * 
     * Calls stopRecMode API to the target server. Request JSON data is such
     * like as below.
     *
     * <pre>
     * {
     *   "method": "stopRecMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @throws IOException  in case of error
     * @return              the JSON response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public JSONObject stopRecMode() 
      throws IOException;
  }
