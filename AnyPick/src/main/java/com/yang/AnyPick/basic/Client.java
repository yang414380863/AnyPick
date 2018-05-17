package com.yang.AnyPick.basic;

/**
 * Created by YanGGGGG on 2017/10/28.
 */


import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import io.reactivex.ObservableEmitter;

public class Client {
    private static final int PORT=26975;
    private static final String ServerIP="120.78.83.222";//120.78.83.222


    public void sendForResult(final ObservableEmitter<String> emitter,final String s){
        new Thread(new Runnable() {
            @Override
            public void run(){
                try (Socket socket = new Socket(ServerIP, PORT)){
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                    out.println(s);//输出到服务器
                    LogUtil.d("send: "+s);
                    String res=in.readLine();//返回服务器返回的内容
                    LogUtil.d("get: "+res);
                    if(res!=null){
                        emitter.onNext(res);
                    }else {
                        emitter.onNext("error");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    emitter.onNext("error");
                }
            }
        }).start();
    }

    public void sendForResult(final String s,final String msg){
        new Thread(new Runnable() {
            @Override
            public void run(){
                try (Socket socket = new Socket(ServerIP, PORT)){
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                    out.println(s);//输出到服务器
                    LogUtil.d("send: "+s);
                    String res=in.readLine();//返回服务器返回的内容
                    LogUtil.d("get: "+res);
                    EventBus.getDefault().post( msg+" "+res);
                }catch (IOException e){
                    e.printStackTrace();
                    EventBus.getDefault().post("error");
                }
            }
        }).start();
    }
}