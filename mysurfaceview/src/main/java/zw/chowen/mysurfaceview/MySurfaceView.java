package zw.chowen.mysurfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by zw on 2019/9/8 11:38
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private boolean mIsDrawing;
    private SurfaceHolder mSufaceHolder;

    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mSufaceHolder = getHolder();
        mSufaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new MyThread().start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    private class MyThread extends Thread {
        Canvas canvas;
        long second;
        @Override
        public void run() {
            while (mIsDrawing) {
                try {
                    //锁定画布，并创建画布
                    canvas = mSufaceHolder.lockCanvas();
                    canvas.drawColor(Color.WHITE);
                    //创建画笔
                    Paint paint = new Paint();
                    paint.setColor(Color.BLUE);
                    paint.setTextSize(60);
                    canvas.drawText("计时器"+ (++second) +"秒",200, 340, paint);
                    Thread.sleep(1000);
                    paint.setColor(Color.GRAY);
                    canvas.drawLine(200, 360, 200, 900, paint);
                    canvas.drawCircle(200, 550, 60, paint);
                    //....可以在工作线程绘制各种图形
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //解锁画布并提交画布显示
                    if (canvas != null) {
                        mSufaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
