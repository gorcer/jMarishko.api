package com.gorcer.jMarishko.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.stream.Stream;

public class DBManager {

	static Connection connection=null;
	static String connString="";
	
	
	static private void LoadDriver()
	{
		try {   
		    
		    Class.forName("com.mysql.jdbc.Driver")
		        .newInstance();
		   // System.out.println("MySQL Driver loaded");
		    } 
		  catch (Exception e) {
		    
		    //System.out.println("Load driver Error");
		    e.printStackTrace();
		    }
	}    

	
	static void loadConnectionConfig() {
		 if (connString == "") {
		 	try {
		 		String appPath = new File(".").getCanonicalPath();
		 		Path path = Paths.get(appPath + "/config.cfg");
	        
	        	Stream<String> lines = Files.lines(path);
	        	for (Iterator<String> i = lines.iterator(); i.hasNext();) {
	        		connString=connString +  i.next();
	        	}
	        	lines.close();
	        } catch (IOException ex) {
	        	 System.out.println("Cant read from config: " + ex.getMessage());
	        }
		 }
		 
	}
	
	static void Connect()
	{
		LoadDriver();
		loadConnectionConfig();
		
		try {
			connection = DriverManager.getConnection(connString);
			
			Statement stmt = connection.createStatement();
			 
			 stmt.executeUpdate("SET names 'utf8'");
			  //System.out.println("DB Connect Ok!");
			 
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	
	
		
	}
	
	static public ResultSet getSQLResult(String sql)
	{
		
		ResultSet rs = null;

		
		
		try {
			Statement stmt = connection.createStatement();
		  	    if (stmt.execute(sql)) {
		        rs = stmt.getResultSet();
		    }

		}
		
		catch (SQLException ex){
			
			System.out.println(sql);
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(rs);
		
		
	}
	
	static public MaMessage getLinkedWord(MaMessage word)
	{
		
	String sql = "select tt.soder, tt.mask" +
					" from" +
					" (select t2.soder, t2.mask" +
					" from talink tl" +
					" inner join talk t2 on t2.id=tl.id_2" +
					" inner join  talk t1 on t1.id=tl.id_1" +
					" where" +
					" t1.mask like '"+word.mask+"'" +
					" group by t2.soder" +
					" order by tl.ic desc" +
					" limit " + MaConfig.AnswerCloudSize + ") as tt" +
					" order by rand()"
					;
	
	//System.out.println(sql);
	ResultSet rs = getSQLResult(sql);

	
	try
	{
	if (rs.next())
		return(new MaMessage( rs.getString(1), rs.getString(2)) );
	else
		return(null);	
	}
	
	catch (SQLException ex){
	    // handle any errors
		System.out.println(sql);
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}
	
	
		
	return(null);
	}


	static public MaMessage getAnswer(MaMessage word, MUser user, boolean useDialogLike)
	{
		
	// в 1 из 3 случаев выбираем по IC
	String sort = (Math.round(Math.random()/2)==1?"tl.ic desc":"LENGTH(t2.soder) desc");	
	
	String DialogLikeSQL ="";
	
	// Если этот разговор похож на другой разговор, то попробовать искать там фразы.
	if (user.dialogLike.id!=0 && useDialogLike==true)
	DialogLikeSQL =" and t2.id in ("+getDialogFilterSQL(user.dialogLike.id, 2)+")";
	
	String sql = "select tt.soder, tt.mask" +
					" from" +
					" (select t2.soder, t2.mask" +
					" from talink tl" +
					" inner join talk t2 on t2.id=tl.id_2" +
					" inner join  talk t1 on t1.id=tl.id_1" +
					" " +
					" where" +
					" t1.mask like '"+word.mask+"'" +
					" and t2.id<>" + word.id +
					" and t2.id not in " +
					" (" +
					  getDialogFilterSQL(user.dialog.id, 1) +					
					" )" +
					DialogLikeSQL  +
					" group by t2.soder" +
					" order by " + sort +
					" limit " + MaConfig.AnswerCloudSize + ") as tt" +
					" order by rand()"
					;
	
	//System.out.println(sql);
	ResultSet rs = getSQLResult(sql);

	
	try
	{
	if (rs.next())
		return(new MaMessage( rs.getString(1), rs.getString(2)) ); // Если фраза найдена - возаращаем её.
	else
	{
		if (user.dialogLike.id!=0 && useDialogLike==true)
			return(getAnswer(word, user, false));  // Иначе пробуем найти без привязки к похожему диалогу но такое которое ещё не повторялось.
		else
			return(getLinkedWord(word)); // Или, если привязки к диалогу не было, просто связанное слово.
	}
			
	}
	
	catch (SQLException ex){
	    // handle any errors
		System.out.println(sql);
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}
	
	
		
	return(getLinkedWord(word));
	}
	
	
	public static void processMessageByMask(MaMessage msg, MUser user) {

		msg.id=getMessageIDByMask(msg.mask);
			
			if (msg.id!=0)
			{
				//дописать update respect
			}
			else
			{
				msg.id=AddTalk(msg);
			}
	}



	private static int AddTalk(MaMessage msg) {
		
		String sql="insert into talk(soder, mask, md) values ('"+msg.getBody()+"', '"+msg.mask+"', md5('"+msg.mask+"'))";		
		int id=0;  
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);	
			id=getLastInsertId();
	    }
	
		catch (SQLException ex){
		System.out.println(sql);
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
		}
	
	return(id);
	}



	public static int linkMessages(MaMessage msg1,MaMessage msg2) {

		
			if (msg1!=null)
				if (msg1.id==0)
					msg1.id = getMessageIDByMask(msg1.mask);
			
			if (msg2!=null)
				if (msg2.id==0)
						msg2.id = getMessageIDByMask(msg2.mask);
			
		
			int link_id = getLinkIDbyMessages(msg1, msg2);
			if (link_id!=0)
			{
				updateLinkIC(link_id); //Усиливаем связь
				
				if (msg1!=null)
				decLinkIC2OverTalk(msg1, link_id); //Ослабеваем связь у остальных связей
			}
			else
			{
				link_id=addLink(msg1, msg2);
			}
			
			return(link_id);
	}



	private static void decLinkIC2OverTalk(MaMessage msg1, int link_id) {
		/*
		 * При усилении одних связей должны ослабевать другие. Эффект памяти.
		 * MOD: Перенести в процедуру ночной самоорганизации.
		 * */
	String sql = null;
		
		try {
			Statement stmt = connection.createStatement();
	  	    sql="update talink set ic=ic-1 where id_1 = "+msg1.id+" and id<>"+link_id;
			stmt.executeUpdate(sql);	      
	    }
	
	catch (SQLException ex){
		System.out.println(sql);
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}
	
		
	}



	private static int addLink(MaMessage msg1,MaMessage msg2) {
		String sql = null;
		
		
		int id=0;
		try {
			Statement stmt = connection.createStatement();
	  	    sql="insert into talink(id_1, id_2) values ("+(msg1==null?"0":msg1.id)+", "+(msg2==null?"0":msg2.id)+")";
			stmt.executeUpdate(sql);	
			id=getLastInsertId();
	    }

	
	
	catch (SQLException ex){
		System.out.println(sql);
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}
	
		return(id);
	}



	private static int getLastInsertId() {
		String sql = null;
		
		try
		{
			sql = "SELECT LAST_INSERT_ID()";
			
			ResultSet rs = getSQLResult(sql);
			
			if (rs.next())
			{
				return(rs.getInt(1));
			}
			 
		}
		
		catch (SQLException ex){
		    // handle any errors
			System.out.println(sql);
		    System.out.println("SQLException : " + ex.getMessage());		    
		}
		
		return(0);
		

		
	}



	private static void updateLinkIC(int linkId) {
		String sql = null;
		
		try {
			Statement stmt = connection.createStatement();
	  	    sql="update talink set ic=ic+1 where id = "+linkId+"";
			stmt.executeUpdate(sql);	      
	    }

	
	
	catch (SQLException ex){
		System.out.println(sql);
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}
	
		
	}



	private static int getLinkIDbyMessages(MaMessage msg1, MaMessage msg2) {
		String sql = null;
		String id1;
		String id2;
		
		if (msg1==null)
			id1 = "=0";
		else
			id1 = "='"+msg1.id+"'";
		
		if (msg2==null)
			id2 = "=0";
		else
			id2 = "='"+msg2.id+"'";
		
		try
		{
			sql = "select id from talink where id_1"+id1+" and id_2"+id2+"";
			
			ResultSet rs = getSQLResult(sql);
			
			if (rs.next())
			{
				return(rs.getInt(1));
			}
			 
			
			
			
			
		}
		
		catch (SQLException ex){
		    // handle any errors
			System.out.println(sql);
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(0);
		
	}



	private static int getMessageIDByBody(String msg) {
		
		String sql = "select id from talk where soder='"+msg+"'";
		try
		{			
			ResultSet rs = getSQLResult(sql);
			if (rs.next())
			{
				return(rs.getInt(1));
			}
			else
				return ( AddTalk( new MaMessage(msg) ) );
			
		}
		
		catch (SQLException ex){
		    // handle any errors
			System.out.println(sql);
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(0);
	}


	public static int getMessageIDByMask(String msk) {
		
		String sql = "select id from talk where mask='"+msk+"'";
		try
		{			
			ResultSet rs = getSQLResult(sql);
			if (rs.next())
			{
				return(rs.getInt(1));
			}
			
		}
		
		catch (SQLException ex){
		    // handle any errors
			System.out.println(sql);
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(0);
	}


	

	public static void processDialog(MUser user, int link_id) {
		
			
		if (user.dialog.id==0)
			user.dialog.id = AddDialog(user);
		
		AddDialogDet(user.dialog.id, link_id);
		
		user.dialogLike.id = DBManager.getDialogLike(user.dialog.id);
	}



	private static int getDialogLike(int id) {

		String sql = "SELECT d2.head_id" +
					 " FROM" +
					 " dialogdet d" +
					 " inner join dialogdet d2 on d.talink_id=d2.talink_id" +
					 " where d.head_id="+id+" and d2.head_id<>" + id +
					 " group by d2.head_id" +
					 " order by count(d2.talink_id) desc";
		
		try
		{			
			
			ResultSet rs = getSQLResult(sql);
			
			if (rs.next())
			{
				return(rs.getInt(1));
			}
			
		}
		
		catch (SQLException ex){
		    // handle any errors
			System.out.println(sql);
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(0);
		
	}



	private static int AddDialogDet(int head_id, int linkId) {
		

		String sql="insert into DialogDet(head_id, talink_id) values ("+head_id+", "+linkId+")";
		int id=0;
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			id = getLastInsertId();
	    	}
	
		catch (SQLException ex){
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());	    
			}
	
		return(id);	
	}



	private static int AddDialog(MUser user) {
		

		String sql="insert into DialogHead(user_id, create_dtm) values ('"+user.id+"', now())";
		int id=0;
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			id = getLastInsertId();
	    	}
	
		catch (SQLException ex){
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());	    
			}
	
		return(id);
		
	}



	public static int processUser(String userName) {

		int user_id = getUserByName(userName);
		
		if (user_id==0)
			user_id=AddUser(userName);
			
			
		return user_id;
	}



	private static int AddUser(String userName) {

		String sql="insert into users(name, create_dtm) values ('"+userName+"', now())";
		int id=0;
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			id = getLastInsertId();
	    	}
	
		catch (SQLException ex){
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());	    
			}
	
		return(id);
	}



	private static int getUserByName(String userName) {
		String sql = "select id from users where name='"+userName+"'";
		
		try
		{			
			
			ResultSet rs = getSQLResult(sql);
			
			if (rs.next())
			{
				return(rs.getInt(1));
			}
			
		}
		
		catch (SQLException ex){
		    // handle any errors
			System.out.println(sql);
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(0);
	}



	public static MaMessage getHello(MUser u) {
		String sql = " select tt.soder, tt.mask" +
					 " from (" +
					 " select t.soder, t.mask " +
					 " from talk_attr ta" +
					 " inner join talk t on t.id=ta.talk_id" +
					 " where ta.isHello>0" +
					 " order by ta.isHello desc, t.ic desc" +
					 " Limit " + MaConfig.HelloCloudSize +
					 " ) as tt" +
					 " order by rand()" +
					 " limit 1";
		
		try
		{			
			
			ResultSet rs = getSQLResult(sql);
			
			if (rs.next())
			{
				return(new MaMessage(rs.getString(1), rs.getString(2)));
			}
			
		}
		
		catch (SQLException ex){
		    // handle any errors
			System.out.println(sql);
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(null);

		
	}

	public static String getDialogFilterSQL(int dialog_id, int fld)
	{
		String sql = "  select tl.id_"+fld + //Убираем варианты уже озвученные в диалоге
		"  from talink tl" +
		"  inner join dialogdet dd on dd.talink_id=tl.id" +
		"  inner join dialoghead dh on dh.id=dd.head_id" +
		"  where dh.id = " +dialog_id +
		" ";
		return(sql);
	}

	public static MaMessage getSomething(MUser u, boolean useAll) {
		
		String addSql="";
		
		if (!useAll)
			addSql = "where t.id not in (" +
					 getDialogFilterSQL(u.dialog.id, 1) +
					 " )";
			
		
		String sql = " select tt.soder, tt.mask" +
					 " from (" +
					 " select t.soder, t.mask " +
					 " from talk t" +
					 " inner join talk_attr ta on ta.talk_id=t.id and ta.isSomething>0" +
					 " " + addSql + 					 
					 " order by ta.isSomething desc, t.ic desc" +
					 " Limit " + MaConfig.SomethingCloudSize +
					 " ) as tt" +
					 " order by rand()" +
					 " limit 1";

			try
			{			
			
			ResultSet rs = getSQLResult(sql);
			
			if (rs.next())
			{
				return(new MaMessage(rs.getString(1), rs.getString(2)));
			}
			
			}
			
			catch (SQLException ex){
			// handle any errors
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			}
			
			return(null);
	}



	public static void addHello(MaMessage msg) {

		String sql="";
		if (msg.attrID==0)		
		sql=" insert into talk_attr(talk_id, isHello) values ("+msg.id+", 1)";
		else
		sql=" update talk_attr set isHello=isHello+1 where id="+msg.attrID;
		
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			/* id = getLastInsertId(); */
	    	}
	
		catch (SQLException ex){
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());	    
			}
					
	}



	public static MaMessage loadMessageAttr(MaMessage msg) {
		
		
		if (msg.id==0)
			msg.id=getMessageIDByMask(msg.mask);
			
		if (msg.id==0)
			return msg;
			
		String sql = " select id, isHello, isSomething from talk_attr where talk_id="+msg.id;

			try
			{			
			
			ResultSet rs = getSQLResult(sql);
			
			if (rs.next())
			{
				msg.attrID = rs.getInt(1); 
				msg.isHello=rs.getInt(2);
				msg.isSomething=rs.getInt(3);
			}
			
			}
			
			catch (SQLException ex){
			// handle any errors
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			}
			
			return(msg);
		
	}



	public static boolean isAllow2useBot(String userName) {
		String sql = " select isAllow from users where name='"+userName+"'";

		try
		{			
		
		ResultSet rs = getSQLResult(sql);
		
		if (rs.next())
		{
			return(((rs.getInt(1)==1)?true:false));
		}
		else return(false);
		
		}
		
		catch (SQLException ex){
		// handle any errors
		System.out.println(sql);
		System.out.println("SQLException: " + ex.getMessage());
		System.out.println("SQLState: " + ex.getSQLState());
		System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return(false);
	}



	public static void addSomething(MaMessage msg) {
		
		String sql="";
		if (msg.attrID==0)		
		sql=" insert into talk_attr(talk_id, isSomething) values ("+msg.id+", 1)";
		else
		sql=" update talk_attr set isSomething=isSomething+1 where id="+msg.attrID;
		
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			/* id = getLastInsertId(); */
	    	}
	
		catch (SQLException ex){
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());	    
			}
	}



	public static void allowUserToChat(MUser u) {

		if (u.id==0) return;
		
		String sql="update users set isAllow = 1 where id=" + u.id;
		
		
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			}
	
		catch (SQLException ex){
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());	    
			}
	}

	public static int RecalcAllMask()
	{
		int cnt=0;
		String rmask="";
		String dbmask="";
		
		String sql = " select t.soder, t.mask, t.id " +
		 " from talk t" +	  					 
		 " order by t.id";

			try
			{			
			
			ResultSet rs = getSQLResult(sql);
			
			Statement stmt2 = connection.createStatement();

			while (rs.next())
			{
				rmask = WordMgr.getMask(rs.getString(1));
				dbmask = rs.getString(2);
				
				rmask = rmask.replace("'", "");
				rmask = rmask.replace("\\", "");
				
				if (!rmask.equals(dbmask)) 
					{
					cnt++;
				
					
					if (rmask.length()>0) {
						sql="update talk set mask = '"+rmask+"' where id=" + rs.getInt(3);
						stmt2.executeUpdate(sql);
						}
					}
			}
			
			
			}
			
			catch (SQLException ex){
			// handle any errors
			System.out.println(sql);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			}
			
			System.out.println("Total " + cnt + " updates");
			return(cnt);
				}



	public static void Disconnect() {
		// TODO Auto-generated method stub
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public static void saveTalkToFile(String toFile) throws Exception {
		// TODO Auto-generated method stub
		String appPath = new File(".").getCanonicalPath();
		
		
		String sql = " select t.soder " +
		 " from talk t" +	  					 
		 " order by t.id";
		
		ResultSet rs = getSQLResult(sql);		
		Statement stmt = connection.createStatement();
		
		// Create file
        FileWriter fstream = new FileWriter(appPath + toFile, false);
        BufferedWriter out = new BufferedWriter(fstream);
        
        System.out.println("Выгружаем базу фраз...");
		while (rs.next()) {			
	        out.write(rs.getString(1));
	        out.newLine();
	        
		}
		System.out.println("Готово...");
		//Close the output stream
        out.close();
			
		
	}

	
}
