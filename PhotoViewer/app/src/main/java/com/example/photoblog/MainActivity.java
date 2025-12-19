package com.example.photoblog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.view.View;

import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {

    ImageView imgView;
    TextView textView;
    String site_url = "http://10.0.2.2:8000";
    // token을 멤버 변수로 이동하여 다른 내부 클래스에서도 사용할 수 있도록 합니다.
    String token = "bf46b8f9337d1d27b4ef2511514c798be1a954b8";
    JSONObject post_json;
    String imagUrl = null;
    Bitmap bmImg = null;

    CloadImage taskDonwload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("FCM_TEST", "토큰 가져오기 실패", task.getException());
                    return;
                }

                // 1. 토큰 가져오기 성공!
                String fcmToken = task.getResult();

                // 2. 로그창에 출력 (Logcat에서 FCM_TEST 검색해서 복사하세요!)
                Log.d("FCM_TEST", "내 기기 토큰: " + fcmToken);

                // 3. 눈으로 확인하기 쉽게 화면에도 잠깐 띄워줌
                Toast.makeText(MainActivity.this, "토큰 확인됨", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void onClickDownload(View v) {
        if(taskDonwload != null && taskDonwload.getStatus() == AsyncTask.Status.RUNNING){
            taskDonwload.cancel(true);
        }
        taskDonwload= new CloadImage();
        taskDonwload.execute(site_url+"/api_root/Post/");
        Toast.makeText(getApplicationContext(),"Download",Toast.LENGTH_LONG).show();
    }

    private class CloadImage extends AsyncTask<String, Integer, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmapList = new ArrayList<>();
            HttpURLConnection conn = null;
            try {
                String apiUrl = urls[0];
                URL urlAPI = new URL(apiUrl);
                conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token); // 멤버 변수 token 사용
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close(); // reader를 닫아줍니다.

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);

                    for (int i = 0; i < aryJson.length(); i++) {
                        post_json = (JSONObject) aryJson.get(i);
                        String imageUrl = post_json.getString("image");

                        // "null" 문자열이거나 비어있는지 확인합니다.
                        if (imageUrl != null && !imageUrl.equals("null") && !imageUrl.isEmpty()) {
                            URL myImageUrl = new URL(imageUrl);
                            // 이미지 다운로드를 위한 새 연결을 만듭니다.
                            HttpURLConnection imgConn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = imgConn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
                            bitmapList.add(imageBitmap);
                            imgStream.close();
                            imgConn.disconnect(); // 이미지 연결은 여기서 닫습니다.
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if(conn != null){
                    conn.disconnect(); // 메인 연결은 여기서 닫습니다.
                }
            }
            return bitmapList;
        }

        @Override
        protected void onPostExecute(List<Bitmap> images){
            if(images.isEmpty()){
                textView.setText("불러올 이미지가 없습니다.");
            }else{
                textView.setText("이미지 로드 성공!");
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                ImageAdapter adapter = new ImageAdapter(images);

                recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }
    }
}
