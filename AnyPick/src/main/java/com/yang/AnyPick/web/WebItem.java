package com.yang.AnyPick.web;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by YanGGGGG on 2017/4/5.
 */

public class WebItem implements Serializable {

    private String thumbnail;//配图/缩略图
    private String title;//标题
    private String link;//点击之后要进去的网页URL
    private ArrayList<String> img;//原图链接
    private ArrayList<String> article;//详细文字内容


    public ArrayList<String> getArticle() {
        return article;
    }

    public void setArticle(ArrayList<String> article) {
        this.article = article;
    }

    public ArrayList<String> getImg() {
        return img;
    }

    public void setImg(ArrayList<String> img) {
        this.img = img;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
