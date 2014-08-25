package com.networking.server;

import java.awt.Component;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

import com.networking.server.model.Student;

public class ServerData {
	ArrayList<Integer> studentList = new ArrayList<Integer>();
	HashMap<Integer,Student> mapIdStudent=new HashMap<Integer, Student>();
	HashMap<String,Integer> mapIdIp=new HashMap<String, Integer>();

	Component parent;
	ServerData(Component parComponent)
	{
		parent=parComponent;
	}
	public void initStudentList(String strIds) {
		studentList.clear();
		String[] ranges = strIds.split(",");
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i].contains("-")) {
				String[] temp=ranges[i].split("-");
				int lower=Integer.valueOf(temp[0]);
				int uppper=Integer.valueOf(temp[1]);
				if(lower>uppper)
					JOptionPane.showMessageDialog(parent,"Invalid student id format");
				else
				{
					for(int j=lower;j<=uppper;j++)
					{
						studentList.add(j);
					}
				}
			}
			else
			{
				studentList.add(Integer.valueOf(ranges[i]));
			}
		}
		System.out.println(""+studentList.size());

	}

	public boolean isStudentIdAvailable(int id) {
		if(studentList.size()==0)
			return true;
		return studentList.contains(id);
	}
	public int getStudentListsize()
	{
		return studentList.size();
		
	}
	public boolean checkDuplicateIPAddress(int idX,String ipX)
	{
		if(mapIdIp.containsKey(ipX))
		{
			int id=mapIdIp.get(ipX);
			if(id==idX)
				return false;
		}
		
			return true;
	}
	public boolean checkDuplicateID(int idX,String ipX)
	{
		if(mapIdStudent.containsKey(idX))
		{
			String ip=mapIdStudent.get(idX).getIp();
			if(ip.equals(ipX))
				return true;
			else
				return false;
		}
		return true;
		
	}
	public boolean isStudentAlreadyConnected(int id)
	{
		if(mapIdStudent.containsKey(id))
			return true;
		else
			return false;
	}
	public Student getStudentById(int id)
	{
		return mapIdStudent.get(id);
	}
	public void addStudentInMap(Student stdnt)
	{
		mapIdIp.put(stdnt.getIp(), stdnt.getID());
		mapIdStudent.put(stdnt.getID(),stdnt);
	}
}
