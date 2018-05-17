package com.yang.AnyPick.web;

import android.preference.PreferenceManager;

import com.yang.AnyPick.basic.FileUtil;
import com.yang.AnyPick.basic.JsonUtils;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.basic.MyApplication;
import com.yang.AnyPick.web.html.ItemRule;
import com.yang.AnyPick.web.html.Rule;

import java.util.ArrayList;


/**
 * Created by YanGGGGG on 2017/6/6.
 */

public class WebsiteInit {
    private static Website[] websiteList;
    private static String[] websiteNameList;
    private static ArrayList<Website> websiteArrayList;
    private static int length;

    public static String[] getWebsiteNameList(){//没有Download Website时从Assets读取
        if (true){
            //createWebsiteJson();
        }
        String username= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("username","");
        String[] downloadWebsites=FileUtil.showFileFromData(username);
        if (downloadWebsites==null||downloadWebsites.length==0){
            websiteNameList =FileUtil.getListFromAssets("website");
            LogUtil.d("Find "+ length+ "websites in Assets");
        }else {
            websiteNameList=downloadWebsites;
            LogUtil.d("Find "+ length+ "websites in Data");
        }
        length=websiteNameList.length;
        return websiteNameList;
    }

    public static Website[] getWebsiteList(){//没有Download Website时从Assets读取
        getWebsiteNameList();
        String username= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("username","");
        String[] downloadWebsites=FileUtil.showFileFromData(username);
        websiteList =new Website[length];
        if (downloadWebsites==null||downloadWebsites.length==0){
            for (int i = 0; i< length; i++){
                websiteList[i]= JsonUtils.JsonToWebsite(FileUtil.readFileFromAssets("website/"+ websiteNameList[i]));
                LogUtil.d("Find "+ websiteNameList[i]+" in Assets");
            }
        }else {
            for (int i = 0; i< length; i++){
                websiteList[i]= JsonUtils.JsonToWebsite(FileUtil.readFileFromData(username,websiteNameList[i]));
                LogUtil.d("Find "+ websiteNameList[i]+" in Data");
            }
        }
        return websiteList;
    }

    public static ArrayList<Website> getWebsiteArrayList(){
        getWebsiteNameList();
        websiteArrayList =new ArrayList<>();
        for (int i = 0; i< length; i++){
            LogUtil.d("Find "+ websiteNameList[i]);
            websiteArrayList.add(JsonUtils.JsonToWebsite(FileUtil.readFileFromAssets("website/"+ websiteNameList[i])));
        }
        return websiteArrayList;
    }

    private static void createWebsiteJson(){
        ItemRule rule =new ItemRule();
        Website website=new Website("ZAKER","http://www.myzaker.com/channel/660",rule);
        website.setItemSelector("div.section > div[class=figure flex-block]");
        rule.setLinkRule(new Rule("a[href]","attr","href","//(www.myzaker.com/article/[a-z|0-9]+/)","$1"));
        rule.setThumbnailRule(new Rule("a.img","attr","style","(zkres[0-9].myzaker" + ".com/[0-9]+/[a-z|A-Z|0-9|=|_]+.jpg)","$1"));
        rule.setTitleRule(new Rule("a[href]","attr","title"));
        website.setDetailItemSelector("div.content > p");
        rule.setArticleRule(new Rule("*","text"));
        rule.setImgRule(new Rule("img[class=opacity_0]","attr","src"));
        website.setNextPageRule(new Rule("a[class=next_page]","attr","href"));
        website.setCategory(new String[]{
                "热点",
                "http://www.myzaker.com/channel/660",
                "娱乐",
                "http://www.myzaker.com/channel/9",
                "汽车",
                "http://www.myzaker.com/channel/7",
                "体育",
                "http://www.myzaker.com/channel/8",
                "科技",
                "http://www.myzaker.com/channel/13",
                "国内",
                "http://www.myzaker.com/channel/1",
                "国际",
                "http://www.myzaker.com/channel/2",
                "军事",
                "http://www.myzaker.com/channel/3",
                "财经",
                "http://www.myzaker.com/channel/4",
                "互联网",
                "http://www.myzaker.com/channel/5",
                "教育",
                "http://www.myzaker.com/channel/11",
        });
        String s=JsonUtils.ObjectToJson(website);
        LogUtil.d(s);
    }

}
