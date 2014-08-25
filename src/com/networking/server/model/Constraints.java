package com.networking.server.model;

public class Constraints {
	
	private String directory="";
	private String  filetypes="";
	private String  fileNames="";
	private String  students;
	//private String path;
	private double sizeMB=2;
	private int fileNum=10;
	private boolean CanUploadFolder=false;
	
	public void setDirectory(String direc)
	{
		this.directory=direc;
	}
	public void setFileTypes(String files)
	{
		this.filetypes=files;
	}
	public void setFileNames(String files)
	{
		this.fileNames=files;
	}

	public void setStudents(String students)
	{
		this.students=students;
	}
	public void setSizeMB(double size)
	{
		this.sizeMB=size;
	}
	public void setFileNum(int num)
	{
		this.fileNum=num;
	}
	public void setCanUploadFolder(boolean canFolder)
	{
		this.CanUploadFolder=canFolder;
	}
	public String getDirectory()
	{
		return this.directory;
	}
	public String getFileTypes()
	{
		return this.filetypes;
	}
	public String getFileNames()
	{
		return this.fileNames;
	}
	public String getStudents()
	{
		return this.students;
	}
	public double getSizeMB()
	{
		return this.sizeMB;
	}
	public int  getFileNum()
	{
		return this.fileNum;
	}
	public boolean getCanUploadFolder()
	{
		return this.CanUploadFolder;
	}
	

}
