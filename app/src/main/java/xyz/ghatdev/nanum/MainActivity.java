package xyz.ghatdev.nanum;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
        if(!isMyServiceRunning(this,"xyz.ghatdev.nanum.MainService")){
            startService(new Intent(this,MainService.class));
        }
    }

    private boolean isMyServiceRunning(Context ctx, String s_service_name) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (s_service_name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

