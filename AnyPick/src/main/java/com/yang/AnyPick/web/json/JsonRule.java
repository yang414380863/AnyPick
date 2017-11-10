package com.yang.AnyPick.web.json;

/**
 * Created by JesusY on 2017/6/4.
 */

public class JsonRule {

    private String jsonPath;//JSON路径
    private String headString;//前向拼接字符串
    private String tailString;//后向拼接字符串

    private String regex; //正则表达式
    private String replace;//替换式

    //最正常的情况，只需要JSON路径
    public JsonRule (String jsonPath){
        this.jsonPath = jsonPath;
    }

    public JsonRule (String jsonPath,int flag,String regex,String replace){
        this.jsonPath = jsonPath;
        this.regex=regex;
        this.replace=replace;
    }

    //路径+前拼接字符串
    public JsonRule (String jsonPath, String headString){
        this.jsonPath = jsonPath;
        this.headString = headString;
    }

    //路径+前后拼接字符串
    public JsonRule (String jsonPath, String headString, String tailString){
        this.jsonPath = jsonPath;
        this.headString = headString;
        this.tailString = tailString;
    }



    public String getJsonPath(){
        return jsonPath;
    }

    public void setJsonPath(String jsonPath){
        this.jsonPath = jsonPath;
    }

    public String getHeadString() {
        if (headString == null)
            return "";
        else
            return headString;
    }

    public void setHeadString(String headString) {
        this.headString = headString;
    }

    public String getTailString() {
        if (tailString == null)
            return "";
        else
            return tailString;
    }

    public void setTailString(String tailString) {
        this.tailString = tailString;
    }
}
