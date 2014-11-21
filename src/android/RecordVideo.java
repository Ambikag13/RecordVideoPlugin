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
package com.snipme.record;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;
import org.apache.cordova.file.FileUtils;
import org.apache.cordova.file.LocalFilesystemURL;
import org.apache.cordova.mediacapture.Capture;
import org.apache.cordova.mediacapture.FileHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class Record extends CordovaPlugin {

	private static final String TAG = Record.class.getName();

	public static final String RECORD_VIDEO = "recordVideo";
	private static final int CAPTURE_VIDEO = 0;
	private static final int CAPTURE_NO_MEDIA_FILES = 3;

	private static final String VIDEO_3GPP = "video/3gpp";
	private static final String VIDEO_MP4 = "video/mp4";
	private static final String AUDIO_3GPP = "audio/3gpp";
	private static final String IMAGE_JPEG = "image/jpeg";

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
    	Log.i(TAG, "execute method  "+action);
    	try{
    		if(RECORD_VIDEO.equals("recordVideo")){
        		Intent intent = new Intent(this.cordova.getActivity(), CameraActivity.class);
        		this.cordova.getActivity().startActivityForResult(intent, CAPTURE_VIDEO);
        		callbackContext.success();
        		return true;
        	}
    		callbackContext.error("Invalid action");
            return false;
    	}catch(Exception e) {
    	    System.err.println("Exception: " + e.getMessage());
    	    callbackContext.error(e.getMessage());
    	    return false;
    	} 
    }
    
	private String getTempDirectoryPath() {
        File cache = null;

        // Use internal storage
        cache = cordova.getActivity().getCacheDir();

        // Create the cache directory if it doesn't exist
        cache.mkdirs();
        return cache.getAbsolutePath();
    }
    
    /**
     * Creates a JSONObject that represents a File from the Uri
     *
     * @param data the Uri of the audio/image/video
     * @return a JSONObject that represents a File
     * @throws IOException
     */
    private JSONObject createMediaFile(Uri data) {
        File fp = webView.getResourceApi().mapUriToFile(data);
        JSONObject obj = new JSONObject();

        Class webViewClass = webView.getClass();
        PluginManager pm = null;
        try {
            Method gpm = webViewClass.getMethod("getPluginManager");
            pm = (PluginManager) gpm.invoke(webView);
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        if (pm == null) {
            try {
                Field pmf = webViewClass.getField("pluginManager");
                pm = (PluginManager)pmf.get(webView);
            } catch (NoSuchFieldException e) {
            } catch (IllegalAccessException e) {
            }
        }
        FileUtils filePlugin = (FileUtils) pm.getPlugin("File");
        LocalFilesystemURL url = filePlugin.filesystemURLforLocalPath(fp.getAbsolutePath());

        try {
            // File properties
            obj.put("name", fp.getName());
            obj.put("fullPath", fp.toURI().toString());
            if (url != null) {
                obj.put("localURL", url.toString());
            }
            // Because of an issue with MimeTypeMap.getMimeTypeFromExtension() all .3gpp files
            // are reported as video/3gpp. I'm doing this hacky check of the URI to see if it
            // is stored in the audio or video content store.
            if (fp.getAbsoluteFile().toString().endsWith(".3gp") || fp.getAbsoluteFile().toString().endsWith(".3gpp")) {
                if (data.toString().contains("/audio/")) {
                    obj.put("type", AUDIO_3GPP);
                } else {
                    obj.put("type", VIDEO_3GPP);
                }
            } else {
                obj.put("type", FileHelper.getMimeType(Uri.fromFile(fp), cordova));
            }

            obj.put("lastModifiedDate", fp.lastModified());
            obj.put("size", fp.length());
        } catch (JSONException e) {
            // this will never happen
            e.printStackTrace();
        }
        return obj;
    }
    
    /**
     * Called when the video view exits.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param intent            An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     * @throws JSONException
     */
    public void onActivityResult(int requestCode, int resultCode, final Intent intent) {
    	 if (resultCode == Activity.RESULT_OK) {
    		 if (requestCode == CAPTURE_VIDEO) {
    			    Log.i(TAG, "onActivityResult: Video Capture success");
    	            final Record that = this;
    	            Runnable captureVideo = new Runnable() {

    	                @Override
    	                public void run() {
    	                
    	                    Uri data = null;
    	                    
    	                    if (intent != null){
    	                        // Get the uri of the video clip
    	                        data = intent.getData();
    	                        Log.i(TAG, "onActivityResult: Uri: "+data);
    	                        Log.i(TAG, "Uri of the video file recorded");
    	                    }
    	                    
    	                    if( data == null){
    	                       File movie = new File(getTempDirectoryPath(), "Capture.avi");
    	                       data = Uri.fromFile(movie);
    	                    }
    	                    
    	                    // create a file object from the uri
    	                    if(data == null)
    	                    {
    	                        that.fail(createErrorObject(CAPTURE_NO_MEDIA_FILES, "Error: data is null"));
    	                    }
    	                    else
    	                    {
    	                        results.put(createMediaFile(data));

    	                        if (results.length() >= 1) {
    	                            // Send Uri back to JavaScript for viewing video
    	                            that.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, results));
    	                        } else {
    	                            // still need to capture more video clips
    	                            //captureVideo(duration);
    	                        }
    	                    }
    	                }
    	            };
    	            this.cordova.getThreadPool().execute(captureVideo);
    	        }
    	 }else if (resultCode == Activity.RESULT_CANCELED) {
    		 
    	 }
    	
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
