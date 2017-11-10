package com.yang.AnyPick.web;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.yang.AnyPick.basic.*;
import com.yang.AnyPick.web.html.*;
import com.yang.AnyPick.web.json.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
 * Created by YanGGGGG on 2017/9/26.
 * 后台实现访问网页->爬取内容->保存本地的工作
 */
//单例模式
public class Browser {

    private ArrayList<WebItem> webContentList;
    private Website websiteNow;
    private WebItem webItemNow;
    private int sizeThisPage;
    private String nextPageUrl;
    private int pageNow =1;
    private Date latestUpdate;
    private String categoryNow;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private volatile static Browser instance;


    private Observable<ArrayList<WebItem>> listObservable;
    private ObservableEmitter<ArrayList<WebItem>> listEmitter;
    public void setListEmitter(ObservableEmitter<ArrayList<WebItem>> listEmitter) {
        this.listEmitter = listEmitter;
    }

    private Observable<WebItem> detailObservable;
    private ObservableEmitter<WebItem> detailEmitter;
    public void setDetailEmitter(ObservableEmitter<WebItem> detailEmitter) {
        this.detailEmitter = detailEmitter;
    }

    //将默认的构造函数私有化，防止其他类手动new
    private Browser(){
        webContentList =new ArrayList<>();
    }

    public static Browser getInstance(){
        if(instance==null){
            synchronized (Browser.class){
                if(instance==null)
                    instance=new Browser();
            }
        }
        return instance;
    }

    public Website getWebsiteNow() {
        return websiteNow;
    }

    public int getSizeThisPage() {
        return sizeThisPage;
    }

    void setNewObservableList(){
        listObservable =Observable.create(new ObservableOnSubscribe<ArrayList<WebItem>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<WebItem>> emitter) throws Exception {
                Browser.getInstance().setListEmitter(emitter);
            }
        });
    }

    void setNewObservableDetail(){
        detailObservable =Observable.create(new ObservableOnSubscribe<WebItem>() {
            @Override
            public void subscribe(ObservableEmitter<WebItem> emitter) throws Exception {
                Browser.getInstance().setDetailEmitter(emitter);
            }
        });
    }

    public void sendRequest(final Website website,Observer<ArrayList<WebItem>> observer){
        if (listEmitter !=null&&!listEmitter.isDisposed()){//如果发起新Request时,旧的Request还未完成,则终止旧的Request
            listEmitter.onError(new Exception());
        }
        recordLastRequest(website);
        setNewObservableList();
        webContentList.clear();
        listObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String url=websiteNow.getIndexUrl();
                    OkHttpClient client = new OkHttpClient();
                    if (pageNow>1){
                        url=websiteNow.getNextPageUrl();
                    }
                    LogUtil.d("Request url "+url);
                    final Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            LogUtil.d("onFailure");
                            Toast.makeText(MyApplication.getContext(),"Network connection failure",Toast.LENGTH_SHORT).show();
                            listEmitter.onError(e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (pageNow >1){
                                if (!website.isJsonNext()){
                                    //解析HTML
                                    Document doc= Jsoup.parse(response.body().string());
                                    analysis(doc);
                                }else {
                                    //解析JSON
                                    String s=response.body().string();
                                    //JSONObject jsonObject=JSON.parseObject(response.body().string());
                                    analysisJSON(s);
                                }
                            }else {
                                if (!website.isJsonIndex()){
                                    //解析HTML
                                    Document doc=Jsoup.parse(response.body().string());
                                    analysis(doc);
                                }else {
                                    //解析JSON
                                    String s=response.body().string();
                                    //JSONObject jsonObject=JSON.parseObject(response.body().string());
                                    analysisJSON(s);
                                }
                            }
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void recordLastRequest(Website website){
        pref= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        editor=pref.edit();
        if (website==null){
            return;
        }
        if (website!=websiteNow){
            pageNow =1;
        }
        websiteNow =website;
        editor.putString("lastIndex",websiteNow.getIndexUrl());
        editor.apply();//保存最后一次打开的网页URL
        Date date = new Date(System.currentTimeMillis());
        latestUpdate=date;
        SharedPreferences pref;
        pref= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        SharedPreferences.Editor editor;
        editor=pref.edit();
        editor.putString("latestUpdate",date.toString());
        editor.apply();
    }

    private void analysis(Document doc){

        Elements list = doc.select(websiteNow.getItemSelector());
        sizeThisPage=list.size();

        //解析主要信息
        for (int i=0;i<sizeThisPage;i++){
            if (SelectorAndRegex.getItemcount(doc,websiteNow)==0){
                listEmitter.onComplete();
            }
            webContentList.add(new WebItem());
            webContentList.get(i).setLink(SelectorAndRegex.getItemData(doc,websiteNow,"Link",i));
            webContentList.get(i).setTitle(SelectorAndRegex.getItemData(doc,websiteNow,"Title",i));
            webContentList.get(i).setThumbnail(SelectorAndRegex.getItemData(doc,websiteNow,"Thumbnail",i));

            //Logger.d(webContentList.get(i).getLink()+"\n"+webContentList.get(i).getTitle()+"\n"+webContentList.get(i).getThumbnail());
        }
        LogUtil.d("Finish load "+webContentList.size()+" item");

        if (!websiteNow.getCategoryRule().getSelector().equals("")){
            categoryNow=SelectorAndRegex.getOtherData(doc,websiteNow,"Category");
        }
        //解析列表的下一页
        if (!websiteNow.getNextPageRule().getSelector().equals("")){
            nextPageUrl=SelectorAndRegex.getOtherData(doc,websiteNow,"NextPage",webContentList.size()+1, pageNow);
            nextPageUrl=nextPageUrl.replaceAll("categorys","categories");
        }
        LogUtil.d("nextPageUrl "+nextPageUrl);
        listEmitter.onNext(webContentList);
        listEmitter.onComplete();
    }

    private void analysisJSON(String jsonData){

        List<Object> links = JsonRuleConnector.getCompleteLinks(jsonData);
        List<Object> thumbnails = JsonRuleConnector.getCompleteThumbnails(jsonData);
        List<Object> titles = JsonRuleConnector.getCompleteTitles(jsonData);
        String nextPage = JsonRuleConnector.getCompleteNextPage(jsonData);
        categoryNow=categoryNow.replaceAll("categorys","categories");
        nextPageUrl=nextPage.replaceAll("category",categoryNow);
        sizeThisPage=links.size();


        //解析主要信息
        for (int i=0;i<sizeThisPage;i++){
            if (links.size()==0){
                return;
            }
            webContentList.add(new WebItem());
            webContentList.get(i).setLink(links.get(i).toString());
            webContentList.get(i).setTitle(titles.get(i).toString());
            webContentList.get(i).setThumbnail(thumbnails.get(i).toString());
        }
        LogUtil.d("Finish load "+webContentList.size()+" item");
        //解析列表的下一页
        LogUtil.d("nextPageUrl "+nextPageUrl);
        listEmitter.onComplete();
    }

    public void sendRequestDetail(final Observer<WebItem> observer,final WebItem webItem){
        if (detailEmitter !=null&&!detailEmitter.isDisposed()){//如果发起新Request时,旧的Request还未完成,则终止旧的Request
            detailEmitter.onError(new Exception());
        }
        setNewObservableDetail();
        detailObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
        if (webItemNow!=null&&webItem==webItemNow){
            webItem.getImg().clear();
        }
        webItemNow=webItem;
        webItem.setImg(new ArrayList<String>());
        webItem.setArticle(new ArrayList<String>());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url(websiteNow.getNextPageDetailUrl())
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            LogUtil.d("onFailure");
                            Toast.makeText(MyApplication.getContext(),"Network connection failure",Toast.LENGTH_SHORT).show();
                            listEmitter.onError(e);
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try{
                                Document doc=Jsoup.parse(response.body().string());
                                analysisDetail(doc,webItem,observer);
                            }catch (Exception e){
                                //发送一个加载出错的广播
                                e.printStackTrace();
                            }
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void analysisDetail(Document doc,WebItem webItem,Observer<WebItem> observer) {
        String nextPageDetail;
        Elements list = doc.select(websiteNow.getDetailItemSelector());
        for (int i = 0; i < list.size(); i++) {
            webItem.getImg().add("");
            webItem.getArticle().add("");
            webItem.getImg().set(webItem.getImg().size()-1,SelectorAndRegex.getDetailData(doc,websiteNow,"Img",i));
            webItem.getArticle().set(webItem.getArticle().size()-1,SelectorAndRegex.getDetailData(doc,websiteNow,"Article",i));
        }
        if (!websiteNow.getNextPageDetailRule().getSelector().equals("")) {
            nextPageDetail = SelectorAndRegex.getOtherData(doc,websiteNow,"NextPageDetail");
            //LogUtil.d("nextPageDetail "+nextPageDetail);
            if (nextPageDetail.equals("")) { //没有下一页
//LogUtil.d("detail "+webContentList.get(id).getImg());
                detailEmitter.onNext(webItem);
                detailEmitter.onComplete();
            } else {//继续下一页
                websiteNow.setNextPageDetailUrl(nextPageDetail);
                sendRequestDetail(observer,webItem);
            }
        } else {//没有下一页的Rule
//LogUtil.d("detail "+webContentList.get(id).getImg());
            detailEmitter.onNext(webItem);
            listEmitter.onComplete();
        }
    }

    public void nextPage(){
        if (nextPageUrl!=null){
            websiteNow.setNextPageUrl(nextPageUrl);
            pageNow++;
        }else {
            //LogUtil.d(no more page");
        }
    }
}
