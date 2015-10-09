package xyz.ghatdev.nanum;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

public class MainService extends Service {
    public MainService() {
    }

    private static final int ANIMATION_FRAME_RATE = 30;	// Animation frame rate per second.

    private WindowManager windowManager;
    private ImageView appHead;
    private WindowManager.LayoutParams mParams;
    private Timer mTrayAnimationTimer;
    private TrayAnimationTimerTask 	mTrayTimerTask;
    private Handler mAnimationHandler = new Handler();
    private int mPrevDragX;
    private int mPrevDragY;
    private int mStartDragX;
    private boolean mIsTrayOpen = false;
    private float deltaX;
    private float deltaY;
    private ImageView uploadBtn;
    private WindowManager.LayoutParams btnParams;
    private boolean isOpen = false;

    private SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,int flag, int startid)
    {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        try {

            Realm.setDefaultConfiguration(realmConfiguration);
            Realm mdb = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            Realm.migrateRealm(realmConfiguration);
        }

        return START_STICKY_COMPATIBILITY;
    }

    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            final int action = event.getActionMasked();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    if(isOpen)
                    {
                        uploadBtn.setVisibility(View.INVISIBLE);
                    }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragTray(action, (int)event.getRawX(), (int)event.getRawY());
                    break;
                default:
                    return true;
            }

            return false;
        }
    };

    private ImageView.OnClickListener mViewClickListener = new ImageView.OnClickListener() {

        @Override
        public void onClick(View v) {


            if(mParams.x>0)
            {
                btnParams.x=mParams.x-appHead.getWidth()-10;
            }
            else if(mParams.x<0)
            {
                btnParams.x=mParams.x+appHead.getWidth()+10;
            }
            btnParams.y=mParams.y;
            windowManager.updateViewLayout(uploadBtn,btnParams);
            if(isOpen)
            {
                uploadBtn.setVisibility(View.INVISIBLE);
                isOpen=false;
            }
            else
            {
                uploadBtn.setVisibility(View.VISIBLE);
                isOpen=true;
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        int screenWidth=getResources().getDisplayMetrics().widthPixels;
        preferences = getSharedPreferences("default",MODE_PRIVATE);


        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        uploadBtn = new ImageView(MainService.this);
        uploadBtn.setImageResource(R.mipmap.btn_upload);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        appHead = new ImageView(this);
        appHead.setImageResource(R.mipmap.ic_launcher);
        appHead.setOnTouchListener(mViewTouchListener);
        appHead.setOnClickListener(mViewClickListener);
        appHead.setClickable(true);

        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        mParams.x=preferences.getInt("headX", (screenWidth/2)-(appHead.getWidth()/2)+(appHead.getWidth()/3));
        mParams.y=preferences.getInt("headY",0);

        btnParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        if(mParams.x<0)
        {
            btnParams.x=mParams.x+appHead.getWidth()*3/2;
        }
        else
        {
            btnParams.x=mParams.x-appHead.getWidth()*3/2;
        }
        btnParams.y=mParams.y;

        uploadBtn.setVisibility(View.INVISIBLE);

        windowManager.addView(appHead, mParams);
        windowManager.addView(uploadBtn,btnParams);
    }

    private void dragTray(int action, int x, int y){
        switch (action){
            case MotionEvent.ACTION_DOWN:

                // Cancel any currently running animations/automatic tray movements.
                if (mTrayTimerTask!=null){
                    mTrayTimerTask.cancel();
                    mTrayAnimationTimer.cancel();
                }

                // Store the start points
                mStartDragX = x;
                //mStartDragY = y;
                mPrevDragX = x;
                mPrevDragY = y;
                break;

            case MotionEvent.ACTION_MOVE:

                // Calculate position of the whole tray according to the drag, and update layout.
                deltaX = x-mPrevDragX;
                deltaY = y-mPrevDragY;
                mParams.x += deltaX;
                mParams.y += deltaY;
                mPrevDragX = x;
                mPrevDragY = y;
                windowManager.updateViewLayout(appHead, mParams);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                // When the tray is released, bring it back to "open" or "closed" state.
                if ((mIsTrayOpen && (x-mStartDragX)<=0) ||
                        (!mIsTrayOpen && (x-mStartDragX)>=0))
                    mIsTrayOpen = !mIsTrayOpen;
                int dest=0;
                if(deltaX<-30)
                {
                    dest=0;
                }
                else if(deltaX>30)
                {
                    dest=1;
                }
                mTrayTimerTask = new TrayAnimationTimerTask(dest);

                mTrayAnimationTimer = new Timer();
                mTrayAnimationTimer.schedule(mTrayTimerTask, 0, ANIMATION_FRAME_RATE);
                break;
        }
    }

    private class TrayAnimationTimerTask extends TimerTask {

        // Ultimate destination coordinates toward which the tray will move
        int mDestX;
        int mDestY;

        public TrayAnimationTimerTask(int dest){

            // Setup destination coordinates based on the tray state.
            super();

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            if(dest==0)
            {
                mDestX=-(screenWidth/2)+(appHead.getWidth()/2)-(appHead.getWidth()/3);
            }
            else
            {
                mDestX = (screenWidth/2)-(appHead.getWidth()/2)+(appHead.getWidth()/3);
            }


            // Keep upper edge of the widget within the upper limit of screen
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            if(mParams.y>=(screenHeight/2)-(appHead.getHeight()*2))
            {
                mDestY=(screenHeight/2)-(appHead.getHeight()/2)-(appHead.getHeight()/3);
            }
            else if(mParams.y<=-(screenHeight/2)+(appHead.getHeight()*2))
            {
                mDestY = -(screenHeight/2)+(appHead.getHeight()/2)+(appHead.getHeight()/3);
            }
            else
            {
                mDestY=mParams.y;
            }

        }

        // This function is called after every frame.
        @Override
        public void run() {

            // handler is used to run the function on main UI thread in order to
            // access the layouts and UI elements.
            mAnimationHandler.post(new Runnable() {
                @Override
                public void run() {

                    // Update coordinates of the tray
                    mParams.x = (2 * (mParams.x - mDestX)) / 3 + mDestX;
                    mParams.y = (2 * (mParams.y - mDestY)) / 3 + mDestY;
                    windowManager.updateViewLayout(appHead, mParams);

                    // Cancel animation when the destination is reached
                    if (Math.abs(mParams.x - mDestX) < 2 && Math.abs(mParams.y - mDestY) < 2) {
                        preferences = getSharedPreferences("default",MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("headX",mParams.x);
                        editor.putInt("headY",mParams.y);
                        editor.commit();
                        TrayAnimationTimerTask.this.cancel();
                        mTrayAnimationTimer.cancel();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (appHead != null) windowManager.removeView(appHead);
    }
}
