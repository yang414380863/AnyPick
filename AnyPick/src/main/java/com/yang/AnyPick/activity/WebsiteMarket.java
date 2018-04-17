package com.yang.AnyPick.activity;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.Client;
import com.yang.AnyPick.basic.FileUtil;
import com.yang.AnyPick.basic.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class WebsiteMarket extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MarketAdapter adapter;
    private ArrayList<String> websiteNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.website_market);

        String username= PreferenceManager.getDefaultSharedPreferences(this).getString("username","");
        FileUtil.showFileFromData(username);

        adapter=new MarketAdapter(this);
        //瀑布流
        recyclerView=(RecyclerView)findViewById(R.id.market_list);
        StaggeredGridLayoutManager layoutManager=new
                StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);//列数1
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                new Client().sendForResult(emitter,"marketGetList");
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtil.d("onNextGet: " + s);
                        getList(s);
                    }
                });

    }

    private void getList(String s){
        websiteNames = new ArrayList<>(Arrays.asList(s.split(",")));
        adapter.getMarketList().clear();//要重新指向一次才能检测到刷新
        adapter.getMarketList().addAll(websiteNames);
        adapter.notifyDataSetChanged();
    }
}
