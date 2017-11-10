package com.yang.AnyPick.web;

import com.yang.AnyPick.web.html.ItemRule;
import com.yang.AnyPick.web.html.Rule;

/**
 * Created by YanGGGGG on 2017/4/6.
 * 新建一个爬虫即新建一个Website对象
 */

public class Website {

    private String webSiteName;//网站名
    private String indexUrl;//网站首页
    private String nextPageUrl;//List页面下一页
    private String nextPageDetailUrl;//Detail页面下一页
    private Rule nextPageRule;//List页面下一页Rule
    private Rule nextPageDetailRule;//Detail页面下一页Rule
    private Rule categoryRule;
    private String[] category;//分类
    private int JsonIndex=0;//第一页是否JSON格式
    private int JsonNext=0;//下一页是否JSON格式

    private ItemRule itemRule;//对一个item内部的属性的选择器
    private String itemSelector;//筛选出所有item的选择器
    private String detailItemSelector;


    public Website(String webSiteName,String indexUrl,ItemRule itemRule){
        this.webSiteName=webSiteName;
        this.indexUrl=indexUrl;
        this.itemRule = itemRule;
        detailItemSelector="*";
        nextPageRule=new Rule("");
        nextPageDetailRule=new Rule("");
        categoryRule=new Rule("");
    }

    public Website(String webSiteName,String indexUrl,ItemRule itemRule,int JsonIndex,int JsonNext){
        this(webSiteName,indexUrl,itemRule);
        this.JsonIndex=JsonIndex;
        this.JsonNext=JsonNext;
    }

    public boolean isJsonIndex(){
        if (this.JsonIndex==1){
            return true;
        }else {
            return false;
        }
    }

    public boolean isJsonNext(){
        if (this.JsonNext==1){
            return true;
        }else {
            return false;
        }
    }

    public String getDetailItemSelector() {
        return detailItemSelector;
    }
    public void setDetailItemSelector(String detailItemSelector) {
        this.detailItemSelector = detailItemSelector;
    }
    public String getItemSelector() {
        return itemSelector;
    }
    public void setItemSelector(String itemSelector) {
        this.itemSelector = itemSelector;
    }
    public ItemRule getItemRule() {
        return itemRule;
    }
    public void setItemRule(ItemRule itemRule) {
        this.itemRule = itemRule;
    }
    public String getWebSiteName() {
        return webSiteName;
    }
    public void setWebSiteName(String webSiteName) {
        this.webSiteName = webSiteName;
    }
    public String getIndexUrl() {
        return indexUrl;
    }
    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }
    public String[] getCategory() {
        return category;
    }
    public void setCategory(String[] category) {
        this.category = category;
    }
    public String getNextPageUrl() {
        return nextPageUrl;
    }
    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }
    public String getNextPageDetailUrl() {
        return nextPageDetailUrl;
    }
    public void setNextPageDetailUrl(String nextPageDetailUrl) {
        this.nextPageDetailUrl = nextPageDetailUrl;
    }
    public Rule getNextPageRule() {
        return nextPageRule;
    }
    public void setNextPageRule(Rule nextPageRule) {
        this.nextPageRule = nextPageRule;
    }
    public Rule getNextPageDetailRule() {
        return nextPageDetailRule;
    }
    public void setNextPageDetailRule(Rule nextPageDetailRule) {
        this.nextPageDetailRule = nextPageDetailRule;
    }

    public Rule getCategoryRule() {
        return categoryRule;
    }

    public void setCategoryRule(Rule categoryRule) {
        this.categoryRule = categoryRule;
    }
}
