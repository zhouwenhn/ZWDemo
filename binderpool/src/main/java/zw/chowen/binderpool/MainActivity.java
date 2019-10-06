package zw.chowen.binderpool;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static Logger LOGGER = Logger.getLogger("MainActivity");
    private BinderPoolManager mBinderPoolManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBinderPoolManager = BinderPoolManager.getIns(this);
        LOGGER.severe("chowen#process=" + Process.myPid());

        findViewById(R.id.start_other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IBinder binder = mBinderPoolManager.queryInterface(1);
                IMyAidlInterface iMyAidlInterface1 = BinderPoolManager.IMyAidlInterfaceImpl.asInterface(binder);
                LOGGER.info("chowen#onResume#iMyAidlInterface1=" + iMyAidlInterface1);
                if (iMyAidlInterface1 != null) {
                    try {
                        LOGGER.info("chowen#onResume#value=" + iMyAidlInterface1.getValue());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 6000);

    }
}
