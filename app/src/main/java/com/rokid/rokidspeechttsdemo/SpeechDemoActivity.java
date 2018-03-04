package com.rokid.rokidspeechttsdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rokid.rokidspeechttsdemo.utils.LogUtil;
import com.rokid.speech.PrepareOptions;
import com.rokid.speech.Speech;
import com.rokid.speech.SpeechCallback;
import com.rokid.speech.SpeechOptions;

/**
 * Created by siokagami on 2018/3/5.
 */

public class SpeechDemoActivity extends AppCompatActivity {

    private Speech speech;
    private TextView tvAsrResult;
    private TextView tvAsrBackResult;
    private EditText etSpeechText;
    private Button btnSpeechTextTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_demo);
        initSpeech();
        initView();

    }

    private void initView() {
        etSpeechText = findViewById(R.id.et_speech_text);
        btnSpeechTextTest = findViewById(R.id.btn_speech_text_test);
        tvAsrResult = findViewById(R.id.tv_asr_result);
        tvAsrBackResult = findViewById(R.id.tv_asr_back_result);
        btnSpeechTextTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textSpeechRequest(etSpeechText.getText().toString().trim());
            }
        });
    }

    private void initSpeech() {
        speech = new Speech();
        SpeechOptions opts = new SpeechOptions();
        opts.set_codec("opu");
        opts.set_lang("zh");
        speech.config(opts);
        PrepareOptions prepareOptions = new PrepareOptions();
        prepareOptions.host = "apigwws.open.rokid.com";
        prepareOptions.port = 443;
        prepareOptions.branch = "/api";
        prepareOptions.key = Prepare.ROKID_KEY;
        prepareOptions.device_type_id = Prepare.ROKID_DEVICE_TYPE_ID;
        prepareOptions.secret = Prepare.ROKID_SECRET;
        prepareOptions.device_id = Prepare.ROKID_DEVICE_ID;
        speech.prepare(prepareOptions);

    }

    private void textSpeechRequest(String s) {
        speech.putText(s, new SpeechCallback() {
            @Override
            public void onStart(int i) {
                LogUtil.d("onStart " + i);
            }

            @Override
            public void onIntermediateResult(int i, String s, String s1) {
                LogUtil.d("onIntermediateResult " + i + " " + s + " " + s1);
            }

            @Override
            public void onAsrComplete(int i, String s) {
                LogUtil.d("onAsrComplete " + i + " " + s);
            }

            @Override
            public void onComplete(int i, final String s, final String s1) {
                LogUtil.d("onComplete " + s + " " + s1);
                //需要在主线程更新ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //自然语义解析结果
                        tvAsrResult.setText(s);
                        //Rokid Speech skill返回的结果
                        tvAsrBackResult.setText(s1);
                    }
                });
            }

            @Override
            public void onCancel(int i) {
                LogUtil.d("onCancel " + i);
            }

            @Override
            public void onError(int i, int i1) {
                LogUtil.d("onError " + i + " " + i1);
            }
        });
    }


}
