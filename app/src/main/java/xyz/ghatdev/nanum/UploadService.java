package xyz.ghatdev.nanum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.PriorityQueue;
import java.util.Random;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import microsoft.aspnet.signalr.client.hubs.HubConnection;

public class UploadService extends Service {

    private final String serverUrl = "http://api.ghatdev.xyz/File/Upload";


    public UploadService() {
    }

    private String filename;
    private String type;
    private int k=0;
    private NotificationManager notificationManager;
    private Notification.Builder nbuilder;
    private int nId=0;
    private EventBus bus = EventBus.getDefault();

    private HubConnection connection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private  MyHandler handler = new MyHandler();
    private Intent i=null;

    @Override
    public int onStartCommand(Intent intent,int flag,int startid)
    {
        super.onStartCommand(intent, flag, startid);
        i=intent;
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, UploadActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        nbuilder = new Notification.Builder(this);

        nbuilder.setSmallIcon(R.drawable.ic_uploads);
        nbuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_uploadslarge));
        nbuilder.setTicker(getString(R.string.text_uploading));
        nbuilder.setContentTitle(getString(R.string.text_uploading));
        nbuilder.setContentIntent(pendingIntent);
        nbuilder.setProgress(0, 0, true);
        nbuilder.setContentText(getString(R.string.text_uploading));
        nbuilder.setAutoCancel(false);


        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        try{

            Realm.setDefaultConfiguration(realmConfiguration);
            Realm mdb = Realm.getDefaultInstance();
        }
        catch (RealmMigrationNeededException e)
        {
            Realm.migrateRealm(realmConfiguration);
        }


        filename = intent.getStringExtra("fname");
        type = intent.getStringExtra("type");

        if(filename!=null)
        {
            notificationManager.notify(nId,nbuilder.build());
            Thread uploadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Realm db = Realm.getDefaultInstance();
                    File file = new File(filename);
                    String result = "";
                    int r = getRandom(100000, 10);
                    db.beginTransaction();
                    FileData fileData = db.createObject(FileData.class);
                    fileData.setFileName(file.getName());
                    fileData.setUuid("");
                    fileData.setKey(r);
                    fileData.setPath(filename);
                    fileData.setType(type);
                    db.commitTransaction();
                    nbuilder.setContentText(file.getName());
                    try{

                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(serverUrl);
                        post.setHeader("Accept-Charset", "UTF-8");
                        ContentBody body = new FileBody(file);
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                        builder.addPart("file",body);
                        builder.addPart("filename",new StringBody(file.getName(),Charset.forName("UTF-8")));
                        post.setEntity(new ProgressHttpEntityWrapper(builder.build(),progressCallback));
                        HttpResponse response = client.execute(post);
                        HttpEntity entity = response.getEntity();

                        if(entity!=null)
                        {
                            result = EntityUtils.toString(entity);
                        }
                        db.beginTransaction();
                        fileData.setUuid(result);
                        db.commitTransaction();
                        Message message = handler.obtainMessage();
                        message.obj=result;
                        handler.sendMessage(message);
                        nId++;
                    }
                    catch (Exception e)
                    {
                        nbuilder.setContentTitle(getString(R.string.upload_error));
                        nbuilder.setContentIntent(PendingIntent.getService(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT));
                        nbuilder.setProgress(0,0,false);
                        nbuilder.setTicker(getString(R.string.upload_error));
                        notificationManager.notify(nId,nbuilder.build());
                        db.beginTransaction();
                        fileData.removeFromRealm();
                        db.commitTransaction();
                    }
                    db.refresh();
                    db.close();
                }
            });
            uploadThread.start();

        }


        return START_NOT_STICKY;
    }

    public int getRandom(int max, int offset) {

        Random mRand = new Random();

        int nResult = mRand.nextInt(max) + offset;

        return nResult;

    }



    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast toast;
            ClipData clipData;
            ClipboardManager myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            toast = Toast.makeText(UploadService.this,R.string.file_uploaded,Toast.LENGTH_SHORT);
            toast.show();
            clipData = ClipData.newPlainText("Nanum Link","http://api.ghatdev.xyz/File/Get/"+msg.obj);
            myClipboard.setPrimaryClip(clipData);
            BusEvent event = new BusEvent(100);
            bus.post(event);

        }
    }



    ProgressHttpEntityWrapper.ProgressCallback progressCallback = new ProgressHttpEntityWrapper.ProgressCallback() {
        @Override
        public void progress(float progress) {
            int p = (int)progress;
            if(p==100)
            {
                nbuilder.setAutoCancel(true)
                        .setContentTitle(getString(R.string.noti_uploadcomplete))
                        .setProgress(0, 0, false)
                        .setTicker(getString(R.string.noti_uploadcomplete));
                notificationManager.notify(nId, nbuilder.build());
                k=0;
            }
            else if(k<p) {
                nbuilder.setProgress(100, p, false);
                notificationManager.notify(nId, nbuilder.build());
                k=p;
            }
        }
    };

}
