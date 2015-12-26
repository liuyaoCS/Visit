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
import com.menglvren.visit.model.Server;
import com.menglvren.visit.util.ProxySetting;

/***
 * 每10s刷一个节目：载入节目页面后，3.5秒点击，点击耗时0.5秒，6秒播放
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

    private String mIp;
    private int mPort;

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
                        Server server = NetConfig.validIps.get(NetConfig.validIpIndex);
                        NetConfig.validIpIndex = (NetConfig.validIpIndex + 1) % NetConfig.validIps.size();
                        Log.i("ly", "mainWeb status-->config proxy ip=" + server.ip);
                        configProxy(newWeb, server.ip, Integer.parseInt(server.port));

                        break;
                    default:
                        break;
                }
                return true;
            }
        });


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
        newWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i("ly", "newWeb status-->start url:" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("ly", "newWeb status-->finish url:" + url);

                handler.sendEmptyMessageDelayed(EVENT_SIMULATE_CLICK, DELAY_PLAY);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.i("ly", "newWeb status-->error:" + description);
            }
        });

    }
    private void configProxy(WebView web,String ip,int port){
        if(Build.VERSION.SDK_INT== Build.VERSION_CODES.KITKAT){
            ProxySetting.setKitKatWebViewProxy(web.getContext().getApplicationContext(), ip, port);
            //ProxySetting.clearKitKatWebViewProxy(web.getContext().getApplicationContext());
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            ProxySetting.setProxyICSPlus(web,ip,port,"");
        }else{
            Toast.makeText(MainActivity.this, "仅支持android版本4.1-4.4", Toast.LENGTH_LONG).show();
        }

        //
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
                    "setTimeout(\"task()\",10000*2);" +
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
        //ProxySetting.clearKitKatWebViewProxy(getApplicationContext());
        NetConfig.servers.clear();
        NetConfig.validIps.clear();
        NetConfig.badIps.clear();
        NetConfig.validIpIndex =0;
        System.exit(0);
    }
}
