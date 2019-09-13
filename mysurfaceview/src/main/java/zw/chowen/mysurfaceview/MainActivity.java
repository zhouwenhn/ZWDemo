package zw.chowen.mysurfaceview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MySurfaceView mySurfaceView = new MySurfaceView(this);
        setContentView(mySurfaceView);
    }
}
