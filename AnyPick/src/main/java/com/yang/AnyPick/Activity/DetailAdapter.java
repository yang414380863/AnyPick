package com.yang.AnyPick.Activity;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yang.AnyPick.R;

import java.util.ArrayList;


/**
 * Created by YanGGGGG on 2017/3/22.
 * 显示详细情况
 */

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder>implements View.OnClickListener{

    private ArrayList<String> urls=new ArrayList<>();
    private ArrayList<String> texts=new ArrayList<>();
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView textView;

        public ViewHolder(View view){
            super(view);
            image=(ImageView)view.findViewById(R.id.image);
            textView=(TextView)view.findViewById(R.id.textview);
        }
    }

    public DetailAdapter(Context context){
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.web_content_item_detail, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setElevation(0);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        holder.image.setVisibility(View.GONE);//隐藏image
        holder.textView.setVisibility(View.GONE);//隐藏textView
        if (!urls.get(position).equals("")){
            holder.image.setVisibility(View.VISIBLE);
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.white)
                    .error(R.drawable.error)
                    .fitCenter()
                    .dontAnimate();//无载入动画;
            Glide
                    .with(context)
                    .load(urls.get(position))
                    .thumbnail(Glide.with(context).load(R.drawable.loading2))
                    .apply(options)
                    //.crossFade() //设置淡入淡出效果，默认300ms，可以传参 会导致图片变形 先不用
                    .into(holder.image);
        }
        if (!texts.get(position).equals("")){
            holder.textView.setVisibility(View.VISIBLE);
            holder.textView.setText(texts.get(position));
        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount(){
        return urls.size();
    }

    public ArrayList<String> getUrls(){
        return urls;
    }
    public ArrayList<String> getTexts(){
        return texts;
    }
    @Override
    public void onClick(View view){
        //点击->获取链接->显示图片/目录
        int position=(int)view.getTag();
        if(!urls.get(position).equals("")){
            Intent intent=new Intent(context,ViewPicture.class);
            intent.putExtra("url",urls.get(position));
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
