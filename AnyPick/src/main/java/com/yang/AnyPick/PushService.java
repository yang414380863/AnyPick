package com.yang.AnyPick;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.orhanobut.logger.Logger;
import com.yang.AnyPick.basic.ActivityCollector;
import com.yang.AnyPick.basic.Client;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.basic.MyApplication;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class PushService extends Service {

    private static boolean isRunning;
    //唤醒间隔
    private static final int TEN_MINIUTE=10*60*1000;
    private static final int TEN_SECOND=10*1000;
    private static int intervalTime;

    private String username;

    @Override
    public void onCreate() {
        super.onCreate();
        intervalTime=TEN_SECOND;
        isRunning=false;
        EventBus.getDefault().register( this );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning=true;
        username=intent.getExtras().getString("username");
        if (!username.equals("")){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d("check PushService");
                    new Client().sendForResult("checkPush "+username);
                }
            }).start();
            AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
            long time= SystemClock.elapsedRealtime()+intervalTime;//从开机到现在的毫秒数+唤醒间隔
            Intent intentToPushService=new Intent(this, PushService.class);
            intentToPushService.putExtra("username",username);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,time, PendingIntent.getService(this,1,intentToPushService,0));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning=false;
        EventBus.getDefault().unregister( this );
    }

    public static boolean isRunning(){
        return isRunning;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void judgePush ( String event){
        Logger.d("get push event: "+event);
        if (event!=""){
            //todo 跳转ListActivity 传Index 关闭其他活动
            Intent intent=new Intent(MyApplication.getContext(),ListActivity.class);
            //intent.addFlags()
        }
    }
}
