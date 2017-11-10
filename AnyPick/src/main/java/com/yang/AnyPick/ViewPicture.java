package com.yang.AnyPick;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yang.AnyPick.R;
import com.yang.AnyPick.download.DownloadService;
import com.github.chrisbanes.photoview.PhotoView;

public class ViewPicture extends AppCompatActivity implements View.OnClickListener {

    //沉浸式
    static View systemBar;
    //广播接收器
    private Receiver receiver;

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder=(DownloadService.DownloadBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private String url="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_picture);

        Button startDownload = (Button) findViewById(R.id.start_download);
        Button pauseDownload = (Button) findViewById(R.id.pause_download);
        Button cancelDownload = (Button) findViewById(R.id.cancel_download);
        startDownload.setOnClickListener(this);
        pauseDownload.setOnClickListener(this);
        cancelDownload.setOnClickListener(this);

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.example.yang.myapplication.CLICK_PUSH");
        receiver=new Receiver();
        registerReceiver(receiver,intentFilter);

        //沉浸式
        systemBar=getWindow().getDecorView();
        systemBar.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//显示导航栏
                        //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN//显示状态栏
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//不显示导航栏
                        | View.SYSTEM_UI_FLAG_FULLSCREEN//不显示状态栏
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//STICKY沉浸模式

        FloatingActionButton fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击事件
                downloadBinder.startDownload(url);
            }
        });

        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
        }

        Intent intent2=getIntent();
        url=intent2.getExtras().getString("url");
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);

        Glide
                .with(this)
                .load(url)
                .into(photoView);
    }
    @Override
    public void onClick(View view){
        if (downloadBinder==null){
            return;
        }
        switch (view.getId()) {
            case R.id.start_download:
                downloadBinder.startDownload(url);
                break;
            case R.id.pause_download:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel_download:
                downloadBinder.cancelDownload();
                break;
            default:
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
    }
    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.example.yang.myapplication.CLICK_PUSH")){
                finish();
            }
        }
    }

}
