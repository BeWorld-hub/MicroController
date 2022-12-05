package com.microcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private int mCurrentState;

    private ImageButton mStateButton;
    private TextView mStateName;

    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentState = 0;
        mStateButton = findViewById(R.id.button_record);
        mStateName = findViewById(R.id.textView);

        getPermissions();
    }

    public void handleButtonClick(View view) {
        if (mCurrentState == 0) {
            mStateButton.setImageResource(R.drawable.ic_baseline_mic_off_128);
            mStateName.setText("остановить");

            startRecord();
        } else if (mCurrentState == 1) {
            mStateButton.setImageResource(R.drawable.ic_baseline_play_arrow_128);
            mStateName.setText("прослушать");

            stopRecord();
        } else if (mCurrentState == 2) {
            mStateButton.setImageResource(R.drawable.ic_baseline_stop_128);
            mStateName.setText("остановить");

            playRecord();
        } else if (mCurrentState == 3) {
            mStateButton.setImageResource(R.drawable.ic_baseline_mic_128);
            mStateName.setText("записать");

            stopPlayingRecord();
        }

        setState(mCurrentState + 1);
    }

    private void setState(int numState) {
        mCurrentState = numState % 4;
    }

    private void startRecord() {
        try {
            mRecorder = new MediaRecorder();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(getPathRecord());
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void stopRecord() {
        mRecorder.stop();
        mRecorder.release();

        mRecorder = null;
    }

    private void playRecord() {
        try {
            mPlayer = new MediaPlayer();

            mPlayer.setDataSource(getPathRecord());
            mPlayer.prepare();

            mPlayer.setOnCompletionListener(mediaPlayer -> {
                mStateButton.setImageResource(R.drawable.ic_baseline_mic_128);
                mStateName.setText("записать");

                mPlayer.release();
                mPlayer = null;

                setState(0);
            });

            mPlayer.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void stopPlayingRecord() {
        mPlayer.stop();
        mPlayer.release();

        mPlayer = null;
    }

    private void getPermissions() {
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (writePermission == PackageManager.PERMISSION_DENIED || recordPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
            }, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200) {
            if (Objects.nonNull(grantResults) && (grantResults[0] != PackageManager.PERMISSION_GRANTED
                        || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {

                AlertDialog.Builder errorPermissionMassage = new AlertDialog.Builder(this);
                errorPermissionMassage.setMessage("Приложению нужно использовать Микрофон и Файлы для работы");
                errorPermissionMassage.setCancelable(false);

                errorPermissionMassage.show();
            }
        }
    }

    private String getPathRecord() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());

        File recordDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File record = new File(recordDirectory, "record" + ".mp3");

        return record.getPath();
    }

    private void removeRecord() {
        File file = new File(getPathRecord());
        file.delete();
    }

    @Override
    protected void onDestroy() {
        removeRecord();
        super.onDestroy();
    }
}
