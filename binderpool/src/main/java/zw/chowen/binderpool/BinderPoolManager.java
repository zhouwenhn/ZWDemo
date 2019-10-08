package zw.chowen.binderpool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.logging.Logger;

/**
 * Created by zhouwen on 2019-10-06 22:24
 * Binder连接池实例
 */
public class BinderPoolManager {

    private static Logger LOGGER = Logger.getLogger("BinderPoolManager");
    private static BinderPoolManager sBinderPoolManager;

    private IBinderPoolInterface mIBinderPoolInterface;
    private Context mContext;

    public BinderPoolManager(Context context) {
        mContext = context.getApplicationContext();
        bindService(context.getApplicationContext());
    }

    public static BinderPoolManager getIns(Context context) {
        if (sBinderPoolManager == null) {
            synchronized (BinderPoolManager.class) {
                if (sBinderPoolManager == null) {
                    sBinderPoolManager = new BinderPoolManager(context);
                }
            }
        }

        return sBinderPoolManager;
    }


    public class IMyAidlInterfaceImpl extends zw.chowen.binderpool.IMyAidlInterface.Stub {

        @Override
        public String getValue() throws RemoteException {
            return "chowen#this is IMyAidlInterface";
        }
    }

    public class IMyAidlInterfaceImpl2 extends IMyAidlInterface2.Stub {

        @Override
        public int add(int a, int b) throws RemoteException {
            LOGGER.info("chowen#a and b >>" + (a + b));
            return a+b;
        }
    }

    public IBinder queryInterface(int type) {
        try {
            LOGGER.info("chowen#queryInterface#mIBinderPoolInterface>>" + mIBinderPoolInterface);
            if (mIBinderPoolInterface != null) {
                return mIBinderPoolInterface.queryInterface(type);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    public class BinderPoolServiceImpl extends IBinderPoolInterface.Stub {

        public BinderPoolServiceImpl() {
            super();
        }

        @Override
        public IBinder queryInterface(int type) throws RemoteException {
            IBinder binder = null;
            switch (type) {
                case 1:
                    binder = new IMyAidlInterfaceImpl();
                    break;
                case 2:
                    binder = new IMyAidlInterfaceImpl2();
                    break;
            }
            return binder;
        }
    }

    public synchronized void bindService(Context context) {
      Intent intent = new Intent(context, BinderPoolService.class);
      context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public IBinderPoolInterface getBinder() {
        return new BinderPoolServiceImpl();
    }

    // process has already died
    private Binder.DeathRecipient deathRecipient = new Binder.DeathRecipient(){

        @Override
        public void binderDied() {
            LOGGER.info("chowen#binderDied");
            mIBinderPoolInterface.asBinder().unlinkToDeath(deathRecipient, 0);
            mIBinderPoolInterface = null;
            bindService(mContext);
        }
    };


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIBinderPoolInterface = IBinderPoolInterface.Stub.asInterface(service);
            LOGGER.info("chowen#onServiceConnected#mIBinderPoolInterface="+ mIBinderPoolInterface);

            try {
                mIBinderPoolInterface.asBinder().linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LOGGER.info("chowen#onServiceDisconnected");
        }
    };

    public void unbindService(Context context){
        context.unbindService(serviceConnection);
    }
}
