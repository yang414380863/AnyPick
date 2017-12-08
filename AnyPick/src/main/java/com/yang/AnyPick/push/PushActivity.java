package com.yang.AnyPick.push;


import android.content.Intent;
import android.os.Bundle;

import com.yang.AnyPick.activity.ListActivity;
import com.yang.AnyPick.basic.BaseActivity;
import com.yang.AnyPick.basic.LogUtil;

import org.greenrobot.eventbus.EventBus;

public class PushActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("checkPush");
        Intent intent=getIntent();
        String index;
        if (!intent.hasExtra("index")){
            finish();
        }
        index=intent.getStringExtra("index");
        Intent intentToListActivity =new Intent(this,ListActivity.class);
        startActivity(intentToListActivity);
        EventBus.getDefault().post("getPush "+index);
        finish();
    }
}
