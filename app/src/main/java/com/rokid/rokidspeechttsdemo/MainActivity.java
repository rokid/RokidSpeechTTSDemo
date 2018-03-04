package com.rokid.rokidspeechttsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by siokagami on 2018/3/3.
 */

public class MainActivity extends AppCompatActivity {
    private Button btnSpeechDemo;
    private Button btnTtsDemo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnSpeechDemo = findViewById(R.id.btn_speech_demo);
        btnTtsDemo = findViewById(R.id.btn_tts_demo);
        btnSpeechDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SpeechDemoActivity.class));
            }
        });
        btnTtsDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TTSDemoActivity.class));
            }
        });

    }


}
