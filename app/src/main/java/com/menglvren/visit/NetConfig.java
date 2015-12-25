package com.menglvren.visit;
import java.util.ArrayList;
import java.util.HashSet;

public class NetConfig {

	public  static boolean isDebug=true;

	public static final String URL_GETPROXY="http://www.xicidaili.com/nn/";
	public static final String VIPURL_GETPROXY ="http://xvre.daili666api.com/ip/?" +
			"tid=559484726216210&num=10&category=2&exclude_ports=8088&filter=on";
	public static String[] manualProxys=new String[]{
			"111.203.244.4:8081",
			"117.136.234.9:81",
			"112.53.81.186:80",
			"119.188.115.26:8080",
			"124.160.194.71:80",
			"180.166.112.47:8888",
			"195.186.81.94:80",
			"195.186.81.92:80",
			"111.202.71.60:8080"
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

class Server{
	public String ip;
	public String port;
	public Server(String i,String p){
		ip=i;
		port=p;
	}
}
