package com.menglvren.visit.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.menglvren.visit.NetConfig;
import com.menglvren.visit.R;
import com.menglvren.visit.model.IpListCache;
import com.menglvren.visit.model.Server;
import com.menglvren.visit.model.VipListCache;
import com.menglvren.visit.util.ProxySetting;

import java.util.HashSet;

/***
 * 每10s刷一个节目：载入节目页面后，3.5秒点击，点击耗时0.5秒，6秒播放
 * 播放失败会删除相应代理，最后的有效代理在退出时会持久化存储
 */
public class MainActivity extends Activity {
    public static final int EVENT_SIMULATE_CLICK = 0;
    public static final int EVENT_CHANGE_PROXY = 1;
    public static int DELAY_PLAY = 3500;
    public static int DELAY_SIMULATE_CLICK = 500;

    WebView mainWeb;
    WebView newWeb;
    Handler handler;

    boolean isJSInserted=false;
    boolean vipFlag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case EVENT_SIMULATE_CLICK:
                        setSimulateClick(newWeb, newWeb.getWidth() / 2, newWeb.getWidth() / 2);
                        break;
                    case EVENT_CHANGE_PROXY:
                        configProxy(newWeb);
                        updateIndex(1);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        vipFlag=getIntent().getBooleanExtra("isVip",false);
        configMainWeb();
        configNewWeb();
    }

    private void configMainWeb() {
        mainWeb = (WebView) findViewById(R.id.web);
        mainWeb.getSettings().setJavaScriptEnabled(true);
        mainWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mainWeb.getSettings().setSupportMultipleWindows(true);
        mainWeb.getSettings().setUserAgentString(NetConfig.agent);
        mainWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i("ly", "mainWeb status-->start url:" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("ly", "mainWeb status-->finish url:" + url);

                if (!isJSInserted) {
                    view.loadUrl("javascript:" + jsAction());
                    isJSInserted = true;
                }
            }
        });
        mainWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWeb);
                resultMsg.sendToTarget();

                Log.i("ly", "mainWeb status-->create new window");
                if (newWeb.getVisibility() != View.VISIBLE) {
                    newWeb.setVisibility(View.VISIBLE);
                }

                return true;
            }
        });
        mainWeb.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void changeProxy() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(EVENT_CHANGE_PROXY);
                    }
                });

            }
        }, "proxy");

        mainWeb.loadUrl(NetConfig.home);
    }

    private void configNewWeb() {
        newWeb = (WebView) findViewById(R.id.newWeb);
        newWeb.getSettings().setJavaScriptEnabled(true);
        newWeb.getSettings().setAppCacheEnabled(false);
        newWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        newWeb.getSettings().setUserAgentString(NetConfig.agent);
        newWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                Log.i("ly", "newWeb status-->start url:" + url + " title->" + view.getTitle() + " icon->" + favicon);
                if (!isValid(view.getTitle(), url)) {
                    removeCurrentProxy();
                    //view.stopLoading();
                    updateIndex(1);
                    configProxy(newWeb);
                    return;
                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("ly", "newWeb status-->finish url:" + url + " title-->" + view.getTitle() + " icon->" + view.getFavicon());
                handler.sendEmptyMessageDelayed(EVENT_SIMULATE_CLICK, DELAY_PLAY);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.i("ly", "newWeb status-->error:" + description);

            }
        });

    }
    private boolean isValid(String title,String url){
        if(title.contains("找不到网页")
            || title.contains("403")
            || title.contains("404")
            || title.contains("ERROR")
            || title.contains("Proxy")
            || title.contains("错误")
            || url.contains("403")
            || url.contains("404")){
            return false;
        }
        return true;
    }
    private void updateIndex(int delt){
        if(NetConfig.validIps.size()==0){
            Toast.makeText(MainActivity.this,"代理已清空",Toast.LENGTH_SHORT).show();
            finish();
        }else{
            NetConfig.validIpIndex=(NetConfig.validIpIndex+delt+NetConfig.validIps.size())%NetConfig.validIps.size();
            if(NetConfig.validIpIndex==0){
                //Toast.makeText(MainActivity.this,"代理已用完",Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void removeCurrentProxy(){
        String ip=System.getProperty("http.proxyHost");
        String port=System.getProperty("http.proxyPort");
        Server errServer=new Server(ip,port);
        if(NetConfig.validIps.contains(errServer)) {
            NetConfig.validIps.remove(errServer);
            Log.i("ly", "remove ip-->" + ip + " from validIps, validIps size-->" + NetConfig.validIps.size());
            updateIndex(-1);
        }
    }
    private void configProxy(WebView web){
        Server server = NetConfig.validIps.get(NetConfig.validIpIndex);
        Log.i("ly", "mainWeb status-->config proxy " + server.ip+":"+server.port);
        String ip=server.ip;
        int port=Integer.parseInt(server.port);
        if(Build.VERSION.SDK_INT== Build.VERSION_CODES.KITKAT){
            ProxySetting.setKitKatWebViewProxy(web.getContext().getApplicationContext(), ip, port);
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            ProxySetting.setProxyICSPlus(web,ip,port,"");
        }else if(Build.VERSION.SDK_INT==Build.VERSION_CODES.ICE_CREAM_SANDWICH
                || Build.VERSION.SDK_INT==Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            ProxySetting.setProxyICS(web,ip,port);
        }else if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.HONEYCOMB_MR2){
            ProxySetting.setProxyUpToHC(web,ip,port);
        }else {
            Toast.makeText(MainActivity.this, "仅支持android4.4及以下系统", Toast.LENGTH_LONG).show();
        }

        web.clearCache(true);
        web.clearHistory();
        web.clearFormData();
    }
    private void configProxy(WebView web,String ip,int port){
        if(Build.VERSION.SDK_INT== Build.VERSION_CODES.KITKAT){
            ProxySetting.setKitKatWebViewProxy(web.getContext().getApplicationContext(), ip, port);
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            ProxySetting.setProxyICSPlus(web,ip,port,"");
        }else if(Build.VERSION.SDK_INT==Build.VERSION_CODES.ICE_CREAM_SANDWICH
                || Build.VERSION.SDK_INT==Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            ProxySetting.setProxyICS(web,ip,port);
        }else if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.HONEYCOMB_MR2){
            ProxySetting.setProxyUpToHC(web,ip,port);
        }else {
            Toast.makeText(MainActivity.this, "仅支持android4.4及以下系统", Toast.LENGTH_LONG).show();
        }

        web.clearCache(true);
        web.clearHistory();
        web.clearFormData();
    }
    private String jsAction() {
        // String jsStr="document.getElementById(\"alink1\").click()";

        String jsStr = "task();" +
                "function task(){" +
                    "var i=0;" +
                    "window.proxy.changeProxy();"+
                    "clickItems(i);" +
                    "var len=document.getElementsByClassName(\"v-link\").length;" +
                    "setTimeout(\"task()\",10000*len);" +
                "};" +
                "function clickItems(i){" +
                    "var collection=document.getElementsByClassName(\"v-link\");" +
                    "var len=collection.length;" +
                    "if(i<len){" +
                        "setTimeout(function(){collection[i].children[0].click();clickItems(++i);},10000);" +
                    "}" +
                "}";

        return jsStr;
    }

    private void setSimulateClick(View view, float x, float y) {
        Log.i("ly", "x,y->" + x + "," + y);
        long downTime = SystemClock.uptimeMillis();
        final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        downTime += DELAY_SIMULATE_CLICK;
        final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_UP, x, y, 0);
        view.onTouchEvent(downEvent);
        view.onTouchEvent(upEvent);
        downEvent.recycle();
        upEvent.recycle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        mainWeb.destroy();
        newWeb.destroy();
        ProxySetting.cancelProxy();

        IpListCache ipListCache=new IpListCache(MainActivity.this);
        HashSet<Server> temp=new HashSet<>();
        temp.addAll(NetConfig.validIps);
        ipListCache.saveIpList(temp);

        if(vipFlag){
            VipListCache vipListCache=new VipListCache(MainActivity.this);
            HashSet<Server> temp1=new HashSet<>();
            temp1.addAll(NetConfig.validIps);
            vipListCache.saveVipList(temp1);
        }

        NetConfig.servers.clear();
        NetConfig.validIps.clear();
        NetConfig.badIps.clear();
        NetConfig.validIpIndex =0;

        System.exit(0);
    }
}
