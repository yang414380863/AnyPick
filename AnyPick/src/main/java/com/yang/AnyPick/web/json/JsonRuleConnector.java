package com.yang.AnyPick.web.json;

import com.yang.AnyPick.web.Browser;
import com.yang.AnyPick.web.Website;
import com.jayway.jsonpath.JsonPath;

import java.util.List;


/**
 * Created by angio on 2017/6/6.
 */

public class JsonRuleConnector {
    static Website websiteNow= Browser.getInstance().getWebsiteNow();
    //private static String jsonData;
    public static List<Object> getCompleteLinks(String jsonData){
        List<Object> links = JsonPath.read(jsonData, websiteNow.getItemRule().getJsonLinkRule().getJsonPath());
        utiltoStringList(links);
        for (int i=0;i<links.size();i++){
            links.set(i,websiteNow.getItemRule().getJsonLinkRule().getHeadString() + links.get(i) + websiteNow.getItemRule().getJsonLinkRule().getTailString());
        }
        return links;
    }

    public static List<Object> getCompleteThumbnails(String jsonData){
        List<Object> thumbnails = JsonPath.read(jsonData, websiteNow.getItemRule().getJsonThumbnailRule().getJsonPath());
        utiltoStringList(thumbnails);
        for (int i=0;i<thumbnails.size();i++){
            thumbnails.set(i,websiteNow.getItemRule().getJsonThumbnailRule().getHeadString() + thumbnails.get(i) + websiteNow.getItemRule().getJsonThumbnailRule().getTailString());
        }
        return thumbnails;
    }

    public static List<Object> getCompleteTitles(String jsonData){
        List<Object> titles = JsonPath.read(jsonData, websiteNow.getItemRule().getJsonTitleRule().getJsonPath());
        utiltoStringList(titles);
        for (int i=0;i<titles.size();i++){
            titles.set(i,websiteNow.getItemRule().getJsonTitleRule().getHeadString() + titles.get(i) + websiteNow.getItemRule().getJsonTitleRule().getTailString());
        }
        return titles;
    }

    public static String getCompleteNextPage(String jsonData){
        String nextPage = JsonPath.read(jsonData, websiteNow.getItemRule().getJsonNextPageRule().getJsonPath()).toString();
        nextPage = websiteNow.getItemRule().getJsonNextPageRule().getHeadString() + nextPage + websiteNow.getItemRule().getJsonNextPageRule().getTailString();

        return nextPage;
    }


    private static void utiltoStringList(List<Object> lists){
        for(Object list : lists)
        {
            list.toString();
        }
    }
}
