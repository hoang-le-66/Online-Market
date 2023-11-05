package com.example.onlinemarket


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.onlinemarket.activities.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class MyFcmService:FirebaseMessagingService() {

    private companion object{
        private const val TAG = "MY_FCM_TAG"

        //Notification channel ID
        private const val NOTIFICATION_CHANNEL_ID = "MARKET_ONL_CHANNEL_ID"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = "${remoteMessage.notification?.title}"
        val body = "${remoteMessage.notification?.body}"
        
        val senderUid = "${remoteMessage.data["senderUid"]}"
        val notificationType = "${remoteMessage.data["notificationType"]}"

        Log.d(TAG, "onMessageReceived: title: $title")
        Log.d(TAG, "onMessageReceived: body: $body")
        Log.d(TAG, "onMessageReceived: senderUid: $senderUid")
        Log.d(TAG, "onMessageReceived: notificationType: $notificationType")

        showChatNotification(title, body, senderUid)
    }

    private fun showChatNotification(notificationTitle: String, notificationDescription: String, senderUid: String){
        //generate random int between 3000 to use as noti id
        val notificationId = Random().nextInt(3000)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //func to setup noti channel in case of Oreo and above
        setupNotificationChannel(notificationManager)
        //Intent to launch ChatAct when noti is clicked
        val intent = Intent(this, ChatActivity::class.java)

        intent.putExtra("receiptUid",senderUid)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //PendingIntent to add in notification
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_market)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())

    }
    private fun setupNotificationChannel(notificationManager: NotificationManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Chat Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Show chats notification"
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}