package com.yang.AnyPick.web;

import android.widget.Toast;

import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.basic.MyApplication;
import com.yang.AnyPick.web.html.ItemRule;
import com.yang.AnyPick.web.html.Rule;
import com.yang.AnyPick.web.html.SelectorAndRegex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by YanGGGGG on 2017/5/30.
 */

public class BrowserForToolBar {
    private ItemRule ruleBING=new ItemRule();
    private Website BING=new Website("Bing","http://cn.bing.com/",ruleBING);
    private Website websiteNow=BING;



    private volatile static BrowserForToolBar instance;

    //将默认的构造函数私有化，防止其他类手动new

    private BrowserForToolBar(){}

    public static BrowserForToolBar getInstance(){
        if(instance==null){
            synchronized (Browser.class){
                if(instance==null)
                    instance=new BrowserForToolBar();
            }
        }
        return instance;
    }
    private Observable<String> observable;
    private ObservableEmitter<String> emitter;
    void setNewObservable(){
        observable =Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                BrowserForToolBar.getInstance().setEmitter(emitter);
            }
        });
    }

    public void setEmitter(ObservableEmitter<String> emitter) {
        this.emitter = emitter;
    }

    public void sendRequest(Observer<String> observer){
        setNewObservable();
        BING.setItemSelector("*");
        ruleBING.setThumbnailRule(new Rule("*","html","(\\/az\\/hprichbg\\/rb\\/[a-z|A-Z|0-9|_|-]+\\.jpg)","http://cn.bing.com$1"));
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String url=websiteNow.getIndexUrl();
                    OkHttpClient client = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            LogUtil.d("onFailure");
                            Toast.makeText(MyApplication.getContext(),"Network connection failure",Toast.LENGTH_SHORT).show();
                            emitter.onError(e);
                            call.cancel();
                        }

                        @Override
                        public void onResponse(Call call, Response response){
                            try{
                                //解析HTML
                                Document doc= Jsoup.parse(response.body().string());
                                String imgSrc= SelectorAndRegex.getItemData(doc,websiteNow,"Thumbnail",1);
                                LogUtil.d("Bing Today Img:"+imgSrc);
                                emitter.onNext(imgSrc);
                                emitter.onComplete();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            call.cancel();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
