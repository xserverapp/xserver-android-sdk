package xserver.app;

/**

 XServer

 © XScoder 2020
 All Rights reserved

 * IMPORTANT *
 RE-SELLING THIS SOURCE CODE TO ANY ONLINE MARKETPLACE
 IS A SERIOUS COPYRIGHT INFRINGEMENT, AND YOU WILL BE
 LEGALLY PROSECUTED

 **/

import com.myname.myapp.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.FirebaseApp;
import com.mb3364.http.AsyncHttpClient;
import com.mb3364.http.HttpClient;
import com.mb3364.http.RequestParams;
import com.mb3364.http.StringHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import eu.amirs.JSON;

public class XServerSDK extends Application {


   // ----------------------------------------------------------------------------------------
   // MARK: - REPLACE THE STRING BELOW WITH THE URL WHERE YOU'VE UPLOADED THE XServer FILES
   // ----------------------------------------------------------------------------------------
   public static String  DATABASE_PATH = "YOUR_DATABASE_PATH";





   // ------------------------------------------------
   // MARK: - GLOBAL STATIC VARIABLES
   // ------------------------------------------------
   public static String IOS_DEVICE_TOKEN = "";
   public static String ANDROID_DEVICE_TOKEN = "";
   public static String TABLES_PATH = DATABASE_PATH + "_Tables/";
   public static String TAG = "log-";
   public static SharedPreferences PREFS;
   public static String pushMessage = "";
   public static String pushType = "";


   // ------------------------------------------------
   // MARK: - XServer -> COMMON ERROR MESSAGES
   // ------------------------------------------------
   public static String XS_ERROR = "No response from server. Try again later.";
   public static String E_101 = "Username already exists. Please choose another one.";
   public static String E_102 = "Email already exists. Please choose another one.";
   public static String E_103 = "Object not found.";
   public static String E_104 = "Something went wrong while sending a Push Notification.";
   public static String E_201 = "Something went wrong while creating/updating data.";
   public static String E_202 = "Either the username or password are wrong. Please type them again.";
   public static String E_203 = "Something went wrong while deleting data.";
   public static String E_301 = "Email doesn't exists in the database. Try a new one.";
   public static String E_302 = "You have signed in with a Social account, password cannot be changed.";
   public static String E_401 = "File upload failed. Try again";



   //-----------------------------------------------
   // MARK - ON CREATE
   //-----------------------------------------------
   public void onCreate() {
      super.onCreate();

      // SharedPreferences
      PREFS = PreferenceManager.getDefaultSharedPreferences(this);

      // Initialize Firebase FCM
      FirebaseApp.initializeApp(this);


   }// ./ onCreate




   //------------------------------------------------------------------------
   //------------------------------------------------------------------------
   // MARK: - XServer FUNCTIONS
   //------------------------------------------------------------------------
   //------------------------------------------------------------------------


   //-----------------------------------------------
   // MARK - XSQuery -> QUERY DATA
   //-----------------------------------------------
   public interface XSQueryHandler { void done(JSON objects, String error); }
   public static void XSQuery(final Activity act, String tableName, String columnName, String orderBy, final XSQueryHandler handler) {

      // Set the Parameters
      RequestParams params = new RequestParams();
      params.put("tableName", tableName);
      params.put("columnName", columnName);
      params.put("orderBy", orderBy);

      final HttpClient client = new AsyncHttpClient();
      client.post(TABLES_PATH + "m-query.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {

               JSON objects;
               // Log.i(TAG, "XSQuery -> RESPONSE: " + response);
               if (response.matches(XS_ERROR)) { handler.done(null, XS_ERROR);
               } else {
                  objects = new JSON(response);
                  handler.done(objects, null);
               }
            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, XS_ERROR); }});
         }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, String.valueOf(throwable)); }});
      }});

   }


   //-----------------------------------------------
   // MARK - XSRefreshObjectData -> REFRESH AN OBJECT'S DATA
   //-----------------------------------------------
   public interface XSRefreshObjectDataHandler { void done(JSON object, String error); }
   public static void XSRefreshObjectData(final Activity act, String tableName, final JSON object, final XSRefreshObjectDataHandler handler) {

      // Set the Parameters
      RequestParams params = new RequestParams();
      params.put("tableName", tableName);

      final HttpClient client = new AsyncHttpClient();
      client.post(TABLES_PATH + "m-query.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {
               // Log.i(TAG, "XSRefreshObjectData -> RESPONSE: " + response);
               if (response.matches(XS_ERROR)) { handler.done(null, XS_ERROR);
               } else {
                  JSON objects = new JSON(response);
                  for(int i=0; i<objects.count(); i++){
                     JSON obj = objects.index(i);
                     if (obj.key("ID_id").stringValue().matches(object.key("ID_id").stringValue()) ){ handler.done(obj, null); }
                  } //./ For
               } //./ If
            }}); // ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, XS_ERROR); }});
         }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, String.valueOf(throwable)); }});
      }});
   }

   

   //-----------------------------------------------
   // MARK - XSDelete -> DELETE AN OBJECT
   //-----------------------------------------------
   public interface XSDeleteHandler { void done(boolean success, String error); }
   public static void XSDelete(final Activity act, String tableName, String id, final XSDeleteHandler handler) {

      // Set the Parameters
      RequestParams params = new RequestParams();
      params.put("tableName", tableName);
      params.put("id", id);
      final HttpClient client = new AsyncHttpClient();
      client.post(TABLES_PATH + "m-delete.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {

               Log.i(TAG, "XSDelete -> RESPONSE: " + response);
               if (response.matches(XS_ERROR)) { handler.done(false, XS_ERROR);
               } else { handler.done(true, null); }

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, XS_ERROR); }});
         }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, String.valueOf(throwable)); }});
      }});

   }




   // ------------------------------------------------
   // MARK: - XSCurrentUser -> GET CURRENT USER DATA
   // ------------------------------------------------
   public interface XSCurrentUserHandler { void done(JSON currUser); }
   public static void XSCurrentUser(final Activity act, final XSCurrentUserHandler handler) {
      final String currentUser = PREFS.getString("currentUser", null);

      if (currentUser != null) {
         // Log.i(TAG, "CURRENT USER ID: " + currentUser);

         // Set the Parameters
         RequestParams params = new RequestParams();
         // no params needed
         HttpClient client = new AsyncHttpClient();
         client.post(TABLES_PATH + "Users.json", params, new StringHttpResponseHandler() {

            // Success
            @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
               act.runOnUiThread(new Runnable() { @Override public void run() {
                  // Log.i(TAG, "XSCurrentUser -> RESPONSE: " + response);

                  if (response.matches(XS_ERROR)) {
                     handler.done(null);
                  } else {
                     JSON users = new JSON(response);
                     boolean ok = false;

                     // Search for currentUser obj
                     if (users.count() != 0) {
                        for (int j = 0; j < users.count(); j++) {
                           JSON uObj = users.index(j);
                           if (uObj.key("ID_id").stringValue().matches(currentUser)) {
                              // Log.i(TAG, "** CURRENT USER: " + uObj.key(USERS_USERNAME).stringValue() + " **");
                              ok = true;
                              handler.done(uObj);
                           }

                           // Object not found
                           if (j == users.count() - 1 && !ok) {
                              if (!uObj.key("ID_id").stringValue().matches(currentUser)) {
                                 PREFS.edit().putString("currentUser", null).apply();
                                 handler.done(null);
                              }
                           }
                        }// ./ For

                     // Object not found
                     } else { handler.done(null); }
                  }// ./ If

               }});// ./ runOnUiThread
            }

            // Failed
            @Override
            public void onFailure(int statusCode, Map<String, List<String>> headers, String response) {
               /* Server responded with a status code 4xx or 5xx error */
               act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null); }});
            }

            @Override
            public void onFailure(Throwable throwable) {
               /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
               act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null);  }});
         }});


      // currentUser PREFS is null
      } else { handler.done(null); }
   }




   // ------------------------------------------------
   // MARK: - XSGetUserPointer -> GET OBJECT POINTER
   // ------------------------------------------------
   public interface XSPointerHandler { void done(JSON pointerObj, String error); }
   public static void XSGetPointer(final Activity act, final String id, String tableName, final XSPointerHandler handler) {
      // Parameters
      RequestParams params = new RequestParams();
      // no params needed
      HttpClient client = new AsyncHttpClient();
      client.post(TABLES_PATH + tableName + ".json", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {
               // Log.i(TAG, "XSGetUserPointer -> RESPONSE: " + response);
               if (response.matches(XS_ERROR)) { handler.done(null, XS_ERROR);
               } else {
                  JSON objects = new JSON(response);
                  boolean ok = false;

                  // Search for currentUser obj
                  if (objects.count() != 0) {
                     for (int j = 0; j < objects.count(); j++) {
                        JSON obj = objects.index(j);
                        if (obj.key("ID_id").stringValue().matches(id)) {
                           ok = true;
                           handler.done(obj, null);
                        }

                        // Object not found
                        if (j == objects.count() - 1 && !ok) {
                           if (!obj.key("ID_id").stringValue().matches(id)) {
                              handler.done(null, E_103);
                           }
                        }
                     }// ./ For

                     // Object not found
                  } else { handler.done(null, E_103); }
               }// ./ If

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(final int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, XS_ERROR); }}); }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, String.valueOf(throwable)); }});
      }});

   }




   // ------------------------------------------------
   // MARK: - XSSignUp -> SIGN UP
   // ------------------------------------------------
   public interface XSSignUpHandler { void done(String result, String error); }
   public static void XSSignUp(final Activity act, String username, String password, String email, String signInWith, final XSSignUpHandler handler) {

      // Set the Parameters
      RequestParams params = new RequestParams();
      params.put("ST_username", username);
      params.put("ST_password", password);
      params.put("ST_email", email);
      params.put("signInWith", signInWith);
      params.put("ST_iosDeviceToken", IOS_DEVICE_TOKEN);
      params.put("ST_androidDeviceToken", ANDROID_DEVICE_TOKEN);

      final HttpClient client = new AsyncHttpClient();
      client.post(TABLES_PATH + "m-signup.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {
               Log.i(TAG, "XSSingUp -> RESPONSE: " + response);
               if (response.matches("e_101")) { handler.done(null, E_101);
               } else if (response.matches("e_102")) { handler.done(null, E_102);
               } else if (response.matches(XS_ERROR) ) { handler.done(null, XS_ERROR);
               } else {
                  handler.done(response, null);
                  String[] resultsArr = response.split("-");
                  String uID = resultsArr[0];
                  PREFS.edit().putString("currentUser", uID).apply();
               }

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, XS_ERROR); }});
         }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, String.valueOf(throwable)); }});
         }});

   }



   // ------------------------------------------------
   // MARK: - XSSignIn -> SIGN IN
   // ------------------------------------------------
   public interface XSSignInHandler { void done(boolean success, String error); }
   public static void XSSignIn(final Activity act, final String username, final String password, final XSSignInHandler handler) {
      // Set the Parameters
      RequestParams params = new RequestParams();
      params.put("tableName", "Users");

      final HttpClient client = new AsyncHttpClient();
      client.post(TABLES_PATH + "m-query.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {
               //Log.i(TAG, "XSSingIn -> RESPONSE: " + response);
               if (response.matches(XS_ERROR)) { handler.done(false, XS_ERROR);
               } else {
                  JSON users = new JSON(response);
                  boolean ok = false;

                  // Search for currentUser obj
                  if (users.count() != 0) {
                     for (int j = 0; j < users.count(); j++) {
                        JSON uObj = users.index(j);
                        if (uObj.key("ST_username").stringValue().matches(username) && uObj.key("ST_password").stringValue().matches(password)) {
                           Log.i(TAG, "** SIGNED IN AS: " + uObj.key("ST_username").stringValue() + " **");
                           PREFS.edit().putString("currentUser", uObj.key("ID_id").stringValue()).apply();
                           ok = true;
                           handler.done(true, null);
                        }

                        // User doesn't exists in database or credentials are wrong
                        if (j == users.count() - 1 && !ok) {
                           if (!uObj.key("ST_username").stringValue().matches(username) && !uObj.key("ST_password").stringValue().matches(password)) {
                              handler.done(false, E_202);
                           }
                        }
                     }// ./ For

                  // No users in the database!
                  } else { handler.done(false, E_202); }
               }

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, XS_ERROR); }});
         }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, String.valueOf(throwable)); }});
      }});

   }



   // ------------------------------------------------
   // MARK: - XSReset Password -> RESET PASSWORD
   // ------------------------------------------------
   public interface ResetPasswordHandler { void done(String result, String error); }
   public static void XSResetPassword(final Activity act, String email, final ResetPasswordHandler handler) {
      RequestParams params = new RequestParams();
      // no params needed
      HttpClient client = new AsyncHttpClient();
      client.get(TABLES_PATH + "forgot-password.php?email=" + email, params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {

               Log.i(TAG, "XSResetPassword -> RESPONSE: " + response);
               if (response.matches(XS_ERROR) ) { handler.done(null, XS_ERROR);
               } else if (response.matches("e_301")) { handler.done(null, E_301);
               } else if (response.matches("e_302")) { handler.done(null, E_302);
               } else { handler.done(response, null); }

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(final int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, XS_ERROR); }}); }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(null, String.valueOf(throwable)); }});
      }});

   }


   //-----------------------------------------------
   // MARK - XSLogout -> LOGOUT
   //-----------------------------------------------
   public interface LogoutHandler { void done(boolean success); }
   public static void XSLogout(LogoutHandler handler) {
      PREFS.edit().putString("currentUser", null).apply();
      handler.done(true);
   }





   // ------------------------------------------------
   // MARK: - XSObject -> CREATE DATA
   // ------------------------------------------------
   public interface XSObjectHandler { void done(boolean success, String error); }
   public static void XSObject(final Activity act, RequestParams params, final XSObjectHandler handler) {

      final HttpClient client = new AsyncHttpClient();
      client.post(TABLES_PATH + "m-add-edit.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {

               Log.i(TAG, "XSObject -> RESPONSE: " + response);
               if (response.matches("saved")) { handler.done(true, null);
               } else if (response.matches(XS_ERROR)) { handler.done(false, XS_ERROR);
               } else { handler.done(false, E_201); }

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, XS_ERROR); }});
         }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, String.valueOf(throwable)); }});
         }});

   }





   //-----------------------------------------------
   // MARK - XSSendAndroidPush -> SEND ANDROID PUSH NOTIFICATION
   //-----------------------------------------------
   public interface XSAndroidPushHandler { void done(boolean success, String error); }
   public static void XSSendAndroidPush(final Activity act, String message, String deviceToken, String pushType, final XSAndroidPushHandler handler) {
      RequestParams params = new RequestParams();
      params.put("message", message);
      params.put("deviceToken", deviceToken);
      params.put("pushType", pushType);

      HttpClient client = new AsyncHttpClient();
      client.post(DATABASE_PATH + "_Push/send-android-push.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {

               Log.i(TAG, "XSSendAndroidPush -> RESPONSE: " + response);
               assert response != null;
               if (response.matches("e_104")) { handler.done(false, E_104);
               } else if (response.matches(XS_ERROR)) { handler.done(false, XS_ERROR);
               } else { handler.done(true, null); }

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(final int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, XS_ERROR); }}); }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, String.valueOf(throwable)); }});
         }});

   }



   //-----------------------------------------------
   // MARK - XSSendiOSPush -> SEND ANDROID PUSH NOTIFICATION
   //-----------------------------------------------
   public interface XSiOSPushHandler { void done(boolean success, String error); }
   public static void XSSendiOSPush(final Activity act, String message, String deviceToken, String pushType, final XSiOSPushHandler handler) {
      RequestParams params = new RequestParams();
      params.put("message", message);
      params.put("deviceToken", deviceToken);
      params.put("pushType", pushType);

      HttpClient client = new AsyncHttpClient();
      client.post(DATABASE_PATH + "_Push/send-ios-push.php?", params, new StringHttpResponseHandler() {

         // Success
         @Override public void onSuccess(int statusCode, Map<String, List<String>> headers, final String response) {
            act.runOnUiThread(new Runnable() { @Override public void run() {

               Log.i(TAG, "XSSendiOSPush -> RESPONSE: " + response);
               assert response != null;
               if (response.matches("e_104")) { handler.done(false, E_104);
               } else if (response.matches(XS_ERROR)) { handler.done(false, XS_ERROR);
               } else { handler.done(true, null); }

            }});// ./ runOnUiThread
         }

         // Failed
         @Override
         public void onFailure(final int statusCode, Map<String, List<String>> headers, String response) {
            /* Server responded with a status code 4xx or 5xx error */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, XS_ERROR); }}); }

         @Override
         public void onFailure(final Throwable throwable) {
            /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
            act.runOnUiThread(new Runnable() { @Override public void run() { handler.done(false, String.valueOf(throwable)); }});
      }});
   }





   //-----------------------------------------------
   // MARK - XSUploadFile -> UPLOAD A FILE
   //-----------------------------------------------
   public interface XSFileHandler { void done(String fileURL, String error); }
   public static void XSUploadFile(String filePath, String fileName, Activity act, final XSFileHandler handler) {
         HttpURLConnection conn;
         DataOutputStream dos;
         String lineEnd = "\r\n";
         String twoHyphens = "--";
         String boundary = "*****";
         final int code;
         int bytesRead, bytesAvailable, bufferSize;
         byte[] buffer;
         int maxBufferSize = 1024*1024;
         File sourceFile = new File(filePath);

         try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(DATABASE_PATH + "upload-file.php");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("file", filePath);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);


            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"fileName\"\r\n\r\n" +
                    fileName + "\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"file\"\r\n"
            );



            dos.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
               dos.write(buffer, 0, bufferSize);
               bytesAvailable = fileInputStream.available();
               bufferSize = Math.min(bytesAvailable, maxBufferSize);
               bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            code = conn.getResponseCode();

            if (code == HttpURLConnection.HTTP_OK) {
               final HttpURLConnection finalConn = conn;
               act.runOnUiThread(new Runnable() {
                     public void run() {
                        InputStream responseStream = null;
                        try {
                           responseStream = new BufferedInputStream(finalConn.getInputStream());
                           BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
                           String line;
                           StringBuilder stringBuilder = new StringBuilder();
                           int i = 0;
                           while ((line = responseStreamReader.readLine()) != null) {
                              if (i != 0) { stringBuilder.append("\n"); }
                              stringBuilder.append(line);
                              i++;
                           }
                           responseStreamReader.close();
                           String response = stringBuilder.toString();
                           responseStream.close();
                           finalConn.disconnect();
                           // Log.i(TAG, "XSUploadFile -> " + response);

                           if (response != null) { handler.done(DATABASE_PATH + response, null);
                           } else { handler.done(null, E_401); }

                        // error
                        } catch (IOException e) { e.printStackTrace(); handler.done(null, XS_ERROR); }
                     }});

               // Bad response from sever
               } else { act.runOnUiThread(new Runnable() {
                     public void run() { handler.done(null, XS_ERROR); }}); }
               fileInputStream.close();
               dos.flush();
               dos.close();

         // No response from server
         } catch (final Exception ex) {
            act.runOnUiThread(new Runnable() {
               public void run() { handler.done(null, ex.getMessage()); }});
         }
   }





   //-----------------------------------------------
   // MARK - CLEAN RECEIVED PUSH DATA
   //-----------------------------------------------
   public static void cleanReceivedPushData() {
      pushMessage = "";
      pushType = "";
   }



   //-----------------------------------------------
   // MARK - GET STRING FROM ARRAY
   //-----------------------------------------------
   public static String XSGetStringFromArray(List<String> array) {
      String arrayStr = "";
      if (array.size() != 0) {
         for (String s : array) { arrayStr += s + ","; }
         arrayStr = arrayStr.substring(0, arrayStr.length() - 1);
      }
      return arrayStr;
   }


   //-----------------------------------------------
   // MARK - GET ARRAY FROM JSONArray
   //-----------------------------------------------
   public static List<String> XSGetArrayFromJSONArray(JSONArray arr) {
      List<String>array = new ArrayList<>();
      for (int i=0; i<arr.length(); i++) {
         try { array.add((String)arr.get(i));
         } catch (JSONException er) { er.printStackTrace(); }
      }
      return array;
   }



   //-----------------------------------------------
   // MARK - REMOVE DUPLICATES FORM ARRAY
   //-----------------------------------------------
   public static List<JSON> XSRemoveDuplicatesFromArray(List<JSON> array) {
      Map<JSON, JSON> cleanMap = new LinkedHashMap<>();
      for (int i = 0; i < array.size(); i++) {
         cleanMap.put(array.get(i), array.get(i));
      }
      return new ArrayList<>(cleanMap.values());
   }



   //-----------------------------------------------
   // MARK - GET STRING FROM DATE
   //-----------------------------------------------
   public static String XSGetStringFromDate(Date date) {
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
      return df.format(date);
   }

   //-----------------------------------------------
   // MARK - GET DATE FROM STRING
   //-----------------------------------------------
   public static Date XSGetDateFromString(String dateString) {
      Date date = null;
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
      try { date = df.parse(dateString);
      } catch (ParseException e) { e.printStackTrace(); }
      return date;
   }

   
 
   //-----------------------------------------------
   // MARK - GET LOCATION FROM GPS Array
   //-----------------------------------------------
   public static Location XSGetLocationFromGPSArray(JSONArray arr) {
      Location loc = new Location("provider");
      List<Double>coordsArr = new ArrayList<>();
      for (int i=0; i<arr.length(); i++) {
         try { coordsArr.add(Double.parseDouble((String)arr.get(i)));
         } catch (JSONException er) { er.printStackTrace(); }
      }
      loc.setLatitude(coordsArr.get(0));
      loc.setLongitude(coordsArr.get(1));
      return loc;
   }


   //-----------------------------------------------
   // MARK - GET STRING FROM LOCATION
   //-----------------------------------------------
   public static String XSGetStringFromLocation(Location loc) {
      List<String>array = new ArrayList<>();
      array.add(String.valueOf(loc.getLatitude()));
      array.add(String.valueOf(loc.getLongitude()));
      String arrayStr = "";
      if (array.size() != 0) {
         for (String s : array) { arrayStr += s + ","; }
         arrayStr = arrayStr.substring(0, arrayStr.length() - 1);
      }
      return arrayStr;
   }


   //-----------------------------------------------
   // MARK - CHECK INTERNET CONNECTION
   //-----------------------------------------------
   public static boolean isInternetConnectionAvailable(Context ctx) {
      ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
      assert cm != null;
      NetworkInfo ni = cm.getActiveNetworkInfo();
      return ni != null && ni.isConnected();
   }

  
}// ./ end
