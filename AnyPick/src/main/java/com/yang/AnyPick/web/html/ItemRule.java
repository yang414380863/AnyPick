package com.yang.AnyPick.web.html;


import com.yang.AnyPick.web.json.JsonRule;

/**
 * Created by YanGGGGG on 2017/4/6.
 */

public class ItemRule {

    private Rule thumbnailRule;
    private Rule titleRule;
    private Rule linkRule;
    private Rule imgRule;
    private Rule articleRule;


    public Rule getThumbnailRule() {
        return thumbnailRule;
    }
    public void setThumbnailRule(Rule thumbnailRule) {
        this.thumbnailRule = thumbnailRule;
    }
    public Rule getTitleRule() {
        return titleRule;
    }
    public void setTitleRule(Rule titleRule) {
        this.titleRule = titleRule;
    }
    public Rule getLinkRule() {
        return linkRule;
    }
    public void setLinkRule(Rule linkRule) {
        this.linkRule = linkRule;
    }
    public Rule getImgRule() {
        return imgRule;
    }
    public void setImgRule(Rule imgRule) {
        this.imgRule = imgRule;
    }
    public Rule getArticleRule() {
        return articleRule;
    }
    public void setArticleRule(Rule articleRule) {
        this.articleRule = articleRule;
    }




    private JsonRule JsonThumbnailRule;
    private JsonRule JsonTitleRule;
    private JsonRule JsonLinkRule;
    private JsonRule JsonNextPageRule;

    public JsonRule getJsonNextPageRule() {
        return JsonNextPageRule;
    }

    public void setJsonNextPageRule(JsonRule jsonNextPageRule) {
        JsonNextPageRule = jsonNextPageRule;
    }

    public JsonRule getJsonThumbnailRule() {
        return JsonThumbnailRule;
    }

    public void setJsonThumbnailRule(JsonRule jsonThumbnailRule) {
        JsonThumbnailRule = jsonThumbnailRule;
    }

    public JsonRule getJsonTitleRule() {
        return JsonTitleRule;
    }

    public void setJsonTitleRule(JsonRule jsonTitleRule) {
        JsonTitleRule = jsonTitleRule;
    }

    public JsonRule getJsonLinkRule() {
        return JsonLinkRule;
    }

    public void setJsonLinkRule(JsonRule jsonLinkRule) {
        JsonLinkRule = jsonLinkRule;
    }

    public ItemRule(){
        thumbnailRule=new Rule("");
        titleRule=new Rule("");
        linkRule=new Rule("");
        imgRule=new Rule("");
        articleRule=new Rule("");

        JsonThumbnailRule=new JsonRule("");
        JsonTitleRule=new JsonRule("");
        JsonLinkRule=new JsonRule("");
        JsonNextPageRule=new JsonRule("");
    }
}
