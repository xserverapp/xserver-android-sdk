package xserver.app;

/**

 XServer

 © WavyCode 2020
 All Rights reserved

 * IMPORTANT *
 RE-SELLING THIS SOURCE CODE TO ANY ONLINE MARKETPLACE
 IS A SERIOUS COPYRIGHT INFRINGEMENT, AND YOU WILL BE
 LEGALLY PROSECUTED

 **/

import com.myname.myapp.R;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static xserver.app.XServerSDK.ANDROID_DEVICE_TOKEN;
import static xserver.app.XServerSDK.TAG;
import static xserver.app.XServerSDK.pushMessage;
import static xserver.app.XServerSDK.pushType;

//-----------------------------------------------
// MARK - FCM SERVICE
//-----------------------------------------------
public class _FCMService extends FirebaseMessagingService {
   @Override
   public void onMessageReceived(RemoteMessage remoteMessage) {
      if (remoteMessage.getData().size() > 0) {
         // Log.i(TAG, "PUSH PAYLOAD: " + remoteMessage.getData());

         JSONObject pushData = new JSONObject(remoteMessage.getData());
         pushType = pushData.optString("pushType");
         pushMessage = pushData.optString("body");
         // Log.i(TAG, "FCM SERVICE -> PUSH RECEIVED - pushType: " + pushType);
         // Log.i(TAG, "FCM SERVICE -> PUSH RECEIVED - pushMessage: " + pushMessage);

         Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
         NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                 .setContentText(pushMessage)
                 .setStyle(new NotificationCompat.BigTextStyle())
                 .setAutoCancel(true)
                 .setSmallIcon(R.mipmap.ic_launcher)
                 .setSound(defaultSoundUri);
         NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         assert notificationManager != null;
         notificationManager.notify(0 , notificationBuilder.build());

         // NotificationCenter
         NotificationCenter.defaultCenter().postNotification();

      }
   }


   @Override
   public void onNewToken(@NotNull String token) {
      Log.i(TAG, "Refreshed token: " + token);
      ANDROID_DEVICE_TOKEN = token;
   }
}


//-----------------------------------------------
// MARK - NOTIFICATION CENTER
//-----------------------------------------------
class NotificationCenter {
   private static NotificationCenter _instance;
   private HashMap<String, ArrayList<Runnable>> registredObjects;
   private NotificationCenter(){
      registredObjects = new HashMap<>();
   }
   static synchronized NotificationCenter defaultCenter(){
      if(_instance == null)
         _instance = new NotificationCenter();
      return _instance;
   }
   synchronized void addFunctionForNotification(Runnable r){
      ArrayList<Runnable> list = registredObjects.get("pushNotif");
      if(list == null) {
         list = new ArrayList<>();
         registredObjects.put("pushNotif", list);
      }
      list.add(r);
   }
   public synchronized void removeFunctionForNotification(String notificationName, Runnable r){
      ArrayList<Runnable> list = registredObjects.get(notificationName);
      if(list != null) {
         list.remove(r);
      }
   }
   synchronized void postNotification(){
      ArrayList<Runnable> list = registredObjects.get("pushNotif");
      if(list != null) {
         for(Runnable r: list)
            r.run();
      }
   }

}


