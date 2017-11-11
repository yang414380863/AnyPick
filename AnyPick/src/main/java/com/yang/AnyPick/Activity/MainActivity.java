package com.yang.AnyPick.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.MyApplication;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private boolean hasLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        hasLogin=pref.getBoolean("hasLogin",false);
        if (isPushService()){
            //通过点击推送启动
            //发送一个点击了推送的广播,使不需要的Activity关闭
            Intent intentBroadcast=new Intent("com.example.yang.yang.CLICK_PUSH");
            MyApplication.getContext().sendBroadcast(intentBroadcast);
            String index=getIntent().getExtras().getString("index");
            Intent intent=new Intent(MainActivity.this,ListActivity.class);
            intent.putExtra("index",index);//如果List之前未启动则通过intent获取推送index
            //todo:将index传给ListActivity,使其访问并刷新界面
            startActivity(intent);
            finish();
        }else {
            //通过点击APP启动
            startPushService();
        }
        if (hasLogin){
            //已登录 直接跳转ListActivity
            Intent intent=new Intent(MainActivity.this,ListActivity.class);
            startActivity(intent);
            finish();
        }else {
            //未登录 进入LoginActivity
            Intent intent=new Intent(MainActivity.this,Login.class);
            startActivity(intent);
            finish();
        }

    }


    //检测是否由push进入Activity todo:各Activity 也要有这个检测
    private boolean isPushService(){
        return getIntent().hasExtra("index");
    }

    //todo:启动push服务
    private void startPushService(){

    }


}
