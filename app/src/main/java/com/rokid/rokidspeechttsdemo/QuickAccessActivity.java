package com.rokid.rokidspeechttsdemo;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.rance.chatui.adapter.ChatAdapter;
import com.rance.chatui.enity.FullImageInfo;
import com.rance.chatui.enity.MessageInfo;
import com.rance.chatui.ui.activity.ChatUIActivity;
import com.rance.chatui.ui.activity.FullImageActivity;
import com.rance.chatui.util.Constants;
import com.rance.chatui.util.GlobalOnItemClickManagerUtils;
import com.rance.chatui.util.MediaManager;
import com.rance.chatui.widget.EmotionInputDetector;
import com.rance.chatui.widget.NoScrollViewPager;
import com.rance.chatui.widget.StateButton;
import com.rokid.rokidspeechttsdemo.utils.FileUtil;
import com.rokid.rokidspeechttsdemo.utils.LogUtil;
import com.rokid.speech.PrepareOptions;
import com.rokid.speech.Speech;
import com.rokid.speech.SpeechCallback;
import com.rokid.speech.SpeechOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by siokagami on 2018/3/6.
 */

public class QuickAccessActivity extends AppCompatActivity {

    private EasyRecyclerView chatList;
    private ImageView emotionVoice;
    private EditText editText;
    private TextView voiceText;
    private ImageView emotionButton;
    private ImageView emotionAdd;
    private StateButton emotionSend;
    private RelativeLayout emotionLayout;
    private NoScrollViewPager viewpager;

    private EmotionInputDetector mDetector;
    private LinearLayoutManager layoutManager;
    private ChatAdapter chatAdapter;
    private List<MessageInfo> messageInfos;

    private Speech speech;
    private SpeechCallback speechCallback;

    //录音相关
    int animationRes = 0;
    int res = 0;
    AnimationDrawable animationDrawable = null;
    private ImageView animView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_access);
        initView();
        EventBus.getDefault().register(QuickAccessActivity.this);
        initChatView();
        initSpeech();
        initSpeechCallback();
    }

    private void initView() {
        chatList = findViewById(com.rance.chatui.R.id.chat_list);
        emotionVoice = findViewById(com.rance.chatui.R.id.emotion_voice);
        editText = findViewById(com.rance.chatui.R.id.edit_text);
        voiceText = findViewById(com.rance.chatui.R.id.voice_text);
        emotionButton = findViewById(com.rance.chatui.R.id.emotion_button);
        emotionAdd = findViewById(com.rance.chatui.R.id.emotion_add);
        emotionSend = findViewById(com.rance.chatui.R.id.emotion_send);
        emotionLayout = findViewById(com.rance.chatui.R.id.emotion_layout);
        viewpager = findViewById(com.rance.chatui.R.id.viewpager);
    }

    private void initChatView() {
        mDetector = EmotionInputDetector.with(this)
                .setEmotionView(emotionLayout)
                .setViewPager(viewpager)
                .bindToContent(chatList)
                .bindToEditText(editText)
                .bindToEmotionButton(emotionButton)
                .bindToAddButton(emotionAdd)
                .bindToSendButton(emotionSend)
                .bindToVoiceButton(emotionVoice)
                .bindToVoiceText(voiceText)
                .build();

        GlobalOnItemClickManagerUtils globalOnItemClickListener = GlobalOnItemClickManagerUtils.getInstance(this);
        globalOnItemClickListener.attachToEditText(editText);

        chatAdapter = new ChatAdapter(this);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatList.setLayoutManager(layoutManager);
        chatList.setAdapter(chatAdapter);
        messageInfos = new ArrayList<>();
        chatAdapter.addAll(messageInfos);
        chatAdapter.notifyDataSetChanged();
        chatAdapter.addItemClickListener(itemClickListener);

    }

    private ChatAdapter.onItemClickListener itemClickListener = new ChatAdapter.onItemClickListener() {
        @Override
        public void onHeaderClick(int position) {
            Toast.makeText(QuickAccessActivity.this, "onHeaderClick", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onImageClick(View view, int position) {
            int location[] = new int[2];
            view.getLocationOnScreen(location);
            FullImageInfo fullImageInfo = new FullImageInfo();
            fullImageInfo.setLocationX(location[0]);
            fullImageInfo.setLocationY(location[1]);
            fullImageInfo.setWidth(view.getWidth());
            fullImageInfo.setHeight(view.getHeight());
            fullImageInfo.setImageUrl(messageInfos.get(position).getImageUrl());
            EventBus.getDefault().postSticky(fullImageInfo);
            startActivity(new Intent(QuickAccessActivity.this, FullImageActivity.class));
            overridePendingTransition(0, 0);
        }

        @Override
        public void onVoiceClick(final ImageView imageView, final int position) {
            if (animView != null) {
                animView.setImageResource(res);
                animView = null;
            }
            switch (messageInfos.get(position).getType()) {
                case 1:
                    animationRes = com.rance.chatui.R.drawable.voice_left;
                    res = com.rance.chatui.R.mipmap.icon_voice_left3;
                    break;
                case 2:
                    animationRes = com.rance.chatui.R.drawable.voice_right;
                    res = com.rance.chatui.R.mipmap.icon_voice_right3;
                    break;
            }
            animView = imageView;
            animView.setImageResource(animationRes);
            animationDrawable = (AnimationDrawable) imageView.getDrawable();
            animationDrawable.start();
            MediaManager.playSound(messageInfos.get(position).getFilepath(), new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    animView.setImageResource(res);
                }
            });
        }
    };

    private void initSpeechCallback() {
        speechCallback = new SpeechCallback() {
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
                LogUtil.d("onComplete" + s + " " + s1);
                //需要在主线程更新ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageInfo message = new MessageInfo();
                        message.setContent(s);
                        message.setType(Constants.CHAT_ITEM_TYPE_LEFT);
                        message.setHeader("http://tupian.enterdesk.com/2014/mxy/11/2/1/12.jpg");
                        messageInfos.add(message);
                        chatAdapter.add(message);
                        chatList.scrollToPosition(chatAdapter.getCount() - 1);
                        chatAdapter.notifyDataSetChanged();
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
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageEventBus(final MessageInfo messageInfo) {
        messageInfo.setHeader("http://img.dongqiudi.com/uploads/avatar/2014/10/20/8MCTb0WBFG_thumb_1413805282863.jpg");
        messageInfo.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
        messageInfo.setSendState(Constants.CHAT_ITEM_SENDING);
        messageInfos.add(messageInfo);
        chatAdapter.add(messageInfo);
        chatList.scrollToPosition(chatAdapter.getCount() - 1);
        messageInfo.setSendState(Constants.CHAT_ITEM_SEND_SUCCESS);
        chatAdapter.notifyDataSetChanged();
        if (messageInfo.getContent() != null ) {
            textSpeechRequest(messageInfo.getContent());
        } else {
            voiceSpeechRequest(FileUtil.file2Byte(messageInfo.getFilepath()));

        }


    }

    private void initSpeech() {
        speech = new Speech();
        SpeechOptions opts = new SpeechOptions();
        opts.set_codec("pcm");
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
        Speech.VoiceOptions voiceOptions = new Speech.VoiceOptions();
        speech.prepare(prepareOptions);

    }

    private void textSpeechRequest(String s) {
        speech.putText(s, speechCallback);
    }

    private void voiceSpeechRequest(byte[] voiceByte) {
        int speechId = speech.startVoice(speechCallback);
        LogUtil.d("putVoice");
        speech.putVoice(speechId, voiceByte);
        LogUtil.d("endVoice");
        speech.endVoice(speechId);
        LogUtil.d("endVoiceFinish");
    }


}
