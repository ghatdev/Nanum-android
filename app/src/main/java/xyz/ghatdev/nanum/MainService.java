package xyz.ghatdev.nanum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

public class MainService extends Service {
    public MainService() {
    }



    private NotificationManager notificationManager;
    private Notification.Builder builder;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,int flag, int startid)
    {
        PendingIntent pendingIntent = PendingIntent.getService(this,0,new Intent(this,HeadService.class),PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.qmenu_title));
        builder.setContentText(getString(R.string.qmenu_text));
        builder.setSmallIcon(R.drawable.ic_noti);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(true);
        builder.setContentIntent(pendingIntent);

        startForeground(startid, builder.build());

        startService(new Intent(this,HeadService.class));

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        try {

            Realm.setDefaultConfiguration(realmConfiguration);
            Realm mdb = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            Realm.migrateRealm(realmConfiguration);
        }



        return START_STICKY;
    }


}
