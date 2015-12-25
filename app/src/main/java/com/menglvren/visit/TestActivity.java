package com.menglvren.visit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


public class TestActivity extends Activity {

    WebView web;
    Button change;
    int index=0;

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        });

        change= (Button) findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configWebview(web, NetConfig.validIps.get(index).ip, Integer.parseInt(NetConfig.validIps.get(index).port));
                index++;
                if(index%2==0){
                    web.loadUrl("http://wap.baidu.com");
                }else{
                    web.loadUrl("http://m.chinaso.com");
                }

                Log.i("ly","ip-->"+NetConfig.validIps.get(index).ip);
            }
        });

        web= (WebView) findViewById(R.id.web);
        initWebview(web);


    }
    private void configWebview(WebView web,String ip,int port){
        if(Build.VERSION.SDK_INT== Build.VERSION_CODES.KITKAT){
            ProxySetting.setKitKatWebViewProxy(web.getContext().getApplicationContext(),ip,port);
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            ProxySetting.setProxyICSPlus(web,ip,port,"");
        }else{
            Toast.makeText(TestActivity.this, "仅支持android版本4.1-4.4", Toast.LENGTH_LONG).show();
        }

        //
        web.clearCache(true);
        web.clearHistory();
        web.clearFormData();
    }

    private void initWebview(final WebView web){
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setUserAgentString(NetConfig.agent);
        web.getSettings().setAppCacheEnabled(false);
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                web.loadUrl(url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("ly", "start");
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.i("ly", description);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("ly", "finish");

            }
        });
    }

}
