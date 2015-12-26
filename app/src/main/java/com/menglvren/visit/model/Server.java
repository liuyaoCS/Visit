package com.menglvren.visit.model;

import java.io.Serializable;

public class Server implements Serializable {
	public String ip;
	public String port;
	public Server(String i,String p){
		ip=i;
		port=p;
	}
}
