package zw.chowen.audiorecordtrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.KeyEventDispatcher;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private Logger mLogger = Logger.getLogger("MainActivity");

    /**
     * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
     */
    public static final int SAMPLE_RATE_INHZ = 44100;

    /**
     * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
     */
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
     */
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    AudioRecord audioRecord;

    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode ==2000) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    mLogger.info(permissions[i]+"is PERMISSION_GRANTED");
                } else {
                    mLogger.info(permissions[i]+"is not PERMISSION_DENIED");
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
            }
        }


        findViewById(R.id.btn_audio_recorder_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });

        findViewById(R.id.btn_audio_recorder_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioRecord != null) {
                    isRecording = false;
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
            }
        });

        findViewById(R.id.btn_audio_track).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private boolean isRecording = true;

    private void startRecord() {
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, bufferSizeInBytes);
        final byte data[] = new byte[bufferSizeInBytes];
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "record.pcm");
        if (!file.mkdirs()) {
            mLogger.info("create file");
        }
        if (file.exists()) {
            mLogger.info("delete file");
            file.delete();
        }
        mLogger.info("file path="+file.getAbsolutePath());

        audioRecord.startRecording();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                     fos = new FileOutputStream(file);
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, data.length);
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            fos.write(data);
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null){
                        try{
                            fos.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }


            }
        }).start();
    }
}
