package com.example.photoblog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.app.TaskStackBuilder;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";

    // 1. 앱을 새로 깔거나 토큰이 갱신될 때 실행됨
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "새 토큰 발급됨: " + token);
        // 나중에 여기서 서버로 토큰을 보내는 코드를 추가할 수 있습니다.
    }

    // 2. 알림(메시지)이 왔을 때 실행됨
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "알림 도착! 발신자: " + remoteMessage.getFrom());

        // 알림 내용이 있으면 화면에 표시
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Uri imageUri = remoteMessage.getNotification().getImageUrl();
            String imageUrl = (imageUri != null) ? imageUri.toString() : null;
            sendNotification(title, body, imageUrl);
        }

    }

    // 알림을 화면에 예쁘게 띄워주는 함수
    private void sendNotification(String title, String messageBody, String imageUrl) {
        Intent intent = new Intent(this, AlertDetailsActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // TaskStackBuilder가 이 역할을 대신합니다.

        // ★ 수정 2: 새 화면에 전달할 데이터 담기 (이름표를 잘 기억하세요!)
        intent.putExtra("title", title);
        intent.putExtra("body", messageBody);
        intent.putExtra("imageUrl", imageUrl);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(new Intent(this, MainActivity.class));
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        String channelId = "TheftAlertChannel"; // 채널 ID
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher) // 아이콘 (기본)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 안드로이드 8.0(Oreo) 이상은 채널 설정이 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "도난 경보 알림",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}