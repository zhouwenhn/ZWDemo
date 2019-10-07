package zw.chowen.binderpool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;

import java.util.logging.Logger;

/**
 * Created by zhouwen on 2019-10-06 22:30
 */
public class BinderPoolService extends Service {
    private static Logger LOGGER = Logger.getLogger("BinderPoolService");
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LOGGER.info("chowen#onBind=" + (IBinder) BinderPoolManager.getIns(this).getBinder());
        //返回获取各模块IBinder的AIDL接口
        return (IBinder) BinderPoolManager.getIns(this).getBinder();
    }

    @Override
    public void onCreate() {
        LOGGER.info("chowen#onCreate");
        LOGGER.severe("chowen#process=" + Process.myPid());
        super.onCreate();

    }


    @Override
    public void onDestroy() {
        LOGGER.info("chowen#onDestroy");
        super.onDestroy();
        BinderPoolManager.getIns(this).unbindService(this);
    }

}
