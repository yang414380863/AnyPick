package com.yang.AnyPick.activity;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.Client;
import com.yang.AnyPick.basic.FileUtil;
import com.yang.AnyPick.basic.JsonUtils;
import com.yang.AnyPick.basic.LogUtil;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.ViewHolder>implements View.OnClickListener{
    private static ArrayList<String> marketNameList;
    private Context context;
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name;

        public ViewHolder(View view){
            super(view);
            image=(ImageView)view.findViewById(R.id.image);
            name=(TextView)view.findViewById(R.id.textview);
        }
    }
    public MarketAdapter(Context context){//构造方法
        this.context=context;
        marketNameList =new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //生成一个item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.web_content_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        String websiteName= marketNameList.get(position);
        holder.name.setText(websiteName);
        holder.itemView.setTag(position);
    }

    @Override
    public void onClick(View view){
        //点击->获取链接->显示图片/目录
        int position=(int)view.getTag();
        final String name=marketNameList.get(position);
        final String username=PreferenceManager.getDefaultSharedPreferences(context).getString("username","");
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                new Client().sendForResult(emitter,"marketGetDetail "+name);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtil.d("onNextGet: " + s);
                        FileUtil.writeFileToData(username,name,s);
                    }
                });

    }

    public ArrayList<String> getMarketList(){
        return marketNameList;
    }

    @Override
    public int getItemCount(){
        return marketNameList.size();
    }
}
