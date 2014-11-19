/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package android;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

public class RecordVideo extends CordovaPlugin {


	public static final String RECORD_VIDEO = "recordVideo"; 

	private static final String TAG = RecordVideo.class.getName();

    private CallbackContext callbackContext;        // The callback context from which we were invoked.
    private JSONArray results;                      // The array of results to be returned to the user

    //private CordovaInterface cordova;

//    public void setContext(Context mCtx)
//    {
//        if (CordovaInterface.class.isInstance(mCtx))
//            cordova = (CordovaInterface) mCtx;
//        else
//            LOG.d(LOG_TAG, "ERROR: You must use the CordovaInterface for this to work correctly. Please implement it in your activity");
//    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
       /* this.callbackContext = callbackContext;
        //this.limit = 1;
        //this.duration = 0;
        this.results = new JSONArray();

        JSONObject options = args.optJSONObject(0);
        if (options != null) {
            limit = options.optLong("limit", 1);
            duration = options.optInt("duration", 0);
        }

        if (action.equals("getFormatData")) {
            JSONObject obj = getFormatData(args.getString(0), args.getString(1));
            callbackContext.success(obj);
            return true;
        }
        else if (action.equals("captureAudio")) {
            this.captureAudio();
        }
        else if (action.equals("captureImage")) {
            this.captureImage();
        }
        else if (action.equals("captureVideo")) {
            this.captureVideo(duration);
        }
        else {
            return false;
        }*/
    	Log.i(TAG, "execute method");
    	if(RECORD_VIDEO.equals("recordVideo")){
    		recordVideo();
    	}

        return true;
    }

   
   
    /**
     * Sets up an intent to capture video.  Result handled by onActivityResult()
     */
    private void recordVideo() {
    	Intent intent = new Intent(this.cordova.getActivity(), CameraActivity.class);
		this.cordova.getActivity().startActivity(intent);
    }

   

    private JSONObject createErrorObject(int code, String message) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("code", code);
            obj.put("message", message);
        } catch (JSONException e) {
            // This will never happen
        }
        return obj;
    }

    /**
     * Send error message to JavaScript.
     *
     * @param err
     */
    public void fail(JSONObject err) {
        this.callbackContext.error(err);
    }



}
