package zw.chowen.com;


import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.util.logging.Logger;

/**
 * Created by zhouwen on 2019-10-05 11:12
 */
public class MessagerService extends Service {


    private Messenger messenger = new Messenger(new SHandler());
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.getLogger("MessagerService").severe("MessagerService#"+ Process.myPid());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private class SHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 1:
                    Bundle bundle2 = msg.getData();
                    Logger.getLogger("MessagerService").severe("data>>>>"+ bundle2.getString("datac"));
                    Messenger clientMessenger = msg.replyTo;
                    Bundle bundle = new Bundle();
                    bundle.putString("datas", "from service");
                    Message message  =  Message.obtain(null, 2);
                    message.setData(bundle);
                    message.replyTo = messenger;
                    try {
                        clientMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
