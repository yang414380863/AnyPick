package com.yang.AnyPick.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.BaseActivity;
import com.yang.AnyPick.basic.Client;
import com.yang.AnyPick.basic.LogUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class Login extends BaseActivity {

    private EditText accountEdit;
    private EditText passwordEdit;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;
    private String username;
    private String password;
    private boolean isRemember;
    private boolean hasLogin;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        init();
        loginInit();
        UIInit();
    }

    //初始化数据
    private void init(){
        accountEdit=(EditText)findViewById(R.id.username);
        passwordEdit=(EditText)findViewById(R.id.password);
        rememberPass=(CheckBox)findViewById(R.id.remember_pass);
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        username=pref.getString("username","");
        password=pref.getString("password","");
        isRemember=pref.getBoolean("rememberPassword",false);

    }

    private void loginInit(){
        intent=new Intent(Login.this,ListActivity.class);
        if (isRemember){
            //自动填写帐号密码
            accountEdit.setText(username);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }
        hasLogin=pref.getBoolean("hasLogin",false);
        if (hasLogin){//已登录 直接自动登录
            login();
        }
    }

    private void UIInit(){
        ImageView loginBackground=(ImageView)findViewById(R.id.login_background);
        RequestOptions options = new RequestOptions().centerCrop();
        Glide
                .with(this)
                .load(R.drawable.login)
                .apply(options)
                .into(loginBackground);
        Button login=(Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                login();
            }
        });
        Button visitor=(Button)findViewById(R.id.visitor);
        visitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });
    }

    //登录
    private void login(){
        final String username=accountEdit.getText().toString();
        final String password=passwordEdit.getText().toString();
        if (username.equals("")||password.equals("")){
            Toast.makeText(Login.this,"username/password is empty",Toast.LENGTH_SHORT).show();
            return;
        }

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                new Client().sendForResult(emitter,"login "+username+" "+password);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtil.d("onNextGet: " + s);
                        loginRes(s);
                    }
                });
    }
    //登录结果处理
    private void loginRes(String s){
        //如果帐号密码都对,就成功跳转.否则尝试注册新用户
        String[] ss=s.split(" ");
        String state=ss[0];
        String mark="";
        if (ss.length==2){
            mark=ss[1];
        }
        String username=accountEdit.getText().toString();
        String password=passwordEdit.getText().toString();
        if (state.equals("failed")){
            //登录失败,尝试注册新账号
            register();
        }else if (state.equals("success")){
            //帐号密码正确
            editor=pref.edit();
            if (rememberPass.isChecked()){
                //保存帐号密码
                editor.putBoolean("rememberPassword",true);
                editor.putString("username",username);
                editor.putString("password",password);
            }
            editor.putString("mark",mark);//mark内部不能用空格分隔 用;
            editor.putBoolean("hasLogin",true);
            editor.apply();
            Toast.makeText(Login.this,"login success",Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
    }
    //注册
    private void register(){
        final String username=accountEdit.getText().toString();
        final String password=passwordEdit.getText().toString();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                new Client().sendForResult(emitter,"register "+username+" "+password);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtil.d("onNextGet: " + s);
                        registerRes(s);
                    }
                });
    }
    //注册结果处理
    private void registerRes(String s){
        String username=accountEdit.getText().toString();
        String password=passwordEdit.getText().toString();
        String res=s.split(" ")[0];
        if (res.equals("existed")){
            Toast.makeText(Login.this,"password is incorrect",Toast.LENGTH_SHORT).show();
        }else if (res.equals("success")){
            // 注册成功
            editor=pref.edit();
            if (rememberPass.isChecked()){
                //保存帐号密码
                editor.putBoolean("rememberPassword",true);
                editor.putString("username",username);
                editor.putString("password",password);
            }
            editor.putBoolean("hasLogin",true);
            editor.apply();
            Toast.makeText(Login.this,"register success",Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
    }
}