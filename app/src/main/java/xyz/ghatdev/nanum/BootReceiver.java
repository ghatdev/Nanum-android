package xyz.ghatdev.nanum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ghatdev on 2015. 10. 9..
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context,Intent intent)
    {
        Intent service = new Intent(context,MainService.class);
        context.startService(service);
    }
}
