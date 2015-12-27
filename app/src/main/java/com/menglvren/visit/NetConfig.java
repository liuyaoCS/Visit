package com.menglvren.visit;
import com.menglvren.visit.model.Server;

import java.util.ArrayList;

public class NetConfig {

	public  static boolean isDebug=false;

	public static final String URL_GETPROXY="http://www.xicidaili.com/nn/";
	public static final String VIPURL_GETPROXY ="http://xvre.daili666api.com/ip/?tid=559484726216210&num=10&filter=on";
	public static String[] manualProxys=new String[]{
			"111.203.244.4:8081",
			"117.136.234.9:81",
			"112.53.81.186:80",
			"119.188.115.26:8080",
			"124.160.194.71:80",
			"180.166.112.47:8888",
			"195.186.81.94:80",
			"195.186.81.92:80",
			"114.255.193.240:8080",
			"119.188.94.145:80",
			"182.88.204.58:8123",
			"116.211.19.201:8008",
			"110.73.34.127:8123",
			"115.231.102.251:3128",
			"119.147.161.55:3128",
			"115.231.65.16:8080",
			"115.231.65.15:8080",
			"182.18.19.219:3128",
			"101.231.159.185:3128",
			"27.221.31.78:8080",
			"106.120.76.149:3128",
			"118.244.239.2:3128",
			"123.126.108.190:3128",
			"61.179.110.8:8081",
			"202.106.16.36:3128",
			"58.60.193.249:9999",
			"211.144.81.69:18000",
			"211.144.81.68:18001",
			"101.201.196.64:3128",
			"101.200.202.168:80",
			"101.254.140.172:80",
			"123.56.226.126:3128",
			"114.215.187.135:8888",
			"182.92.154.63:3128"
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

