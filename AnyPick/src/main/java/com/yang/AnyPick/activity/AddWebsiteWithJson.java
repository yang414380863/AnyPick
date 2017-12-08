package com.yang.AnyPick.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.BaseActivity;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.basic.JsonUtils;
import com.yang.AnyPick.web.Website;

public class AddWebsiteWithJson extends BaseActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_website_with_json);

        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar4);
        toolbar.setTitle("Add Website With Json");
        setSupportActionBar(toolbar);

        final EditText editText=(EditText)findViewById(R.id.json_string);
        FloatingActionButton fab=(FloatingActionButton)findViewById(R.id.finish_add_website);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String JsonString=editText.getText().toString();
                Website websiteNew=JsonUtils.JsonToWebsite(JsonString);
                if (websiteNew==null){
                    Intent intent=new Intent();
                    setResult(1,intent);
                    finish();
                    return;
                }
                String[] websitesStringNew=new String[ListActivity.websiteNameList.length+1];
                for (int i = 0; i< ListActivity.websiteNameList.length; i++){
                    websitesStringNew[i]= ListActivity.websiteNameList[i];
                }
                websitesStringNew[ListActivity.websiteNameList.length]=websiteNew.getWebSiteName();
                //websitesStringNew个数=旧的+1->替换websitesString->存到"websitesString"
                pref= PreferenceManager.getDefaultSharedPreferences(AddWebsiteWithJson.this);
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
                    websitesNew2[i]=JsonUtils.JsonToWebsite(websiteInJson);
                }
                ListActivity.websites=websitesNew2;
                ListActivity.websiteNameList=websitesStringNew2;
                for (int i=0;i<websitesStringNew2.length;i++){
                    LogUtil.d(pref.getString(websitesStringNew2[i],""));
                }
                Intent intent=new Intent();
                setResult(1,intent);
                finish();
            }
        });
    }
    //ToolBar
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar4,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add:{
                finish();
                break;
            }
            default:break;
        }
        return true;
    }
}
