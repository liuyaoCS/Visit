package com.menglvren.visit;
import com.menglvren.visit.model.Server;

import java.util.ArrayList;

public class NetConfig {

	public  static boolean isDebug=false;

	public static final String URL_GETPROXY="http://www.xicidaili.com/nn/";
	public static final String VIPURL_GETPROXY ="http://xvre.daili666api.com/ip/?tid=559484726216210&num=20&filter=on";
	public static String[] manualProxys=new String[]{
			"14.130.79.252:9000",
			"14.218.107.220:9797",
			"14.218.212.214:9797",
			"27.221.31.66:8080",
			"27.221.31.78:8080",
			"58.134.102.3:12696",
			"58.220.10.7:80",
			"59.39.88.190:8080",
			"60.29.248.142:8080",
			"61.142.131.180:80",
			"61.142.131.183:80",
			"61.174.10.22:8080",
			"61.179.110.8:8081",
			"101.66.253.22:8080",
			"101.200.202.168:80",
			"103.37.145.17:9999",
			"111.203.244.4:8081",
			"112.53.81.186:80",
			"112.74.132.166:80",
			"113.200.78.27:9999",
			"114.215.187.135:8888",
			"115.231.65.16:8080",
			"115.231.65.15:8080",
			"116.211.19.201:8008",
			"119.135.185.98:9999",
			"183.185.1.78:9797",
			"118.253.82.235:9999",
			"119.130.29.43:9797",
			"119.188.94.145:80",
			"119.188.115.23:80",
			"119.188.115.26:8080",
			"119.147.161.55:3128",
			"120.24.221.241:8888",
			"121.14.138.56:81",
			"121.15.230.126:9797",
			"121.193.143.249:80",
			"122.70.130.41:80",
			"123.7.115.141:9797",
			"124.160.194.71:80",
			"124.251.62.246:80",
			"124.89.33.108:9999",
			"163.125.66.211:9999",
			"175.43.123.1:55336",
			"175.13.20.199:9797",
			"180.97.29.57:80",
			"180.166.112.47:8888",
			"183.62.58.250:9797",
			"210.101.131.231:8088",
			"211.144.72.154:8080",
			"211.144.81.69:18000",
			"211.144.81.68:18001",
			"211.144.81.68:18000",
			"220.175.104.7:9999",
			"222.34.3.10:8080",
			"222.34.3.11:8080",
			"222.34.3.12:8080",
			"222.73.173.169:808",
			"222.176.112.10:80"
	};
	public static String home = "http://i.youku.com/u/UMzE0NDI3NTg3Ng==";
	public static final String agent="Mozilla/5.0 (Windows NT 6.1; WOW64) " +
			"AppleWebKit/535.11 (KHTML, like Gecko) " +
			"Chrome/17.0.963.84 Safari/535.11 LBBROWSER";

	public static ArrayList<Server> servers=new ArrayList<Server>();
	public static int validIpIndex =0;
	public static ArrayList<Server> badIps=new ArrayList<Server>();
	public static ArrayList<Server> validIps=new ArrayList<Server>();

}

