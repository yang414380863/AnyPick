package com.yang.AnyPick.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.yang.AnyPick.push.PushService;
import com.yang.AnyPick.R;
import com.yang.AnyPick.basic.ActivityCollector;
import com.yang.AnyPick.basic.BaseActivity;
import com.yang.AnyPick.basic.Client;
import com.yang.AnyPick.basic.FileUtil;
import com.yang.AnyPick.basic.JsonUtils;
import com.yang.AnyPick.basic.LogUtil;
import com.yang.AnyPick.web.*;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

//列表Activity
public class ListActivity extends BaseActivity {
    //瀑布流列表
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    //侧滑菜单
    private DrawerLayout drawerLayout;
    private NavigationView navViewRight;
    private NavigationView navViewLeft;
    private View navViewLeftHeader;
    private ImageView leftBackground;
    private ImageView rightBackground;
    //下拉刷新 监听器
    private SwipeRefreshLayout swipeRefreshLayout;
    private Snackbar snackbar;
    //标题栏
    private ImageView imageView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    //toolbar_list
    private Toolbar toolbar;
    //获取用户
    private SharedPreferences pref;
    //写入订阅
    private SharedPreferences.Editor editor;
    //用户信息标题栏
    private TextView usernameShow;
    private ImageView userIcon;
    //删除网站
    private ImageButton removeWebsite;
    private TextView removeWebsiteText;
    //登录
    private String mark;
    private boolean hasLogin;
    private String username;
    private String password;
    //初始化网站LIST
    static Website[] websites;
    static String[] websiteNameList;
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
    //定义一个变量，来标识是否退出
    private static boolean isExit = false;
    //推送服务
    Intent pushService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        init();
        initWebsiteList();
        WelcomePicShow();
        pushServiceInit();
        UIInit();
        menuListenerInit();
        listListenerInit();
        //homepageChoose();
    }

    private void init(){
        EventBus.getDefault().register(this);
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        mark=pref.getString("mark","");
        hasLogin=pref.getBoolean("hasLogin",false);
        username=pref.getString("username","");
        password=pref.getString("password","");
        browser=Browser.getInstance();
        webContentList=new ArrayList<>();
        adapter=new ListAdapter(this);
        pushService=new Intent(this, PushService.class);
        // 沉浸式
        //final View systemBar = findViewById(collapsing_toolbar);
        //折叠标题栏
        imageView=(ImageView)findViewById(R.id.image_view);
        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("Loading...");
        isNextPage=false;
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
        removeWebsite =(ImageButton)navViewLeftHeader.findViewById(R.id.remove_website);
        removeWebsiteText =(TextView)navViewLeftHeader.findViewById(R.id.remove_website_text);
        //下滑刷新
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        //瀑布流
        recyclerView=(RecyclerView)findViewById(R.id.recycle_view);
    }

    private void initWebsiteList(){
        if (!hasLogin){
            //游客身份 不从服务器获取
            websiteNameList=WebsiteInit.getWebsiteNameList();
            websites=WebsiteInit.getWebsiteList();
            StringBuilder s=new StringBuilder(websiteNameList[0]);
            for (int i=1;i<websiteNameList.length;i++){
                s=s.append(",").append(websiteNameList[i]);
            }
            editor=pref.edit();
            for (int i=0;i<websites.length;i++){
                editor.putString(websites[i].getWebSiteName(), JsonUtils.ObjectToJson(websites[i]));
                LogUtil.d(pref.getString(websites[i].getWebSiteName(),""));
            }
            editor.apply();
            menuContentRefresh();
            homepageChoose();
        }else {
            //用户身份 从服务器获取List
            //从服务器下载用户的Local
            new Client().sendForResult("getLocal "+username+" "+password,"getLocal");
        }
    }

    private void WelcomePicShow(){
        if (hasLogin){
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

    private void pushServiceInit(){
        pushService.putExtra("username",username);
        startService(pushService);
    }

    private void doSignInOrOut(){
        Intent intent=new Intent(ListActivity.this,Login.class);
        if (hasLogin){
            //点击ICON注销
            editor=pref.edit();
            editor.putBoolean("hasLogin",false);
            editor.putString("username","");
            editor.putString("password","");
            editor.putString("mark","");
            editor.apply();
            PushService.closeAlarm();
            stopService(pushService);
            startActivity(intent);
            finish();
        }else {
            //点击ICON登录
            stopService(pushService);
            startActivity(intent);
            finish();
        }
    }
    private void showOrHideDelete(){
        Menu menuLeft=navViewLeft.getMenu();
        if (menuLeft==null||menuLeft.findItem(0)==null){
            return;
        }
        if (menuLeft.findItem(0).getIcon()!=null){
            if (websites.length!=0){
                for (int i=0;i<websites.length;i++){
                    menuLeft.findItem(i).setIcon(null);
                }
            }
        }else{
            if (websites.length!=0){
                for (int i=0;i<websites.length;i++){
                    menuLeft.findItem(i).setIcon(R.drawable.ic_remove_black_48dp);
                }
            }
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

        //ToolBar 用于打开侧滑菜单的按钮
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.mipmap.ic_launcher_round);
        }
        StaggeredGridLayoutManager layoutManager=new
                StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);//列数2
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        //设置loading颜色 最多4个
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorAccent),getResources().getColor(R.color.colorBlack));
        //首次进入先显示加载中
        //swipeRefreshLayout.setRefreshing(true);
        //手动下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //获取最新数据并刷新
                if (isRefreshing==0){
                    isRefreshing=1;
                    isNextPage=false;
                    browser.resetPage();
                    sendRequestForList(websiteNow);
                    Log.d("refresh","top is going to refresh!");
                }
            }
        });
    }

    private void menuContentRefresh(){
        //动态生成侧滑菜单(Left) 包括:同步更新/删除 item,设置被选中项
        LogUtil.d("menuContentRefresh");
        websiteNameList=WebsiteInit.getWebsiteNameList();
        websites=WebsiteInit.getWebsiteList();
        Menu menuLeft=navViewLeft.getMenu();
        menuLeft.clear();
        if (websites.length!=0){
            for (int i=0;i<websites.length;i++){
                if (websites[i]==null){
                    continue;
                }
                menuLeft.add(R.id.group_left,i,i,websites[i].getWebSiteName());
                menuLeft.findItem(i).setCheckable(true);
                //动态设置侧滑菜单(left)被选中item
                if (websiteNow!=null&&websites[i].getWebSiteName().equals(websiteNow.getWebSiteName())){
                    navViewLeft.setCheckedItem(menuLeft.findItem(i).getItemId());
                }
            }
        }
        menuLeft.add(R.id.group_left,websites.length,websites.length, R.string.Add_Website);
        menuLeft.findItem(websites.length).setIcon(R.drawable.ic_add_black_48dp);
    }

    private void menuListenerInit(){
        navViewLeft.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getTitle().equals(getString(R.string.Add_Website))){
                    //点击添加网页按钮
                    Intent intent=new Intent(ListActivity.this,MarketActivity.class);
                    startActivity(intent);
                }else {
                    Menu menuLeft=navViewLeft.getMenu();
                    if (menuLeft.findItem(0).getIcon()==null){
                        //正常情况下点击左侧item之后的操作
                        drawerLayout.closeDrawers();
                        adapter.getWebContents().clear();//要重新指向一次才能检测到刷新
                        adapter.notifyDataSetChanged();
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
                        //swipeRefreshLayout.setRefreshing(true);
                        isRefreshing=1;
                        isNextPage=false;
                        Log.d("refresh","change website refresh!");
                        collapsingToolbarLayout.setTitle(websiteNow.getWebSiteName());
                    }else {
                        //删除状态下点击
                        String name=item.getTitle().toString();
                        String[] locals=pref.getString("local","").split("!@#");
                        StringBuilder local=new StringBuilder("");
                        for (int i=0;i<locals.length;i++){
                            if (!name.equals(locals[i])){
                                if (i!=locals.length-1){
                                    local.append(locals[i]).append("!@#");
                                }else {
                                    local.append(locals[i]);
                                }
                            }
                        }
                        if (!username.equals("")){
                            new Client().sendForResult("updateLocal "+username+" "+password+" "+local,"updateLocal");
                            pref.edit().putString("local",local.toString()).apply();
                        }
                        FileUtil.deleteFromData(username,name);
                        menuContentRefresh();
                    }

                }
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
                    //swipeRefreshLayout.setRefreshing(true);
                    isRefreshing=1;
                    isNextPage=false;
                    Log.d("refresh","change category refresh!");
                    collapsingToolbarLayout.setTitle(websiteNow.getWebSiteName());
                }
                return true;
            }
        });
        //userIcon监听
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignInOrOut();
            }
        });
        //删除切换按钮
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideDelete();
            }
        };
        removeWebsite.setOnClickListener(onClickListener);
        removeWebsiteText.setOnClickListener(onClickListener);

    }

    private void listListenerInit(){
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
                    if (isRefreshing==0&&websiteNow!=null){
                        isRefreshing=1;
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

    private void firstInit(){
        //安装后首次进入APP
        websiteNow=websites[0];
        sendRequestForList(websites[0]);//默认首页第一个
    }
    private void homepageChoose(){
        //无推送时默认进入上次最后打开的页面,之前未使用过则打开默认主页
        String indexNow=pref.getString("lastIndex","");
        if (indexNow.equals("")){
            firstInit();
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
            }
            if (!isFind){
                websiteNow=websites[0];
            }
        }
    }

    private void sendRequestForList(Website website){
        swipeRefreshLayout.setRefreshing(true);
        LogUtil.d("Request: "+website.getIndexUrl());
        //关闭侧滑菜单
        drawerLayout.closeDrawers();
        final Observer<ArrayList<WebItem>> observer = new Observer<ArrayList<WebItem>>() {
            private Disposable disposable;
            @Override
            public void onSubscribe(Disposable d) {
                disposable=d;
                Logger.d("onSubscribe");
            }

            @Override
            public void onNext(ArrayList<WebItem> list) {
                Logger.d("onNext");
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
                Logger.d("onError");
                disposable.dispose();
                isNextPage=false;
                Toast.makeText(ListActivity.this,"Network connection failure",Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                isRefreshing=0;
            }

            @Override
            public void onComplete() {
                Logger.d("onComplete and dispose");
                swipeRefreshLayout.setRefreshing(false);
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
                RequestOptions options = new RequestOptions().error(R.drawable.toolbar).fitCenter().dontAnimate();//无载入动画;
                Glide
                        .with(ListActivity.this)
                        .load(imgSrc)
                        .thumbnail(0.1f)
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
            if (!isNextPage){
                recyclerView.smoothScrollToPosition(0);//回到顶端
                adapter.notifyDataSetChanged();
                snackbar = Snackbar.make(collapsingToolbarLayout, "Loading", Snackbar.LENGTH_INDEFINITE);
                snackbar.getView().getBackground().setAlpha(100);
                snackbar.setText("Finish Loading");
                snackbar.setDuration(Snackbar.LENGTH_SHORT);
                snackbar.show();
            }else if (isNextPage){
                snackbar.setText("Finish Loading");
                snackbar.setDuration(Snackbar.LENGTH_SHORT);
                snackbar.show();
                adapter.notifyItemRangeInserted(webContentList.size(),webContentList.size()+sizeThisPage);
            }
            //动态设置侧滑菜单(Right)被选中item
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
            menuContentRefresh();
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
        EventBus.getDefault().unregister(this);
    }
    //右上ToolBar
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_list,menu);
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
                if(websiteNow==null){
                    break;
                }
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
        String[] oldMarks=pref.getString("mark","").split("!@#");
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
        StringBuilder newMark;
        if (newStrings.length==0){
            newMark=new StringBuilder("");
        }else {
            newMark=new StringBuilder(newStrings[0]);
        }
        for (int i=1;i<newStrings.length;i++){
            newMark=newMark.append("!@#").append(newStrings[i]);
        }
        LogUtil.d("Marks.size(): "+newMarks.size());
        editor=pref.edit();
        editor.putString("mark",newMark.toString());
        editor.apply();
        mark=pref.getString("mark","");
        if (!username.equals("")){
            Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                    new Client().sendForResult(emitter,"updateMark "+username+" "+password+" "+mark);
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            LogUtil.d("subscribe success: " + s);
                        }
                    });
        }
    }
    public boolean isSubscribe(String websiteIndex){
        boolean hasMark=false;
        String[] strings=mark.split("!@#");
        for (int i=0;i<strings.length;i++){
            if (strings[i].equals(websiteIndex)){
                //如果已经mark订阅过了 则将其从订阅中删除
                hasMark=true;
            }
        }
        return hasMark;
    }

    @Subscribe(threadMode = ThreadMode.MAIN , sticky = true)
    public void getFromEventBus(String event){
        switch (event.split(" ")[0]){
            case "getPush": {
                //点击通知之后的操作
                LogUtil.d("Get Push");
                String index=event.split(" ")[1];
                ActivityCollector.finishExcept(ListActivity.this);
                LogUtil.d("Push Index: "+index);
                boolean isFind=false;
                for (int i=0;i<websites.length;i++){
                    if (websites[i].getCategory()==null){
                        if (websites[i].getIndexUrl().equals(index)){
                            websiteNow=websites[i];
                            sendRequestForList(websiteNow);
                            //swipeRefreshLayout.setRefreshing(true);
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
                                //swipeRefreshLayout.setRefreshing(true);
                                isFind=true;
                                break;
                            }
                        }
                        if (isFind){
                            break;
                        }
                    }
                }
                break;
            }
            case "refreshMenu":{
                //添加了新Website后操作
                LogUtil.d("refreshMenu");
                menuContentRefresh();
                break;
            }
            case "updateLocal":{
                Toast.makeText(this,"Synchro Success",Toast.LENGTH_LONG).show();
                break;
            }
            case "getLocal":{
                if (event.split(" ").length>2){
                    String get=event.split(" ")[2];
                    pref.edit().putString("local",get).apply();
                    String[] locals=get.split("!@#");
                    for (int i=0;i<locals.length;i++){
                        final String tmp=locals[i];
                        Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                new Client().sendForResult(emitter,"marketGetDetail "+tmp);
                            }
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String s) throws Exception {
                                        LogUtil.d("onNextGet: " + s);
                                        FileUtil.writeFileToData(username,tmp,s);
                                        menuContentRefresh();
                                        homepageChoose();
                                    }
                                });
                    }
                }else {
                    menuContentRefresh();
                    homepageChoose();
                }
                break;
            }
            default:{}
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
            ActivityCollector.finishExcept(this);
            finish();
        }
    }

}