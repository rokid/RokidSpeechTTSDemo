package com.rokid.rokidspeechttsdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rokid.rokidspeechttsdemo.utils.LogUtil;
import com.rokid.speech.OpusPlayer;
import com.rokid.speech.PrepareOptions;
import com.rokid.speech.Tts;
import com.rokid.speech.TtsCallback;
import com.rokid.speech.TtsOptions;

/**
 * Created by siokagami on 2018/3/5.
 */

public class TTSDemoActivity extends AppCompatActivity {

    private Tts tts;
    private EditText etText;
    private Button btnTestTts;
    private OpusPlayer opusPlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsdemo);
        initTTS();
        initView();
    }

    private void initView() {
        etText = findViewById(R.id.et_text);
        btnTestTts = findViewById(R.id.btn_test_tts);
        opusPlayer = new OpusPlayer();
        btnTestTts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testTTS(etText.getText().toString().trim());
            }
        });
    }

    private void initTTS() {
        // 创建tts实例并初始化
        tts = new Tts();
        PrepareOptions popts = new PrepareOptions();
        popts.host = "apigwws.open.rokid.com";
        popts.port = 443;
        popts.branch = "/api";
        // 认证信息，需要申请
        popts.key = Prepare.ROKID_KEY;
        popts.device_type_id = Prepare.ROKID_DEVICE_TYPE_ID;
        popts.secret = Prepare.ROKID_SECRET;
        // 设备名称，类似昵称，可自由选择，不影响认证结果
        popts.device_id = Prepare.ROKID_DEVICE_ID;
        tts.prepare(popts);
        // 在prepare后任意时刻，都可以调用config修改配置
        // 默认配置codec = "pcm", declaimer = "zh", samplerate = 24000
        // 下面的代码将codec修改为"opu2"，declaimer、samplerate保持原状不变
        TtsOptions topts = new TtsOptions();
        topts.set_codec("opu2");
        tts.config(topts);
    }

    private void testTTS(String s) {
        // 使用tts
        tts.speak(s,
                new TtsCallback() {
                    // 在这里实现回调接口 onStart, onVoice等
                    // 在onVoice中得到语音数据，调用播放器播放
                    @Override
                    public void onStart(int i) {
                        LogUtil.d(getResources().getString(R.string.text_onStart, i));
                    }

                    @Override
                    public void onText(int i, String s) {
                        LogUtil.d(getResources().getString(R.string.text_onText, i, s));
                    }

                    public void onVoice(int id, byte[] data) {
                        LogUtil.d(getResources().getString(R.string.text_onVoice, id));
                        opusPlayer.play(data);
                    }

                    @Override
                    public void onCancel(int i) {
                        LogUtil.d(getResources().getString(R.string.text_onCancel, i));
                    }

                    @Override
                    public void onComplete(int i) {
                        LogUtil.d(getResources().getString(R.string.text_onTTSComplete, i));
                    }

                    @Override
                    public void onError(int i, int i1) {
                        LogUtil.d(getResources().getString(R.string.text_onError, i, i1));
                    }
                });
    }

}
