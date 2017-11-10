package com.yang.AnyPick.web;

import com.yang.AnyPick.web.html.ItemRule;
import com.yang.AnyPick.web.html.Rule;
import com.yang.AnyPick.web.json.JsonRule;


/**
 * Created by YanGGGGG on 2017/6/6.
 */

public class WebsiteInit {
    static ItemRule rulePOOCG=new ItemRule();
    public final static Website POOCG=new Website("Poocg","https://www.poocg.com/works/index/type/new",rulePOOCG);
    static ItemRule ruleDEVIANTART=new ItemRule();
    public final static Website DEVIANTART=new Website("Deviantart","http://www.deviantart.com/whats-hot/",ruleDEVIANTART);
    static ItemRule ruleLEIFENG=new ItemRule();
    public final static Website LEIFENG=new Website("雷锋网","https://www.leiphone.com/category/sponsor",ruleLEIFENG);
    static ItemRule ruleQdaily=new ItemRule();
    public final static Website Qdaily =new Website("好奇心日报","http://www.qdaily.com/tags/1068.html",ruleQdaily,0,1);

    public static Website[] websitesInit=new Website[]{LEIFENG, Qdaily,POOCG,DEVIANTART};
    public static void init(){

        POOCG.setItemSelector("li:has(div.imgbox)");
        rulePOOCG.setLinkRule(new Rule("div.imgbox > a[href]","attr","href"));
        rulePOOCG.setThumbnailRule(new Rule("div.imgbox > a > img[src]","attr","src"));
        rulePOOCG.setTitleRule(new Rule("div.infobox > p.titles","text"));
        POOCG.setDetailItemSelector("img[style*=max-width]");
        rulePOOCG.setImgRule(new Rule("*","attr","src"
                ,"(https:\\/\\/imagescdn\\.poocg\\.me\\/uploadfile\\/photo\\/[0-9]{4}\\/[0-9]{1,2}\\/[a-z|0-9]+\\.[a-z]+)","$1"));
        POOCG.setNextPageRule(new Rule("a#pagenav","attr","href"));
        POOCG.setCategory(new String[]{"最新","https://www.poocg.com/works/index/type/new","新赞","https://www.poocg.com/works/index/type/love","热门","https://www.poocg.com/works/index/type/hot"
                ,"精华","https://www.poocg.com/works/index/type/best","推荐","https://www.poocg.com/works/index/type/rem"});

        DEVIANTART.setItemSelector("span[class*=thumb]:has(img[data-sigil=torpedo-img])");
        ruleDEVIANTART.setLinkRule(new Rule("a.torpedo-thumb-link","attr","href"));
        ruleDEVIANTART.setThumbnailRule(new Rule("a.torpedo-thumb-link > img[data-sigil=torpedo-img]","attr","src"));
        ruleDEVIANTART.setTitleRule(new Rule("span.info > span.title-wrap > span.title","text"));
        DEVIANTART.setDetailItemSelector("div[class=dev-view-deviation]");
        ruleDEVIANTART.setImgRule(new Rule("img[class=dev-content-full]","attr","src"));
        DEVIANTART.setNextPageRule(new Rule("a.selected","attr","href","(http:\\/\\/www\\.deviantart\\.com\\/[a-z|-]+\\/)","$1?offset=!size"));
        DEVIANTART.setCategory(new String[]{"Newest","http://www.deviantart.com/newest/","What's Hot","http://www.deviantart.com/whats-hot/"
                ,"Undiscovered","http://www.deviantart.com/undiscovered/","Popular 24 hours","http://www.deviantart.com/popular-24-hours/","Popular All Time","http://www.deviantart.com/popular-all-time/"});


        LEIFENG.setItemSelector("li > div.box:has(div.img)");
        ruleLEIFENG.setLinkRule(new Rule("div.img > a[target]","attr","href"));
        ruleLEIFENG.setThumbnailRule(new Rule("div.img > a[target] > img.lazy","attr","data-original"));
        ruleLEIFENG.setTitleRule(new Rule("div.img > a[target] > img.lazy","attr","title"));
        LEIFENG.setDetailItemSelector("div[class=lph-article-comView] > p");
        ruleLEIFENG.setImgRule(new Rule("p img[alt]","attr","src"));
        ruleLEIFENG.setArticleRule(new Rule("p","text"));
        LEIFENG.setNextPageRule(new Rule("div.lph-page > a.next","attr","href"));
        LEIFENG.setCategory(new String[]{"业界","https://www.leiphone.com/category/sponsor","人工智能","http://www.leiphone.com/category/ai","智能驾驶","http://www.leiphone.com/category/transportation","网络安全","http://www.leiphone.com/category/letshome"
                ,"AR/VR","http://www.leiphone.com/category/arvr","机器人","http://www.leiphone.com/category/robot","Fintect","http://www.leiphone.com/category/fintech","物联网","http://www.leiphone.com/category/iot"
                ,"未来医疗","http://www.leiphone.com/category/aihealth","智能硬件","http://www.leiphone.com/category/weiwu","AI+","http://www.leiphone.com/category/aijuejinzhi"});

        Qdaily.setItemSelector("div[class*=packery-item] > a[href]");
        ruleQdaily.setLinkRule(new Rule("a[href]","attr","href","(.*)","http://www.qdaily.com$1"));
        ruleQdaily.setThumbnailRule(new Rule("div[class*=hd] > div >img","attr","data-src"));
        ruleQdaily.setTitleRule(new Rule("div[class*=hd] > div >img","attr","alt"));
        Qdaily.setDetailItemSelector("div.detail > p,div.detail > div[class*=images]");
        ruleQdaily.setImgRule(new Rule("figure > img[data-ratio]","attr","data-src"));
        ruleQdaily.setArticleRule(new Rule("p","text"));
        Qdaily.setNextPageRule(new Rule("div[class=page-content]","html","data-lastkey\\=\"([0-9]+)\" data-([a-z]+)id\\=\"([0-9]+)\"","http://www.qdaily.com/$2s/$2more/$3/$1.json"));
        Qdaily.setCategory(new String[]{"长文章","http://www.qdaily.com/tags/1068.html","10个图","http://www.qdaily.com/tags/1615.html","TOP15","http://www.qdaily.com/tags/29.html"
                ,"商业","http://www.qdaily.com/categories/18.html","智能","http://www.qdaily.com/categories/4.html","设计","http://www.qdaily.com/categories/17.html","时尚","http://www.qdaily.com/categories/19.html"
                ,"娱乐","http://www.qdaily.com/categories/3.html","城市","http://www.qdaily.com/categories/5.html","游戏","http://www.qdaily.com/categories/54.html"});
        Qdaily.setCategoryRule(new Rule("div[class=page-content]","html","data-lastkey\\=\"([0-9]+)\" data-([a-z]+)id\\=\"([0-9]+)\"","$2s/$2more/$3/"));
        ruleQdaily.setJsonThumbnailRule(new JsonRule("$.data.feeds[*].image"));
        ruleQdaily.setJsonTitleRule(new JsonRule("$.data.feeds[*].post.title"));
        ruleQdaily.setJsonLinkRule(new JsonRule("$.data.feeds[*].post.id", "http://www.qdaily.com/articles/", ".html"));
        ruleQdaily.setJsonNextPageRule(new JsonRule("$.data.last_key","http://www.qdaily.com/category/",".json"));

    }
}
