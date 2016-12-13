package com.gorcer.jMarishko.api;

import org.json.simple.JSONObject;

import spark.Spark;

public class ApiController {
	public static void main(String[] args) {
        
		
		Spark.exception(Exception.class, (exception, request, response) -> {
		    exception.printStackTrace();
		});
		
		Spark.before((request, response) -> {
			
			
			if (DBManager.connection == null || DBManager.connection.isClosed())
				DBManager.Connect();
			
			/*
			 *  String version =  request.params(":version");
			if (version != "0.1") {
				Spark.halt(401, "Version error " +version);
			}	*/	    
		});
		
		Spark.after((request, response) -> {
			DBManager.Disconnect();
		});
		

		Spark.post("/:version/getAnswer/", (request, response) -> {
											
			String userName = request.queryParams("userName");
			if (userName == null) {
				Spark.halt(401, "Need userName param");
			}
			
			String phrase = request.queryParams("phrase");
			if (phrase == null) {
				Spark.halt(401, "Need phrase param");
			}
			
			MaMessage msg = new MaMessage(phrase);
        	msg = DBManager.loadMessageAttr(msg);
        	
        	MUser Author = UserMgr.getUserByName(userName);
        	msg.owner = Author;
        	//msg = WordMgr.AppendIfItNeed(Author, msg);
        	
        	WordMgr.processUserMessage(Author, msg);
			
    		//---Answer
        	MaMessage Answer = WordMgr.getAnswer(Author,msg);
    		
        	
        	JSONObject resultJSON = new JSONObject();
        	
        	if (Answer == null) {
        		resultJSON.put("msg", null);
        	} else {
        		resultJSON.put("msg", Answer.body);
        	}
    		
        	return resultJSON.toString();
		});
		
		Spark.post("/:version/getHello/", (request, response) -> {
			
			String userName = request.queryParams("userName");
			if (userName == null) {
				Spark.halt(401, "Need userName param");
			}
			
			
			MUser u = UserMgr.getUserByName(userName);
			MaMessage msg = WordMgr.getHello(u);
			
			JSONObject resultJSON = new JSONObject();
			
			if (msg == null) {
				resultJSON.put("msg", null);
			} else {
				resultJSON.put("msg", msg.body);
			}
			
			return resultJSON.toString();
			
		});
		
		Spark.post("/:version/getSomething/", (request, response) -> {
			
			String userName = request.queryParams("userName");
			if (userName == null) {
				Spark.halt(401, "Need userName param");
			}
			
			MUser u = UserMgr.getUserByName(userName);
			MaMessage msg = WordMgr.getSomething(u);
			
			JSONObject resultJSON = new JSONObject();
			
			if (msg == null) {
				resultJSON.put("msg", null);
			} else {
				resultJSON.put("msg", msg.body);
			}
			
			return resultJSON.toString();
			
		});
		
		Spark.post("/system/upgrade/", (request, response) -> {
			
			DBManager.Connect();	
			
			int cnt = DBManager.RecalcAllMask();
			
			return cnt + " updated";
		});
		
		Spark.post("/system/teach/", (request, response) -> {
			String phrase = request.queryParams("phrase");
			String userName = request.queryParams("userName");
			
			if (phrase == null) {
				return "phrase is required";
			}
			
			if (userName == null) {
				return "userName is required";
			}
			
			MaMessage msg = new MaMessage(phrase);
			MUser u = UserMgr.getUserByName(userName);
			WordMgr.teach(u, msg);
			return "ok";			
			
		});
    }
}
