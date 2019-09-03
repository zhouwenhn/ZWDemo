package zw.chowen.mediaextractordemo;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    //    String newVideoFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"testVideo.mp4";
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaExtractor = new MediaExtractor();
        Logger.getLogger("MainActivity").info("onCreate#SDCARD_PATH=" + SDCARD_PATH);

        findViewById(R.id.media_extractor).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                exactorMedia();
            }
        });


        findViewById(R.id.media_muxer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muxerMediaVideo();
            }
        });

        findViewById(R.id.media_muxer_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muxerMediaAudio();
            }
        });

        findViewById(R.id.media_combine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                combineVideo();
            }
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mediaProc();
//            }
//        }).start();

    }

    @TargetApi(Build.VERSION_CODES.N)
    private void muxerMediaAudio() {
        mediaExtractor = new MediaExtractor();
        int audioIndex = -1;

        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = getAssets().openFd("auth_open.mp4");
            mediaExtractor.setDataSource(assetFileDescriptor);
            int trackCount = mediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    audioIndex = i;
                }
            }
            mediaExtractor.selectTrack(audioIndex);
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(audioIndex);
            mediaMuxer = new MediaMuxer(SDCARD_PATH + "/output_audio", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeAudioIndex = mediaMuxer.addTrack(trackFormat);
            mediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            long stampTime = 0;
            //获取帧之间的间隔时间
            {
                mediaExtractor.readSampleData(byteBuffer, 0);
                if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    mediaExtractor.advance();
                }
                mediaExtractor.readSampleData(byteBuffer, 0);
                long secondTime = mediaExtractor.getSampleTime();
                mediaExtractor.advance();
                mediaExtractor.readSampleData(byteBuffer, 0);
                long thirdTime = mediaExtractor.getSampleTime();
                stampTime = Math.abs(thirdTime - secondTime);
                Log.e("fuck", stampTime + "");
            }

            mediaExtractor.unselectTrack(audioIndex);
            mediaExtractor.selectTrack(audioIndex);
            while (true) {
                int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }
                mediaExtractor.advance();

                bufferInfo.size = readSampleSize;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.offset = 0;
                bufferInfo.presentationTimeUs += stampTime;

                mediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, bufferInfo);
            }
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaExtractor.release();
            Log.e("fuck", "xxxxxfinish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private String SDCARD_PATH ="file:///android_asset/";

    @TargetApi(Build.VERSION_CODES.N)
    private void muxerMediaVideo() {
        mediaExtractor = new MediaExtractor();
        int videoIndex = -1;
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = getAssets().openFd("auth_next.mp4");
//            mediaExtractor.setDataSource(SDCARD_PATH + "/input.mp4");

            mediaExtractor.setDataSource(assetFileDescriptor);
            int trackCount = mediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoIndex = i;
                }
            }

            mediaExtractor.selectTrack(videoIndex);
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(videoIndex);
            mediaMuxer = new MediaMuxer(SDCARD_PATH + "/output_video", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int trackIndex = mediaMuxer.addTrack(trackFormat);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mediaMuxer.start();
            long videoSampleTime;
            {
                mediaExtractor.readSampleData(byteBuffer, 0);
                //skip first I frame
                if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC)
                    mediaExtractor.advance();
                mediaExtractor.readSampleData(byteBuffer, 0);
                long firstVideoPTS = mediaExtractor.getSampleTime();
                mediaExtractor.advance();
                mediaExtractor.readSampleData(byteBuffer, 0);
                long SecondVideoPTS = mediaExtractor.getSampleTime();
                videoSampleTime = Math.abs(SecondVideoPTS - firstVideoPTS);
                Log.d("fuck", "videoSampleTime is " + videoSampleTime);
            }

            mediaExtractor.unselectTrack(videoIndex);
            mediaExtractor.selectTrack(videoIndex);
            while (true) {
                int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }
                mediaExtractor.advance();
                bufferInfo.size = readSampleSize;
                bufferInfo.offset = 0;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.presentationTimeUs += videoSampleTime;

                mediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
            }
            mediaMuxer.stop();
            mediaExtractor.release();
            mediaMuxer.release();

            Log.e("TAG", "finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void exactorMedia() {
        FileOutputStream videoOutputStream = null;
        FileOutputStream audioOutputStream = null;
        try {
            File videoFile = new File(SDCARD_PATH, "/output_video.mp4");
            if (!videoFile.exists()) {
                videoFile.createNewFile();
            }

            File audioFile = new File(SDCARD_PATH, "/output_audio");

            videoOutputStream = new FileOutputStream(videoFile);
            audioOutputStream = new FileOutputStream(audioFile);

            AssetFileDescriptor assetFileDescriptor = null;
            assetFileDescriptor = getAssets().openFd("auth_next.mp4");
            mediaExtractor.setDataSource(assetFileDescriptor);
            int trackCount = mediaExtractor.getTrackCount();
            int audioTrackIndex = -1;
            int videoTrackIndex = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                Log.e("fuck", trackFormat.toString());
                String mineType = trackFormat.getString(MediaFormat.KEY_MIME);

                if (mineType.startsWith("video/")) {
                    videoTrackIndex = i;

                }

                if (mineType.startsWith("audio/")) {
                    audioTrackIndex = i;
                }
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            mediaExtractor.selectTrack(videoTrackIndex);
            while (true) {
                int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }

                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                videoOutputStream.write(buffer);
                byteBuffer.clear();
                mediaExtractor.advance();
            }

            mediaExtractor.selectTrack(audioTrackIndex);
            while (true) {
                int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }

                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                audioOutputStream.write(buffer);
                byteBuffer.clear();
                mediaExtractor.advance();
            }

            Log.e("fuck", "media extractor finish");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaExtractor.release();
            try {
                audioOutputStream.close();
                videoOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void combineVideo() {
        try {
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(SDCARD_PATH + "/output_video");
            MediaFormat videoFormat = null;
            int videoTrackIndex = -1;
            int videoTrackCount = videoExtractor.getTrackCount();
            for (int i = 0; i < videoTrackCount; i++) {
                videoFormat = videoExtractor.getTrackFormat(i);
                String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i;
                    break;
                }
            }

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(SDCARD_PATH + "/output_audio");
            MediaFormat audioFormat = null;
            int audioTrackIndex = -1;
            int audioTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                audioFormat = audioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }

            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            MediaMuxer mediaMuxer = new MediaMuxer(SDCARD_PATH + "/output/", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            long sampleTime = 0;
            {
                videoExtractor.readSampleData(byteBuffer, 0);
                if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    videoExtractor.advance();
                }
                videoExtractor.readSampleData(byteBuffer, 0);
                long secondTime = videoExtractor.getSampleTime();
                videoExtractor.advance();
                long thirdTime = videoExtractor.getSampleTime();
                sampleTime = Math.abs(thirdTime - secondTime);
            }
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);

            while (true) {
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs += sampleTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }

            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }

                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs += sampleTime;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();

            Logger.getLogger("MainActivity").info("combine finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



//    @TargetApi(Build.VERSION_CODES.N)
//    private void mediaProc() {
//        MediaExtractor extractor = new MediaExtractor();
//        AssetFileDescriptor assetFileDescriptor = null;
//        try {
//            assetFileDescriptor = getAssets().openFd("auth_next.mp4");
//            extractor.setDataSource(assetFileDescriptor);
//            int mediaCount = extractor.getTrackCount();
//            for (int i = 0; i < mediaCount; i++) {
//                MediaFormat format = extractor.getTrackFormat(i);
//                extractor.selectTrack(i);
//
//                MediaMuxer mediaMuxer = new MediaMuxer(newVideoFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//                int videoTrackIndex = mediaMuxer.addTrack(format);
//                mediaMuxer.start();
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
