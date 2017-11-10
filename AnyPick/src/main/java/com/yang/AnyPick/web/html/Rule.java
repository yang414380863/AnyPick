package com.yang.AnyPick.web.html;

/**
 * Created by YanGGGGG on 2017/4/6.
 * 自定义爬虫的规则
 */

public class Rule {

    private String selector;//选择器
    private String method;
    private String attribute;//要提取的属性
    private String regex; //正则表达式
    private String replace;//替换式

    //item的Rule只做Selector
    public Rule(String selector){
        this.selector=selector;
    }
    //如method=text/html时不需要attribute
    public Rule(String selector,String method){
        this.selector=selector;
        this.method=method;
    }
    //选择器(method=text/html)+正则
    public Rule(String selector,String method,String regex,String replace){
        this(selector,method);
        this.regex=regex;
        this.replace=replace;
    }
    //默认
    public Rule(String selector,String method,String attribute){
        this.selector=selector;
        this.attribute=attribute;
        this.method=method;
    }
    //选择器+正则
    public Rule(String selector,String method,String attribute,String regex,String replace){
        this(selector,method,attribute);
        this.regex=regex;
        this.replace=replace;
    }


    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }
}
