package xyz.ghatdev.nanum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent service = new Intent(MainActivity.this, UploadService.class);
        startService(service);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();

    }

}

