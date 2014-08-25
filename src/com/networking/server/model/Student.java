package com.networking.server.model;

import java.util.ArrayList;

public class Student {
	String IP;
	int id;
	ArrayList<String> files=new ArrayList<String>();
	public Student(String ip,int id) {
		this.IP=ip;
		this.id=id;
	}
	public void setIP(String ip)
	{
		this.IP=ip;
	}
	public  String getIp()
	{
		return this.IP;
	}
	public int getID()
	{
		return this.id;
	}
	public void addFile(String file)
	{
		files.add(file);
	}
	public int  getFileSize()
	{
		return files.size();
	}
}
