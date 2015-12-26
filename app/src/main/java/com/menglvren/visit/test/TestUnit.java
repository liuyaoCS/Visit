package com.menglvren.visit.test;

import android.content.Context;
import android.util.Log;

import com.menglvren.visit.NetConfig;
import com.menglvren.visit.model.Server;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ly on 2015/12/26.
 */
public class TestUnit {
    public static final String URL="http://v.youku.com/v_show/id_XMTQyNDM3MjczMg==.html?from=y1.7-1.2&x";
    public static void showHTMLPage(Context context){
        HttpGet get=new HttpGet(URL);
        get.setHeader("User-Agent", NetConfig.agent);
        get.setHeader("Cache-Control", "no-cache");

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 1000); //设置连接超时
        HttpConnectionParams.setSoTimeout(params, 1000); //设置请求超时
        get.setParams(params);

        HttpClient hClient = null;
        try {
            hClient=new DefaultHttpClient();
            HttpResponse hResponse=hClient.execute(get);
            if(hResponse.getStatusLine().getStatusCode()==200){
                action(context,hResponse);
            }else{
                Log.i("ly","fetch err->"+hResponse.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("ly","fetch err->"+e.toString());
        }
    }
    public static void action(Context context,HttpResponse hResponse) throws  Exception{

        FileWriter fw=null;
        String file=context.getExternalCacheDir().getPath() + File.separator
                + "video-html.txt";
        fw=new FileWriter(file,true);

        PrintWriter pw=new PrintWriter(fw);

        BufferedReader br = new BufferedReader(new InputStreamReader(hResponse.getEntity().getContent(),"utf-8"));
        StringBuilder sbBuilder=new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null){
            sbBuilder.append(line + "\n");
        }
        pw.write(sbBuilder.toString());
        pw.flush();
        Log.i("ly", "已经保存到" + file);
        br.close();
        pw.close();
    }
    public static void testContain(){
        ArrayList<Server> sets=new ArrayList<>();
        sets.add(new Server("127.0.0.1", "8008"));
        Server test=new Server("127.0.0.1","8008");
        Log.i("ly","contains? "+sets.contains(test));

    }
}
