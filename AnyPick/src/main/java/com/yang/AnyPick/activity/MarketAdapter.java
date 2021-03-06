package com.yang.AnyPick.activity;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.yang.AnyPick.basic.LogUtil;

import org.greenrobot.eventbus.EventBus;

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
                .inflate(R.layout.market_item, parent, false);
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
        int position=(int)view.getTag();
        final String name=marketNameList.get(position);
        final SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(context);
        final String username=pref.getString("username","");
        final String password=pref.getString("password","");
        final String local=pref.getString("local","");

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
                        EventBus.getDefault().post("refreshMenu ");
                        if (!username.equals("")){
                            String res;
                            if (local==null||local.equals("")){
                                res=name;
                                new Client().sendForResult("updateLocal "+username+" "+password+" "+res,"updateLocal");
                            }else {
                                res=local+"!@#"+name;
                                new Client().sendForResult("updateLocal "+username+" "+password+" "+res,"updateLocal");
                            }
                            pref.edit().putString("local",res).apply();
                        }
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
