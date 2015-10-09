package xyz.ghatdev.nanum;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MainService extends Service {
    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,int flag, int startid)
    {

        return START_STICKY;
    }
}
