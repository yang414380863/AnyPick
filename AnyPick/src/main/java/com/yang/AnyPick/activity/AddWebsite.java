package com.yang.AnyPick.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.web.JsonUtils;
import com.yang.AnyPick.web.Website;
import com.yang.AnyPick.web.html.ItemRule;
import com.yang.AnyPick.web.html.Rule;
import com.yang.AnyPick.web.json.JsonRule;

import java.util.Arrays;


public class AddWebsite extends AppCompatActivity {

    Website websiteNew;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_website);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar3);
        toolbar.setTitle("Add Website");
        setSupportActionBar(toolbar);

        final LinearLayout categoryLayout=(LinearLayout)findViewById(R.id.category_parent);
        final LinearLayout itemRuleLayout=(LinearLayout)findViewById(R.id.item_rule_parent);
        final LayoutInflater inflater = LayoutInflater.from(AddWebsite.this);

        final EditText websiteIndex=(EditText)findViewById(R.id.website_index);
        final EditText websiteName=(EditText)findViewById(R.id.website_name);
        final EditText itemSelector=(EditText)findViewById(R.id.item_selector);
        final EditText detailSelector=(EditText)findViewById(R.id.detail_selector);

        final String[] itemName=new String[]{"Link","Thumbnail","Title","Img","Article","NextPage","NextPageDetail","Category"};
        for (int i=0;i<itemName.length;i++){
            inflater.inflate(R.layout.add_item_rule,itemRuleLayout);
            TextView textView=(TextView)itemRuleLayout.getChildAt(i).findViewById(R.id.item_name);
            textView.setText(itemName[i]+":");
        }

        Button addCategory=(Button)findViewById(R.id.add_category);
        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflater.inflate(R.layout.add_category,categoryLayout);
            }
        });


        FloatingActionButton fab=(FloatingActionButton)findViewById(R.id.finish_add_website);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //category
                String[] category= new String[categoryLayout.getChildCount()*2];
                for (int i=0;i<categoryLayout.getChildCount();i++){
                    EditText index=(EditText)categoryLayout.getChildAt(i).findViewById(R.id.category_index);
                    EditText name=(EditText)categoryLayout.getChildAt(i).findViewById(R.id.category_name);
                    if (index.getText().equals("")||name.getText().equals("")){
                    }else{
                        category[2*i]=index.getText().toString();
                        category[2*i+1]=name.getText().toString();
                    }
                }
                LogUtil.d("category: "+Arrays.toString(category));
                String[][] ss=new String[itemRuleLayout.getChildCount()][8];
                //itemRule
                for (int i=0;i<itemRuleLayout.getChildCount();i++){
                    String selector=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.selector));
                    String method=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.method));
                    String attr=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.attr));
                    String regex=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.regex));
                    String replace=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.replace));
                    String jsonPath=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.jsonPath));
                    String headString=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.headString));
                    String tailString=getT((EditText)itemRuleLayout.getChildAt(i).findViewById(R.id.tailString));
                    String[] s=new String[]{selector,method,attr,regex,replace,jsonPath,headString,tailString};
                    for (int j=0;j<s.length;j++){
                        ss[i][j]=s[j];
                    }
                }

                int indexJson;
                int nextJson;
                CheckBox checkBoxIndex=(CheckBox)findViewById(R.id.index_json);
                CheckBox checkBoxNext=(CheckBox)findViewById(R.id.next_json);
                if (checkBoxIndex.isChecked()){
                    indexJson=1;
                }else {
                    indexJson=0;
                }
                if (checkBoxNext.isChecked()){
                    nextJson=1;
                }else {
                    nextJson=0;
                }
                ItemRule ruleNew=new ItemRule();
                websiteNew=new Website(getT(websiteName),getT(websiteIndex),ruleNew,indexJson,nextJson);
                websiteNew.setItemSelector(getT(itemSelector));
                websiteNew.setDetailItemSelector(getT(detailSelector));
                for (int i=0;i<itemName.length;i++){
                    Rule rule= stringToRule(itemName[i]);
                    if (!rule.getSelector().equals(" ")){
                        rule.setSelector(ss[i][0]);
                        rule.setMethod(ss[i][1]);
                        rule.setAttribute(ss[i][2]);
                        rule.setRegex(ss[i][3]);
                        rule.setReplace(ss[i][4]);
                    }

                    JsonRule jsonRule=stringToJsonRule(itemName[i]);
                    if (!jsonRule.getJsonPath().equals(" ")){
                        jsonRule.setJsonPath(ss[i][5]);
                        jsonRule.setHeadString(ss[i][6]);
                        jsonRule.setTailString(ss[i][7]);
                    }
                }

                String[] websitesStringNew=new String[ListActivity.websitesString.length+1];
                for (int i = 0; i< ListActivity.websitesString.length; i++){
                    websitesStringNew[i]= ListActivity.websitesString[i];
                }
                websitesStringNew[ListActivity.websitesString.length]=websiteNew.getWebSiteName();
                //websitesStringNew个数=旧的+1->替换websitesString->存到"websitesString"
                pref= PreferenceManager.getDefaultSharedPreferences(AddWebsite.this);
                editor=pref.edit();
                String s=websitesStringNew[0];
                for (int i=1;i<websitesStringNew.length;i++){
                    s=s+","+websitesStringNew[i];
                }
                editor.putString("websitesString",s);
                editor.putString(websiteNew.getWebSiteName(),JsonUtils.ObjectToJson(websiteNew));
                editor.apply();
                for (int i=0;i<websitesStringNew.length;i++){
                    LogUtil.d(pref.getString(websitesStringNew[i],""));
                }


                String[] websitesStringNew2=pref.getString("websitesString","").split(",");
                Website[] websitesNew2=new Website[websitesStringNew2.length];
                for (int i=0;i<websitesStringNew2.length;i++){
                    String websiteInJson=pref.getString(websitesStringNew2[i],"");
                    websitesNew2[i]=JsonUtils.JsonToObject(websiteInJson);
                }
                ListActivity.websites=websitesNew2;
                ListActivity.websitesString=websitesStringNew2;
                for (int i=0;i<websitesStringNew2.length;i++){
                    LogUtil.d(pref.getString(websitesStringNew2[i],""));
                }
                Intent intent=new Intent();
                setResult(1,intent);
                finish();
            }
        });
    }

    String getT(EditText editText){
        return editText.getText().toString();
    }

    Rule stringToRule(String ruleString){
        Rule rule;
        switch (ruleString){
            case "Link":{
                rule=websiteNew.getItemRule().getLinkRule();
                break;
            }
            case "Title":{
                rule=websiteNew.getItemRule().getTitleRule();
                break;
            }
            case "Thumbnail":{
                rule=websiteNew.getItemRule().getThumbnailRule();
                break;
            }
            case "Img":{
                rule=websiteNew.getItemRule().getImgRule();
                break;
            }
            case "Article":{
                rule=websiteNew.getItemRule().getArticleRule();
                break;
            }
            case "NextPage":{
                rule=websiteNew.getNextPageRule();
                break;
            }
            case "NextPageDetail":{
                rule=websiteNew.getNextPageDetailRule();
                break;
            }
            case "Category":{
                rule=websiteNew.getCategoryRule();
                break;
            }
            default:
                rule=new Rule(" ");
                break;
        }
        return rule;
    }
    JsonRule stringToJsonRule(String ruleString){
        JsonRule rule;
        switch (ruleString){
            case "Link":{
                rule=websiteNew.getItemRule().getJsonLinkRule();
                break;
            }
            case "Title":{
                rule=websiteNew.getItemRule().getJsonTitleRule();
                break;
            }
            case "Thumbnail":{
                rule=websiteNew.getItemRule().getJsonThumbnailRule();
                break;
            }
            case "NextPage":{
                rule=websiteNew.getItemRule().getJsonNextPageRule();
                break;
            }
            default:
                rule=new JsonRule(" ");
                break;
        }
        return rule;
    }

    //ToolBar
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar3,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_json:{
                Intent intent=new Intent(AddWebsite.this,AddWebsiteWithJson.class);
                startActivityForResult(intent,1);
                break;
            }
            default:break;
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==1&&requestCode==1){
            LogUtil.d("get result from AddWebsiteWithJson");
            Intent intent=new Intent();
            setResult(1,intent);
            finish();
        }
    }
}
