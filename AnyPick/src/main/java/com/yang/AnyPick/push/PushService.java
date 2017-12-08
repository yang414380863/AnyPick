package com.yang.AnyPick.push;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.orhanobut.logger.Logger;
import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.Client;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.basic.MyApplication;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;


public class PushService extends Service {

    private static boolean isRunning;
    //唤醒间隔
    private static final int TEN_MINUTE=10*60*1000;
    private static final int ONE_MINUTE=60*1000;
    private static final int TEN_SECOND=10*1000;
    private static int intervalTime;

    SimpleDateFormat sdf;

    private String username;

    @Override
    public void onCreate() {
        super.onCreate();
        sdf= new SimpleDateFormat("yyyy//MM/dd/ HH:mm:ss");
        intervalTime=ONE_MINUTE;
        isRunning=false;
        EventBus.getDefault().register( this );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning=true;
        if (intent==null||!intent.hasExtra("username")){
            LogUtil.d("PushService Stop");
            return Service.START_NOT_STICKY;
        }
        username=intent.getExtras().getString("username");
        if (!username.equals("")){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d("check PushService");
                    new Client().sendForResult("checkPush "+username,"pushService");
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
    public void judgePush (String event){
        if (!event.split(" ")[0].equals("pushService")){
            return;
        }
        event=event.split(" ")[1];
        Logger.d("get push event: "+event);
        if (event!=null&&event!=""){
            SharedPreferences pref;
            pref= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
            String[] res=event.split(";");
            for (int i=0;i<res.length;i++){
                String[] s=res[i].split(",");
                Long getDate=Long.valueOf(s[2]);
                Long latestDate=Long.valueOf(pref.getString(s[0],"0"));
                //LogUtil.d("getDate "+sdf.format(getDate));
                //LogUtil.d("latestDate "+sdf.format(latestDate));
                //对比时间
                if (getDate>latestDate){
                    LogUtil.d("New Push");
                    showNotification(s[0],s[1],getDate);
                }
            }
        }
    }

    private void showNotification(String index,String link,Long date){
        Intent resultIntent = new Intent(MyApplication.getContext(), PushActivity.class);
        resultIntent.putExtra("index",index);
        //resultIntent.putExtra("link",link);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, FLAG_UPDATE_CURRENT);
        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("New Update From: "+index)//标题
                .setContentText("Link: "+link)//正文
                .setWhen(date)//通知发生的时间为服务器更新时间
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
