package com.menglvren.visit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class  GenerateProxyActivity extends Activity {
    Button generate,check,start;
    TextView generate_text,check_text;

    private Handler handler;
    private static final int GENERATE_PROXY=0;
    private static final int UPDATE_VALIDIP_PROXY=1;
    private static final int CHECK_FINISH=2;

    private int generate_click_count=0;
    private final int TIME_OUT=2*1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_proxy);

        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case GENERATE_PROXY:
                        generate_text.setText("代理数量："+NetConfig.servers.size());
                        break;
                    case UPDATE_VALIDIP_PROXY:
                        check_text.setText("有效代理："+NetConfig.validIps.size());
                        break;
                    case CHECK_FINISH:
                        check_text.setText("有效代理："+NetConfig.validIps.size()+" done!");
                        lauchMain();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        generate= (Button) findViewById(R.id.generate);
        check= (Button) findViewById(R.id.check);
        start= (Button) findViewById(R.id.start);
        generate_text= (TextView) findViewById(R.id.generate_text);
        check_text= (TextView) findViewById(R.id.check_text);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate_click_count++;
                generateProxys(generate_click_count);
            }
        });
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        for(Server server:NetConfig.servers){
                            if(isValidIP(server.ip,server.port)){
                                Log.i("ly","valid ip-->"+server.ip);
                                NetConfig.validIps.add(server);
                                handler.sendEmptyMessage(UPDATE_VALIDIP_PROXY);
                                //if(NetConfig.validIps.size()>1)break;
                            }else{
                                Log.i("ly","bad ip-->"+server.ip);
                                NetConfig.badIps.add(server);
                            }
                        }
                        handler.sendEmptyMessage(CHECK_FINISH);
                    }
                }.start();

            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lauchMain();
            }
        });
    }
    private void lauchMain(){
        cancelProxy();
        Intent it=new Intent(GenerateProxyActivity.this,MainActivity.class);
        startActivity(it);
        finish();
    }
    private void generateProxys(final int click_count){
        final int page=click_count;
        new Thread(){
            public void run() {
                HttpGet get=new HttpGet(NetConfig.URL_GETPROXY+page);
                get.setHeader("Cache-Control", "no-cache");
                get.setHeader("User-Agent", NetConfig.agent);

                HttpParams params = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(params, TIME_OUT); //设置连接超时
                HttpConnectionParams.setSoTimeout(params, TIME_OUT); //设置请求超时
                get.setParams(params);

                HttpClient hClient = null;

                try {

                    hClient=new DefaultHttpClient();
                    HttpResponse hResponse=hClient.execute(get);
                    if(hResponse.getStatusLine().getStatusCode()==200){

                        BufferedReader br = new BufferedReader(new InputStreamReader(hResponse.getEntity().getContent(),"utf-8"));
                        StringBuilder sbBuilder=new StringBuilder();
                        String line = null;

                        while ((line = br.readLine()) != null){
                            sbBuilder.append(line + "\n");
                        }
                        br.close();

                        Pattern p = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)[\\s\\S]+?(\\d+)");
                        Matcher m = p.matcher(sbBuilder);

                        while(m.find()) {

                            Log.i("ly", "ip:" + m.group(1));
                            Log.i("ly", "port:" + m.group(2));
                            NetConfig.servers.add(new Server(m.group(1), m.group(2)));
                        }
                        handler.sendEmptyMessage(GENERATE_PROXY);
                    }else{
                        Log.e("ly", "err, get servers from local");
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            };
        }.start();
    }
    private boolean isValidIP(String ip,String port){
        HttpGet get=new HttpGet(NetConfig.home);
        get.setHeader("User-Agent", NetConfig.agent);
        get.setHeader("Cache-Control", "no-cache");

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIME_OUT); //设置连接超时
        HttpConnectionParams.setSoTimeout(params, TIME_OUT); //设置请求超时
        get.setParams(params);

        System.setProperty("http.proxyHost", ip);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyHost", ip);
        System.setProperty("https.proxyPort", port);

        HttpClient hClient = null;
        try {
            hClient=new DefaultHttpClient();
            HttpResponse  hResponse=hClient.execute(get);
            if(hResponse.getStatusLine().getStatusCode()==200){
                return true;
            }else{
                return false;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    private void cancelProxy(){
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort","");
        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("ly", "onDestroy");
        handler.removeCallbacksAndMessages(null);
        cancelProxy();
    }
}
