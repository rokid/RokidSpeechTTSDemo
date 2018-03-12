package com.rance.chatui.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class AudioRecorderUtils {

    private String filePath;

    private String folderPath;

    private final String TAG = "RokidSpeechTTSDemo";

    private long startTime;
    private long endTime;
    private long totalTime;

    private int frequency = 16000;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder;
    private boolean isRecording = false;
    private byte[] buffer;
    private Thread recordThread;
    private OnAudioStatusUpdateListener audioStatusUpdateListener;

    public AudioRecorderUtils() {
        this(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/files/");
    }

    public AudioRecorderUtils(String folderPath) {
        File path = new File(folderPath);
        if (!path.exists())
            path.mkdirs();
        this.folderPath = folderPath;
    }

    public void startRecord(Context context) {
        if (!CheckPermissionUtils.isHasPermission(context)) {
            audioStatusUpdateListener.onError();
            return;
        }
        recordThread = new Thread() {
            @Override
            public void run() {
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfig, audioEncoding);
                buffer = new byte[bufferSize];
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfig, audioEncoding, bufferSize);
                //在这里我们创建一个文件，用于保存录制内容
                File fPath = new File(folderPath);
                fPath.mkdirs();//创建文件夹
                filePath = folderPath + Utils.getCurrentTime() + ".pcm";
                writeToFile();
            }
        };
        recordThread.start();


    }

    public long stopRecord() {
        isRecording = false;
        if (recordThread != null) {
            try {
                recordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return totalTime;
    }

    public void cancelRecord() {
        if (recorder != null) {
            recorder.release();
            recorder.stop();
            recorder = null;
        }
        if (recordThread != null) {
            try {
                recordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File file = new File(filePath);
        if (file.exists())
            file.delete();
        filePath = "";

    }

//    private void updateMicStatus() {
//
//        if (recorder != null) {
//            double ratio = (double) recorder.get / BASE;
//            double db = 0;// 分贝
//            if (ratio > 1) {
//                db = 20 * Math.log10(ratio);
//                if (null != audioStatusUpdateListener) {
//                    audioStatusUpdateListener.onUpdate(db, System.currentTimeMillis() - startTime);
//                }
//            }
//            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
//        }
//    }


    private void writeToFile() {
        try {

            startTime = System.currentTimeMillis();
            Log.d(TAG, "startTime" + startTime);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filePath))));
            recorder.startRecording();
            isRecording = true;

            while (isRecording) {
                //从bufferSize中读取字节，返回读取的short个数
                //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，TODO：待解决
                int bufferReadResult = recorder.read(buffer, 0, buffer.length);
                //循环将buffer中的音频数据写入到OutputStream中
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.write(buffer[i]);
                }
            }
            //录制结束
            totalTime = doStopRecord();
            dos.close();
        } catch (Exception e) {

        }
    }

    private long doStopRecord() {
        if (recorder == null)
            return 0L;
        endTime = System.currentTimeMillis();
        try {
            recorder.stop();
        } catch (IllegalStateException e) {
            Log.d("stopRecord", e.getMessage());
        } catch (RuntimeException e) {
            Log.d("stopRecord", e.getMessage());
        } catch (Exception e) {
            Log.d("stopRecord", e.getMessage());
        }
        recorder.release();
        recorder = null;
        long time = endTime - startTime;
        audioStatusUpdateListener.onStop(time, filePath);
        filePath = "";
        return endTime - startTime;
    }

    public void setOnAudioStatusUpdateListener(OnAudioStatusUpdateListener audioStatusUpdateListener) {
        this.audioStatusUpdateListener = audioStatusUpdateListener;
    }

    public interface OnAudioStatusUpdateListener {
        /**
         * 录音中...
         *
         * @param db   当前声音分贝
         * @param time 录音时长
         */
        public void onUpdate(double db, long time);

        /**
         * 停止录音
         *
         * @param time     录音时长
         * @param filePath 保存路径
         */
        public void onStop(long time, String filePath);

        /**
         * 录音失败
         */
        public void onError();
    }
}


