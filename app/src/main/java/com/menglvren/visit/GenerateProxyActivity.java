package com.menglvren.visit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class  GenerateProxyActivity extends Activity {
    Button generate,check,start,clearCache;
    TextView generate_text,check_text,log;
    RadioButton manual,vip;
    RadioButton socket,filter;

    private Handler handler;
    private static final int GENERATE_PROXY=0;
    private static final int UPDATE_VALIDIP_PROXY=1;
    private static final int CHECK_FINISH=2;
    private static final int UPDATE_LOG=3;

    private int generate_click_count=0;
    private final int TIME_OUT=1*1000;

    private boolean isInterrupted =false;
    ExecutorService service= Executors.newFixedThreadPool(10);

    IpListCache ipListCache;
    HashSet<String> whiteIpList,blackIpList;

    private String currentSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_proxy);

        initServer();
        initIpCache();
        initHandler();
        initView();

    }

    private void initServer(){
        NetConfig.servers.clear();
        NetConfig.validIps.clear();
        NetConfig.badIps.clear();
        NetConfig.validIpIndex =0;
    }
    private void initIpCache(){
        ipListCache=new IpListCache(this);
        whiteIpList=ipListCache.getWhiteIpList();
        if(whiteIpList==null){
            whiteIpList=new HashSet<>();
        }
        blackIpList=ipListCache.getBlackIpList();
        if(blackIpList==null){
            blackIpList=new HashSet<>();
        }
    }
    private void initHandler(){
        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case GENERATE_PROXY:
                        generate_text.setText("代理数量："+NetConfig.servers.size());
                        check.setEnabled(true);
                        if(vip.isChecked() || manual.isChecked()){
                            generate.setEnabled(false);
                        }
                        break;
                    case UPDATE_VALIDIP_PROXY:
                        check_text.setText("有效代理：" + NetConfig.validIps.size());
                        if(NetConfig.validIps.size()>0){
                            start.setEnabled(true);
                        }
                        String htmlStr = "<font color=\"#ff0000\">"+msg.obj.toString()+"</font><br>";
                        log.append(Html.fromHtml(htmlStr));
                        break;
                    case UPDATE_LOG:
                        log.append(msg.obj.toString());
                        break;
                    case CHECK_FINISH:
                        check_text.setText("有效代理："+NetConfig.validIps.size()+" done!");
                        launchMain();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }
    private void initView(){
        generate= (Button) findViewById(R.id.generate);
        check= (Button) findViewById(R.id.check);
        start= (Button) findViewById(R.id.start);
        generate_text= (TextView) findViewById(R.id.generate_text);
        check_text= (TextView) findViewById(R.id.check_text);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manual.isChecked()){
                    generateManualProxys();
                }else if(vip.isChecked()){
                    generateVIPProxys();
                }else{
                    generate_click_count++;
                    generateProxys(generate_click_count);
                }
            }
        });
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(socket.isChecked()){
                   checkProxysBySocket();
               }else {
                   checkProxys();
               }

            }
        });
        check.setEnabled(false);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMain();
            }
        });
        start.setEnabled(false);


        manual = (RadioButton) findViewById(R.id.manual);
        vip= (RadioButton) findViewById(R.id.vip);
        filter = (RadioButton) findViewById(R.id.check_filter);
        socket=(RadioButton) findViewById(R.id.check_socket);


        log= (TextView) findViewById(R.id.log);

        clearCache= (Button) findViewById(R.id.clear_cache);
        currentSize = DataCleanManager.getTotalCacheSize(GenerateProxyActivity.this);
        clearCache.setText("清理缓存 "+currentSize);
        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blackIpList!=null){
                    blackIpList.clear();
                }
                if(whiteIpList!=null){
                    whiteIpList.clear();
                }

                DataCleanManager.cleanInternalCache(GenerateProxyActivity.this);
                currentSize = DataCleanManager.getTotalCacheSize(GenerateProxyActivity.this);
                Toast.makeText(GenerateProxyActivity.this, "缓存已清除", Toast.LENGTH_SHORT).show();
                clearCache.setText("清理缓存 "+currentSize);
            }
        });
    }
    private void checkProxysByPing(){
        for (Server server : NetConfig.servers){
            service.submit(new PingTask(server));
        }
    }
    private void checkProxysBySocket(){
        for (Server server : NetConfig.servers){
            service.submit(new SocketTask(server));
        }
    }
    private void checkProxys(){
        new Thread() {
            @Override
            public void run() {
                for (Server server : NetConfig.servers) {
                    if (isInterrupted) {
                        break;
                    }
                    Message msg = new Message();
                    if (isValidIP(server.ip, server.port)) {
                        Log.i("ly", "valid ip-->" + server.ip);
                        NetConfig.validIps.add(server);

                        msg.what = UPDATE_VALIDIP_PROXY;
                        msg.obj = "valid ip-->" + server.ip + "\n";

                        //if(NetConfig.validIps.size()>1)break;
                    } else {
                        Log.i("ly", "bad ip-->" + server.ip);
                        NetConfig.badIps.add(server);

                        msg.what = UPDATE_LOG;
                        msg.obj = "bad ip-->" + server.ip + "\n";

                    }
                    handler.sendMessage(msg);
                }
                if (!isInterrupted) handler.sendEmptyMessage(CHECK_FINISH);
            }
        }.start();
    }
    private void generateManualProxys(){
        /*测试vip花刺代理*/
        for(String line:NetConfig.manualProxys){
            int index=line.indexOf(":");
            String ip=line.substring(0, index);
            String port=line.substring(index+1);
            NetConfig.servers.add(new Server(ip, port));
        }

        handler.sendEmptyMessage(GENERATE_PROXY);
    }
    private  void generateVIPProxys(){
        new Thread(){
            public void run() {
                HttpGet get=new HttpGet(NetConfig.VIPURL_GETPROXY);
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
                            if(line.contains(":")){
                                int index=line.indexOf(":");
                                String ip=line.substring(0, index);
                                String port=line.substring(index+1);
                                NetConfig.servers.add(new Server(ip, port));
                            }

                        }
                        br.close();

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
    private boolean isValidIPBySocket(final String ip,final String port){
        Boolean ret=false;
        Socket s=new Socket();
        try {
            s.setSoTimeout(TIME_OUT);
            s.connect(new InetSocketAddress(ip, Integer.parseInt(port)), TIME_OUT);
            ret=s.isConnected();
        } catch (IOException e) {
            Log.i("ly","connect error-->"+e.toString());
            e.printStackTrace();
        }finally {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }
    private boolean isValidIP(final String ip,String port){
        if(filter.isChecked()){
            if(whiteIpList.contains(ip)){
                Log.i("ly","in white list");
                return true;
            }else if(blackIpList.contains(ip)){
                Log.i("ly","in black list");
                return false;
            }else{
                /*新ip，重新检测*/
            }
        }
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
                whiteIpList.add(ip);
                Log.i("ly","add white list");
                return true;
            }else{
                blackIpList.add(ip);
                Log.i("ly", "add black list");
                return false;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            blackIpList.add(ip);
            Log.i("ly", "add black list");
            return false;
        }
    }
    private void launchMain(){
        if(NetConfig.validIps.size()==0){
            Toast.makeText(this,"未生成有效代理",Toast.LENGTH_SHORT).show();
            return;
        }
        clearAndSave();
        Intent it=new Intent(GenerateProxyActivity.this,NetConfig.isDebug?TestActivity.class:MainActivity.class);
        startActivity(it);

    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        clearAndSave();
    }
    private void clearAndSave(){
        handler.removeCallbacksAndMessages(null);
        isInterrupted =true;
        service.shutdownNow();

        ProxySetting.cancelProxy();

        ipListCache.saveBlackIpList(blackIpList);
        ipListCache.saveWhiteIpList(whiteIpList);

        finish();
        Log.i("ly", "finish");
    }
    class PingTask implements Runnable{
        Server mServer;
        PingTask(Server server){
           mServer=server;
        }
        @Override
        public void run() {
            Message msg = new Message();
            try
            {
                Process p = Runtime.getRuntime().exec(
                        "ping -c 1 -w "+TIME_OUT/1000+" " + mServer.ip);

                int status = p.waitFor();
                if (status == 0)
                {
                    Log.i("ly", "valid ip-->" + mServer.ip);
                    NetConfig.validIps.add(mServer);

                    msg.what = UPDATE_VALIDIP_PROXY;
                    msg.obj = "ping valid ip-->" + mServer.ip + "\n";

                } else
                {
                    Log.i("ly", "bad ip-->" + mServer.ip+" status:"+status);
                    NetConfig.badIps.add(mServer);

                    msg.what = UPDATE_LOG;
                    msg.obj = "ping bad ip-->" + mServer.ip + "\n";
                }


            } catch (Exception e)
            {
                Log.i("ly", "bad ip-->" + mServer.ip);
                NetConfig.badIps.add(mServer);

                msg.what = UPDATE_LOG;
                msg.obj = "exception bad ip-->" + mServer.ip + "\n";
            }finally {
                handler.sendMessage(msg);
                if(NetConfig.validIps.size()+NetConfig.badIps.size()==NetConfig.servers.size()){
                    handler.sendEmptyMessage(CHECK_FINISH);
                }
            }
        }
    }
    class SocketTask implements Runnable{
        Server mServer;
        SocketTask(Server server){
            mServer=server;
        }
        @Override
        public void run() {
            Message msg = new Message();


            if (isValidIPBySocket(mServer.ip,mServer.port))
            {
                Log.i("ly", "valid ip-->" + mServer.ip);
                NetConfig.validIps.add(mServer);

                msg.what = UPDATE_VALIDIP_PROXY;
                msg.obj = "socket valid ip-->" + mServer.ip + "\n";

            } else
            {
                Log.i("ly", "bad ip-->" + mServer.ip+" status:");
                NetConfig.badIps.add(mServer);

                msg.what = UPDATE_LOG;
                msg.obj = "socket bad ip-->" + mServer.ip + "\n";
            }
            handler.sendMessage(msg);
            if(NetConfig.validIps.size()+NetConfig.badIps.size()==NetConfig.servers.size()){
                handler.sendEmptyMessage(CHECK_FINISH);
            }

        }
    }
}
