package zw.chowen.audiorecordtrack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MySurfaceView2 mySurfaceView2 = new MySurfaceView2(this);
        setContentView(mySurfaceView2);
    }
}
