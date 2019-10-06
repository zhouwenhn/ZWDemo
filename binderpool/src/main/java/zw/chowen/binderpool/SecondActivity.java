package zw.chowen.binderpool;

import android.app.Activity;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.util.logging.Logger;

/**
 * Created by zhouwen on 2019-10-06 23:13
 */
public class SecondActivity extends Activity {
    private static Logger LOGGER = Logger.getLogger("SecondActivity");
    private BinderPoolManager mBinderPoolManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mBinderPoolManager = BinderPoolManager.getIns(this);
        LOGGER.severe("chowen#process=" + Process.myPid());
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IBinder binder = mBinderPoolManager.queryInterface(2);
                IMyAidlInterface2 iMyAidlInterface2 = BinderPoolManager.IMyAidlInterfaceImpl2.asInterface(binder);
                LOGGER.info("chowen#iMyAidlInterfaceImpl2=" + iMyAidlInterface2);
                if (iMyAidlInterface2 != null) {
                    try {
                        LOGGER.info("chowen#a and b is>>>" + iMyAidlInterface2.add(10, 20));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 4000);
    }
}
