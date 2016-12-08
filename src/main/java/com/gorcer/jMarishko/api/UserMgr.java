package com.gorcer.jMarishko.api;

import java.util.Vector;

public class UserMgr extends Vector<MUser>{

	static UserMgr instance=null;
	
	UserMgr()
	{
		super();
	}
	
	static UserMgr get_Instance()
	{		
		if (instance == null)
		{
			instance =  new UserMgr();
		}
		
		return instance;
		
	}
	
	 
	
	static MUser getUserByName(String UserName)
	{
				
		UserMgr um = UserMgr.get_Instance();
		
		
		
		for (int i=0;i<um.size();i++)
		{
		  if (((MUser)um.get(i)).Login.equals(UserName))
			  return((MUser)um.get(i));
		}
			
		
		MUser user = UserMgr.CreateUser(UserName);
		um.add(user);
		user.isAllow = (Config.open2all || isAllow2useBot(UserName));
		
		return(user);
	}
	
	private static MUser CreateUser(String UserName)
	{
	//JabberMgr.addUser2Roster(UserName);
	return(new MUser(UserName));
	}

	private static boolean isAllow2useBot(String userName) {
		
		
		return DBManager.isAllow2useBot(userName);
	}

	public static void linkUsers(Vector<String> vector) {
		// TODO Связываем двух пользователей
		
		
		MUser u1 = null;
		MUser u2 = null;
		
		for (String user: vector)
		{
			if (user.equals(Config.botname)) continue;
			if (u1==null)
			u1=getUserByName(user);
			else
			u2=getUserByName(user);			
		}
		
		u1.LinkedUser = u2;
		u2.LinkedUser = u1;
		
		
	}
}
