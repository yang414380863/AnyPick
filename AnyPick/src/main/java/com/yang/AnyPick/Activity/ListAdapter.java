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
import com.yang.AnyPick.web.Browser;
import com.yang.AnyPick.web.WebItem;

import java.util.ArrayList;



/**
 * Created by YanGGGGG on 2017/3/21.
 * List适配器
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>implements View.OnClickListener {

    private static ArrayList<WebItem> webContentList;
    private Context context;
    RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.white)
            .error(R.drawable.error)
            .fitCenter()
            .dontAnimate();//无载入动画
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name;

        public ViewHolder(View view){
            super(view);
            image=(ImageView)view.findViewById(R.id.image);
            name=(TextView)view.findViewById(R.id.textview);
        }
    }

    public ListAdapter(Context context){//构造方法
        this.context=context;
        webContentList =new ArrayList<>();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //生成一个webimg_item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.web_content_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        WebItem webContent= webContentList.get(position);
        holder.name.setText(webContent.getTitle());
        Glide
                .with(context)
                .load(webContent.getThumbnail())
                //.thumbnail(Glide.with(context).load(R.drawable.loading1))//好像有点问题
                .apply(options)
                //.crossFade() //设置淡入淡出效果，默认300ms，可以传参 会导致图片变形 先不用
                .into(holder.image);
        holder.itemView.setTag(position);
    }

    @Override
    public void onClick(View view){
        //点击->获取链接->显示图片/目录
        int position=(int)view.getTag();
        Browser.getInstance().getWebsiteNow().setNextPageDetailUrl(webContentList.get(position).getLink());


        Intent intent=new Intent(context,DetailActivity.class);
        intent.putExtra("WebItem",webContentList.get(position));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    public ArrayList<WebItem> getWebContents(){
        return webContentList;
    }

    @Override
    public int getItemCount(){
        return webContentList.size();
    }
}
