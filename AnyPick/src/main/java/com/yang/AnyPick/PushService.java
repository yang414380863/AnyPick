package com.yang.AnyPick;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.orhanobut.logger.Logger;
import com.yang.AnyPick.activity.Login;
import com.yang.AnyPick.basic.ActivityCollector;
import com.yang.AnyPick.basic.Client;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.basic.MyApplication;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;


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
            //todo 对比时间
            //todo 显示通知
            //todo 点击通知 跳转ListActivity 传Index 关闭其他活动
            //intent.addFlags()
        }
    }

    private void showNotification(){
        Intent resultIntent = new Intent(this, ListActivity.class);
        //resultIntent.putExtra("index",json.getString("index"));
        //resultIntent.putExtra("date",json.getString("date"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, FLAG_UPDATE_CURRENT);
        NotificationManager manager=(NotificationManager) MyApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("New Update")//标题
                //.setContentText("From: "+json.getString("index"))//正文
                //.setWhen(pushDateCorrect.getTime().getTime())//通知发生的时间为服务器更新时间
                .setContentIntent(pendingIntent)//点击跳转intent
                .setAutoCancel(true)//点击之后自动消失
                .setSmallIcon(R.drawable.ic_plus_one_black_48dp)   //若没有设置largeicon，此为左边的大icon，设置了largeicon，则为右下角的小icon，无论怎样，都影响Notifications area显示的图标
                //.setLargeIcon(BitmapFactory.decodeResource(MyApplication.getContext().getResources(),R.drawable.ic_cloud_circle_white_48dp))//largeicon，
                .setVibrate(new long[]{0,300}) //设置震动，此震动数组为：long vT[]={300,100,300,100}
                .setLights(Color.GREEN,1000,1000)//设置LED灯 .setLights(argb, onMs, offMs)
                .build();
        manager.notify(0,notification);
    }
}
