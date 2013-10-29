/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tidalwave.bluebell.mobile;

import java.io.IOException;
import org.json.JSONObject;

/**
 *
 * @author fritz
 */
public interface SimpleRemoteApi 
  {
    /**
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
     * @return JSON data of response
     */
    public JSONObject actTakePicture() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject getApplicationInfo() throws IOException;

    // Camera Service APIs
    /**
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
     * @return JSON data of response
     */
    public JSONObject getAvailableApiList() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject getAvailableShootMode() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject getEvent(boolean longPollingFlag) throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject getShootMode() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject getSupportedShootMode() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject setShootMode(String shootMode) throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject startLiveview() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject startMovieRec() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject startRecMode() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject stopLiveview() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject stopMovieRec() throws IOException;

    /**
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
     * @return JSON data of response
     */
    public JSONObject stopRecMode() throws IOException;
  }
