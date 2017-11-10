package com.yang.AnyPick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.logger.Logger;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.web.*;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.yang.AnyPick.web.WebsiteInit.websitesInit;

//列表Activity
public class ListActivity extends AppCompatActivity {
    //瀑布流列表
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    //侧滑菜单
    private DrawerLayout drawerLayout;
    private NavigationView navViewRight;
    private NavigationView navViewLeft;
    //下拉刷新 监听器
    private SwipeRefreshLayout swipeRefreshLayout;
    private Snackbar snackbar;
    //标题栏
    private ImageView imageView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    //toolbar
    private Toolbar toolbar;
    //获取用户
    private SharedPreferences pref;
    //写入订阅
    private SharedPreferences.Editor editor;
    //用户信息标题栏
    private TextView usernameShow;
    private ImageView userIcon;
    //侧滑菜单
    private View navViewLeftHeader;
    private ImageView leftBackground;
    private ImageView rightBackground;
    //添加网站
    private ImageButton addWebsite;
    private TextView addWebsiteText;
    //登录
    private String mark;
    private boolean hasLogin;
    private String username;
    //初始化网站LIST
    static Website[] websites;
    static String[] websitesString;
    //LIST内容
    private ArrayList<WebItem> webContentList;
    //当前显示的网站
    private Website websiteNow;
    //当前页的item数
    private int sizeThisPage;
    //是否是访问下一页
    private static boolean isNextPage;
    //单例
    private Browser browser;
    //是否正在刷新
    private int isRefreshing=0;
    //刷新位置
    private String refreshPlace;
    //定义一个变量，来标识是否退出
    private static boolean isExit = false;

    ListAdapter adapter2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        init();
        initWebsiteList();
        loginJudge();
        UIInit();
        menuInit();
        addWebsite();
        listContentInit();
        homepageChoose();
    }

    private void init(){
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        mark=pref.getString("mark","");
        hasLogin=pref.getBoolean("hasLogin",false);
        username=pref.getString("username","");
        editor=pref.edit();
        browser=Browser.getInstance();
        webContentList=new ArrayList<>();
        adapter=new ListAdapter(this);
        // 沉浸式
        //final View systemBar = findViewById(collapsing_toolbar);
        //折叠标题栏
        imageView=(ImageView)findViewById(R.id.image_view);
        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("Loading...");
        refreshPlace="top";
        //ToolBar
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //侧滑菜单
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navViewLeft=(NavigationView)findViewById(R.id.nav_view_left);
        navViewRight=(NavigationView)findViewById(R.id.nav_view_right);
        navViewLeftHeader=navViewLeft.getHeaderView(0);
        usernameShow=(TextView)navViewLeftHeader.findViewById(R.id.username_show);
        userIcon=(ImageView)navViewLeftHeader.findViewById(R.id.user_icon);
        leftBackground=(ImageView)navViewLeftHeader.findViewById(R.id.left_background);
        rightBackground=(ImageView)navViewRight.getHeaderView(0).findViewById(R.id.right_background);
        addWebsite=(ImageButton)navViewLeftHeader.findViewById(R.id.add_website);
        addWebsiteText=(TextView)navViewLeftHeader.findViewById(R.id.add_website_text);
    }

    private void initWebsiteList(){
        String temp=pref.getString("websitesString","");
        if (temp.equals("")||!hasLogin){
            //List为空 或者游客身份
            WebsiteInit.init();
            websites=websitesInit;
            websitesString=new String[]{"雷锋网","好奇心日报","Poocg","Deviantart"};
            StringBuilder s=new StringBuilder(websitesString[0]);
            for (int i=1;i<websitesString.length;i++){
                s=s.append(",").append(websitesString[i]);
            }
            editor.putString("websitesString", s.toString());
            for (int i=0;i<websites.length;i++){
                editor.putString(websites[i].getWebSiteName(),JsonUtils.ObjectToJson(websites[i]));
                LogUtil.d(pref.getString(websites[i].getWebSiteName(),""));
            }
            editor.apply();
        }else {
            //todo:从服务器下载WebsiteList 服务器加一列WebsiteList
            //读"websitesString"个数->创建数组->String->Object
            String[] websitesStringNew=pref.getString("websitesString","").split(",");
            Website[] websitesNew=new com.yang.AnyPick.web.Website[websitesStringNew.length];
            for (int i=0;i<websitesStringNew.length;i++){
                String websiteInJson=pref.getString(websitesStringNew[i],"");
                websitesNew[i]= JsonUtils.JsonToObject(websiteInJson);
            }
            websites=websitesNew;
            websitesString=websitesStringNew;
        }
    }

    private void homepageChoose(){
        //todo 点击推送通知后 设置index为跳转的指定的页面
        //无推送时默认进入上次最后打开的页面
        //无推送且之前未使用过则打开默认主页
        if (getIntent().hasExtra("index")){
            Intent intent=getIntent();
            String index=intent.getExtras().getString("index");
            LogUtil.d("index:"+index);
            boolean isFind=false;
            for (int i=0;i<websites.length;i++){
                if (websites[i].getCategory()==null){
                    if (websites[i].getIndexUrl().equals(index)){
                        websiteNow=websites[i];
                        sendRequestForList(websiteNow);
                        break;
                    }else {
                        //无Category/Index的跳过
                        continue;
                    }
                }else {
                    for (int j=0;j<websites[i].getCategory().length;j++,j++){
                        if (websites[i].getCategory()[j+1].equals(index)){
                            websiteNow=websites[i];
                            websiteNow.setIndexUrl(websites[i].getCategory()[j+1]);
                            sendRequestForList(websiteNow);
                            isFind=true;
                            break;
                        }
                    }
                    if (isFind){
                        break;
                    }
                }
            }
        }else {
            String indexNow=pref.getString("lastIndex","");
            if (indexNow.equals("")){
                websiteNow=websites[0];
                sendRequestForList(websites[0]);//默认首页第一个
            }else {
                boolean isFind=false;
                for (int i=0;i<websites.length;i++){
                    if (isFind){
                        break;
                    }
                    if (websites[i]==null||websites[i].getIndexUrl()==null){
                        continue;
                    }
                    if (websites[i].getCategory()==null||websites[i].getCategory().length==0){
                        continue;
                    }
                    if (websites[i].getIndexUrl().equals(indexNow)){
                        websiteNow=websites[i];
                        sendRequestForList(websiteNow);
                        isFind=true;
                        break;
                    }
                    if (isFind){
                        break;
                    }
                    //检查category
                    for (int j=0;j<websites[i].getCategory().length;j++,j++){
                        if (websites[i].getCategory()[j+1].equals(indexNow)){
                            websiteNow=websites[i];
                            websiteNow.setIndexUrl(websites[i].getCategory()[j+1]);
                            sendRequestForList(websiteNow);
                            isFind=true;
                            break;
                        }
                    }
                    if (isFind){
                        break;
                    }
                }
            }
        }
    }

    private void loginJudge(){
        if (!username.equals("")){
            //已登录
            usernameShow.setText("Welcome: "+username);
            userIcon.setImageResource(R.drawable.ic_logout);
            editor=pref.edit();
            editor.putString("mark",mark);
            editor.apply();
        }else {
            //未登录
            usernameShow.setText("Welcome: visitor");
            userIcon.setImageResource(R.drawable.ic_login);
        }
    }

    private void doSignInOrOut(){
        Intent intent=new Intent(ListActivity.this,MainActivity.class);
        if (hasLogin){
            //点击ICON注销
            editor.putBoolean("hasLogin",false);
            editor.putString("mark","");
            editor.apply();
            startActivity(intent);
            finish();
        }else {
            //点击ICON登录
            startActivity(intent);
            finish();
        }
    }

    private void UIInit(){
        sendRequestForToolBar();
        RequestOptions options = new RequestOptions().centerCrop();
        Glide
                .with(this)
                .load(R.drawable.head_background)
                .apply(options)
                .into(leftBackground);
        Glide
                .with(this)
                .load(R.drawable.head_background)
                .apply(options)
                .into(rightBackground);

        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignInOrOut();
            }
        });
        //ToolBar 用于打开侧滑菜单的按钮
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.mipmap.ic_launcher_round);
        }
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        //瀑布流
        recyclerView=(RecyclerView)findViewById(R.id.recycle_view);
        StaggeredGridLayoutManager layoutManager=new
                StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);//列数2
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        //设置loading颜色 最多4个
        //swipeRefreshLayout.setColorSchemeColors();
        //首次进入先显示加载中
        swipeRefreshLayout.setRefreshing(true);
        //手动下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //获取最新数据并刷新
                if (isRefreshing==0){
                    isRefreshing=1;
                    refreshPlace="top";
                    sendRequestForList(websiteNow);
                    Log.d("refresh","top is going to refresh!");
                }
            }
        });
    }

    private void menuInit(){
        //动态生成侧滑菜单(Left)
        Menu menuLeft=navViewLeft.getMenu();
        menuLeft.clear();
        if (websites.length!=0){
            for (int i=0;i<websites.length;i++){
                if (websites[i]==null){
                    continue;
                }
                menuLeft.add(R.id.group_left,i,i,websites[i].getWebSiteName());
            }
        }
        navViewLeft.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //点击左侧item之后的操作
                adapter.getWebContents().clear();//要重新指向一次才能检测到刷新
                adapter.notifyDataSetChanged();
                drawerLayout.closeDrawers();
                int position=0;
                for (int i=0;i<websites.length;i++){
                    if (websites[i]==null){
                        continue;
                    }
                    if (item.getTitle().equals(websites[i].getWebSiteName())){
                        position=i;
                    }
                }
                sendRequestForList(websites[position]);
                swipeRefreshLayout.setRefreshing(true);
                isRefreshing=1;
                refreshPlace="top";
                Log.d("refresh","change website refresh!");
                collapsingToolbarLayout.setTitle(websiteNow.getWebSiteName());
                return true;
            }
        });
        navViewRight.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //点击右侧item之后的操作
                adapter.getWebContents().clear();//要重新指向一次才能检测到刷新
                adapter.notifyDataSetChanged();
                drawerLayout.closeDrawers();
                int positionOfCategory=0;
                if (websiteNow.getCategory()!=null){
                    for (int i=0;i<websiteNow.getCategory().length/2;i++){
                        if (item.getTitle().equals(websiteNow.getCategory()[2*i])){
                            positionOfCategory=2*i+1;
                        }
                    }
                    websiteNow.setIndexUrl(websiteNow.getCategory()[positionOfCategory]);
                    sendRequestForList(websiteNow);
                    swipeRefreshLayout.setRefreshing(true);
                    isRefreshing=1;
                    refreshPlace="top";
                    Log.d("refresh","change category refresh!");
                    collapsingToolbarLayout.setTitle(websiteNow.getWebSiteName());
                }
                return true;
            }
        });
    }

    private void addWebsite(){

        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ListActivity.this,AddWebsite.class);
                startActivityForResult(intent,1);
            }
        };
        addWebsite.setOnClickListener(onClickListener);
        addWebsiteText.setOnClickListener(onClickListener);
    }

    private void listContentInit(){
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int SCROLL_STATE_IDLE=0;//表示屏幕已停止。屏幕停止滚动时为0
                int SCROLL_STATE_TOUCH_SCROLL=1;//表示正在滚动。当屏幕滚动且用户使用的触碰或手指还在屏幕上时为1
                int SCROLL_STATE_FLING=2;//手指做了抛的动作（手指离开屏幕前，用力滑了一下，屏幕产生惯性滑动）
                //沉浸式
                if(newState == SCROLL_STATE_FLING){
                    /*//隐藏?需要
                    systemBar.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//显示导航栏
                                    //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN//显示状态栏
                                    //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//不显示导航栏
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN//不显示状态栏
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE);//沉浸模式
                    */
                }
                if(newState == SCROLL_STATE_IDLE){
                    //systemBar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
                //划到底部刷新
                if(!recyclerView.canScrollVertically(1)){//检测划到了底部
                    if (isRefreshing==0){
                        isRefreshing=1;
                        refreshPlace="bottom";
                        Log.d("refresh","bottom is going to refresh!");
                        browser.nextPage();//发送加载下一页的请求
                        isNextPage=true;
                        sendRequestForList(websiteNow);
                        snackbar = Snackbar.make(collapsingToolbarLayout, "Loading", Snackbar.LENGTH_INDEFINITE);
                        snackbar.getView().getBackground().setAlpha(100);
                        snackbar.show();
                    }
                }else if(!recyclerView.canScrollVertically(-1)) {//检测划到了顶部
                    //systemBar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

            }
        });
    }

    private void push(){

    }

    private void sendRequestForList(Website website){
        Observer<ArrayList<WebItem>> observer = new Observer<ArrayList<WebItem>>() {
            private Disposable disposable;
            @Override
            public void onSubscribe(Disposable d) {
                disposable=d;
                Logger.d("subscribe");
            }

            @Override
            public void onNext(ArrayList<WebItem> list) {
                Logger.d("next");
                if (isNextPage){
                    webContentList.addAll(list);
                }else {
                    webContentList.clear();
                    webContentList.addAll(list);
                }
                showList();
            }

            @Override
            public void onError(Throwable e) {
                Logger.d("error");
                disposable.dispose();
                isNextPage=false;
            }

            @Override
            public void onComplete() {
                Logger.d("complete and dispose");
                disposable.dispose();
                isNextPage=false;
            }
        };
        browser.sendRequest(website,observer);
    }

    private void sendRequestForToolBar(){
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Logger.d("subscribe");
            }

            @Override
            public void onNext(String imgSrc) {
                Logger.d("ToolBar get "+imgSrc);
                RequestOptions options = new RequestOptions().error(R.drawable.toolbar).fitCenter();
                Glide
                        .with(ListActivity.this)
                        .load(imgSrc)
                        .apply(options)
                        .into(imageView);
            }

            @Override
            public void onError(Throwable e) {
                Logger.d("error");
            }

            @Override
            public void onComplete() {
                Logger.d("complete");
            }
        };
        BrowserForToolBar.getInstance().sendRequest(observer);
    }

    private void showList(){
        websiteNow=browser.getWebsiteNow();
        if (true){//websiteIndex!=null&&websiteIndex.equals(websiteNow.getIndexUrl())
            sizeThisPage=browser.getSizeThisPage();
            collapsingToolbarLayout.setTitle(websiteNow.getWebSiteName());
            LogUtil.d("finish refresh!");
            adapter.getWebContents().clear();//要重新指向一次才能检测到刷新
            adapter.getWebContents().addAll(webContentList);
            if (refreshPlace.equals("top")){
                recyclerView.smoothScrollToPosition(0);//回到顶端
                adapter.notifyDataSetChanged();
                snackbar = Snackbar.make(collapsingToolbarLayout, "Loading", Snackbar.LENGTH_INDEFINITE);
                snackbar.getView().getBackground().setAlpha(100);
                snackbar.setText("Finish Loading");
                snackbar.setDuration(Snackbar.LENGTH_SHORT);
                snackbar.show();
            }else if (refreshPlace.equals("bottom")){
                snackbar.setText("Finish Loading");
                snackbar.setDuration(Snackbar.LENGTH_SHORT);
                snackbar.show();
                adapter.notifyItemRangeInserted(webContentList.size(),webContentList.size()+sizeThisPage);
            }
            //动态生成侧滑菜单(Right)设置被选中item
            Menu menuRight=navViewRight.getMenu();
            menuRight.clear();
            if (websiteNow.getCategory()!=null){
                for (int i=0;i<websiteNow.getCategory().length/2;i++){
                    menuRight.add(R.id.group_right,i,i,websiteNow.getCategory()[2*i]);
                    menuRight.findItem(i).setCheckable(true);
                    if (websiteNow.getCategory()[2*i+1].equals(websiteNow.getIndexUrl())){
                        navViewRight.setCheckedItem(menuRight.findItem(i).getItemId());
                    }
                }
            }
            //动态生成侧滑菜单(left)设置被选中item
            Menu menuLeft=navViewLeft.getMenu();
            menuLeft.clear();
            if (websites.length!=0){
                for (int i=0;i<websites.length;i++){
                    if (websites[i]==null){
                        continue;
                    }
                    menuLeft.add(R.id.group_left,i,i,websites[i].getWebSiteName());
                    menuLeft.findItem(i).setCheckable(true);
                    if (websites[i].getWebSiteName().equals(websiteNow.getWebSiteName())){
                        navViewLeft.setCheckedItem(menuLeft.findItem(i).getItemId());
                    }
                }
            }
            //设置toolbar
            Menu toolbarMenu= toolbar.getMenu();
            if (isSubscribe(websiteNow.getIndexUrl())){
                toolbarMenu.getItem(0).setIcon(R.drawable.ic_star_white_48dp);//换成已订阅的图标
            }else{
                toolbarMenu.getItem(0).setIcon(R.drawable.ic_star_border_white_48dp);//换成未订阅的图标
            }
            swipeRefreshLayout.setRefreshing(false);
            isRefreshing=0;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
    //右上ToolBar
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Menu toolbarMenu= toolbar.getMenu();
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(Gravity.START);
                break;
            case R.id.subscribe:
                subscribe(websiteNow.getIndexUrl());//执行订阅/取消订阅操作
                if (isSubscribe(websiteNow.getIndexUrl())){
                    toolbarMenu.getItem(0).setIcon(R.drawable.ic_star_white_48dp);//换成已订阅的图标
                    Toast.makeText(this,"Subscribe Successful",Toast.LENGTH_SHORT).show();
                }else{
                    toolbarMenu.getItem(0).setIcon(R.drawable.ic_star_border_white_48dp);//换成未订阅的图标
                    Toast.makeText(this,"Unsubscribe Successful",Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
        return true;
    }

    public void subscribe(String websiteIndex){
        int hasMark=0;
        String[] oldMarks=pref.getString("mark","").split(",");
        ArrayList<String> newMarks=new ArrayList<>();
        for (int i=0;i<oldMarks.length;i++){
            if(oldMarks[0].equals("")){
                break;
            }
            if (oldMarks[i].equals(websiteIndex)){
                //如果已经mark订阅过了 则将其从订阅中删除
                hasMark=1;
                continue;
            }
            newMarks.add(oldMarks[i]);
        }
        if (hasMark==0){
            //没有mark过,添加
            newMarks.add(websiteIndex);
        }
        String[] newStrings=newMarks.toArray(new String[newMarks.size()]);
        StringBuilder newMark=new StringBuilder(newStrings[0]);
        for (int i=1;i<newStrings.length;i++){
            newMark=newMark.append(",").append(newStrings[i]);
        }
        LogUtil.d(newMarks.size());
        editor=pref.edit();
        editor.putString("mark",newMark.toString());
        editor.apply();
        //上传服务器
        /*todo
        final String s=newString;
        final AVQuery<AVObject> query1 = new AVQuery<>("_User");
        query1.whereEqualTo("username", username);
        query1.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                for (AVObject item : list) {
                    item.put("mark", s);
                    item.saveInBackground();
                }
            }
        });
        final AVQuery<AVObject> query2 = new AVQuery<>("_Installation");
        query2.whereEqualTo("installationId", AVInstallation.getCurrentInstallation().getInstallationId());
        query2.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                for (AVObject item : list) {
                    item.put("mark", s);
                    item.saveInBackground();
                }
            }
        });
*/
        LogUtil.d("mark "+newMark.toString());
    }

    public boolean isSubscribe(String websiteIndex){
        boolean hasMark=false;
        String[] strings=mark.split(",");
        for (int i=0;i<strings.length;i++){
            if (strings[i].equals(websiteIndex)){
                //如果已经mark订阅过了 则将其从订阅中删除
                hasMark=true;
            }
        }
        return hasMark;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==1&&requestCode==1){
            LogUtil.d("get result from AddWebsite");
            Menu menuLeft=navViewLeft.getMenu();
            menuLeft.clear();
            if (websites.length!=0){
                for (int i=0;i<websites.length;i++){
                    if (websites[i]==null){
                        continue;
                    }
                    menuLeft.add(R.id.group_left,i,i,websites[i].getWebSiteName());
                    menuLeft.findItem(i).setCheckable(true);
                    if (websites[i].getWebSiteName().equals(websiteNow.getWebSiteName())){
                        navViewLeft.setCheckedItem(menuLeft.findItem(i).getItemId());
                    }
                }
            }
        }
    }

    //双击返回退出
    private Handler mHandler= new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "Press Back Again To Exit", Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    public void forPush(String index){
        adapter2.getWebContents().clear();//要重新指向一次才能检测到刷新
        adapter2.notifyDataSetChanged();

        for (int i=0;i<websites.length;i++){
            if (websites[i].getCategory()==null){
                continue;
            }
            for (int j=0;j<websites[i].getCategory().length;j+=2){
                if (websites[i].getCategory()[j+1].equals(index)){
                    websiteNow=websites[i];
                    websiteNow.setIndexUrl(websites[i].getCategory()[j+1]);
                    swipeRefreshLayout.setRefreshing(true);
                    sendRequestForList(websiteNow);
                }
            }
        }
    }
}