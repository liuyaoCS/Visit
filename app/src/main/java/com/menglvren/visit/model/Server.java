package com.menglvren.visit.model;


import java.io.Serializable;

public class Server implements Serializable {
	public String ip;
	public String port;
	public Server(String i,String p){
		ip=i;
		port=p;
	}

	@Override
	public int hashCode() {
		return (ip+port).hashCode();
	}

	@Override
	public boolean equals(Object o) {

		boolean flag1=((Server)o).ip.equals(ip);
		boolean flag2=((Server)o).port.equals(port);
		return flag1 && flag2;
	}
}
