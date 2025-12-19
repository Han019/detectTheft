package com.example.photoblog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // 아까 추가한 라이브러리

public class AlertDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvBody;
    private ImageView ivImage;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_details);

        // 1. 뷰 찾기
        tvTitle = findViewById(R.id.tvAlertTitle);
        tvBody = findViewById(R.id.tvAlertBody);
        ivImage = findViewById(R.id.ivAlertImage);
        btnConfirm = findViewById(R.id.btnConfirm);

        // 2. 알림에서 넘어온 데이터 받기 (Intent)
        // "title", "body", "imageUrl" 이라는 이름표로 데이터를 꺼냅니다.
        String title = getIntent().getStringExtra("title");
        String body = getIntent().getStringExtra("body");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // 3. 화면에 데이터 표시하기
        if (title != null) {
            tvTitle.setText(title);
        }
        if (body != null) {
            tvBody.setText(body);
        }

        // ★ 핵심: Glide로 이미지 URL 로딩하기
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl) // 서버가 준 URL
                    .placeholder(R.drawable.ic_launcher_foreground) // 로딩 중에 보여줄 임시 이미지
                    .error(R.drawable.ic_launcher_background) // 에러 났을 때 보여줄 이미지
                    .into(ivImage); // 이미지를 넣을 뷰
        } else {
            // 이미지가 없으면 이미지 영역 숨기기 (선택사항)
            // findViewById(R.id.cardViewImage).setVisibility(View.GONE);
        }

        // 4. 확인 버튼 누르면 화면 닫기
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 종료
            }
        });
    }
}
