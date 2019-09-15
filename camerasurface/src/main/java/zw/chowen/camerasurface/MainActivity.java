package zw.chowen.camerasurface;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    Logger mLogger = Logger.getLogger("MainActivity");
    SurfaceHolder mSurfaceHolder;
    SurfaceView mSurfaceView;
    Camera camera;

    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        procPermissions();

        mSurfaceView = findViewById(R.id.sv);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogger.info("autoFocus");
                camera.autoFocus(null);
            }
        });
        findViewById(R.id.btn_start_focus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
                    @Override
                    public void onAutoFocusMoving(boolean start, Camera camera) {
                        mLogger.info("onAutoFocusMoving");
                    }
                });
            }
        });

        findViewById(R.id.btn_start_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        mLogger.info("onAutoFocus#success=" + success);
                        if (success) {
                            startCamera();
                        }
                    }
                });
            }
        });
        findViewById(R.id.btn_stop_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseCamera();
            }
        });
    }

    private void procPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    mLogger.info("onRequestPermissionsResult#PERMISSION_DENIED =" + permissions[i]);
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setParameters() {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(ImageFormat.NV21);
        parameters.setRotation(90);
        camera.setParameters(parameters);

        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_ALARMS), "camera");
            mLogger.info("file path=" + file.getAbsolutePath());
            fos = new FileOutputStream(file);
            if (!file.mkdirs()) {
                mLogger.info("not create");
            }
            if (file.exists()) {
                file.delete();
            }
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mLogger.info("onPreviewFrame#data=" + data);
                    //处理NV21数据
//                    try {
//                        fos.write(data);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        releaseCamera();
        super.onPause();
    }


    FileOutputStream fos = null;

    private void startCamera() {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        setParameters();
        try {
            camera.setPreviewDisplay(mSurfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setAutoFocusMoveCallback(null);
            camera.cancelAutoFocus();
            camera.release();

            camera = null;
        }

        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
