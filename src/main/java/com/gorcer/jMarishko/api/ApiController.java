package com.gorcer.jMarishko.api;

import org.json.simple.JSONObject;

import spark.Spark;

public class ApiController {
	public static void main(String[] args) {
        

		Spark.exception(Exception.class, (exception, request, response) -> {
		    exception.printStackTrace();
		});
		
		Spark.before((request, response) -> {
			
			if (DBManager.connection == null)
				DBManager.Connect();
			
			/*
			 *  String version =  request.params(":version");
			if (version != "0.1") {
				Spark.halt(401, "Version error " +version);
			}	*/	    
		});
		
		Spark.get("/:version/getAnswer/:phrase/:userName/", (request, response) -> {
											
			
			String phrase =  request.params(":phrase");
			String UserName = request.params(":userName");
			
			MaMessage msg = new MaMessage(phrase);
        	msg = DBManager.loadMessageAttr(msg);
        	
        	MUser Author = UserMgr.getUserByName(UserName);
        	msg.owner = Author;
        	msg = WordMgr.AppendIfItNeed(Author, msg);
        	
        	WordMgr.processUserMessage(Author, msg);
			
    		//---Answer
        	MaMessage Answer = WordMgr.getAnswer(Author,msg);
    		
        	
        	JSONObject resultJSON = new JSONObject();
        	
        	if (Answer == null) {
        		resultJSON.put("answer", null);
        	} else {
        		resultJSON.put("answer", Answer.body);
        	}
    		
        	return resultJSON.toString();
		});
		
		Spark.get("/:version/getHello/:userName/", (request, response) -> {
			
			MUser u = UserMgr.getUserByName(request.params(":userName"));
			MaMessage msg = WordMgr.getHello(u);
			
			JSONObject resultJSON = new JSONObject();
			
			if (msg == null) {
				resultJSON.put("hello", null);
			} else {
				resultJSON.put("hello", msg.body);
			}
			
			return resultJSON.toString();
			
		});
		
		Spark.get("/:version/getSomething/:userName/", (request, response) -> {
			
			MUser u = UserMgr.getUserByName(request.params(":userName"));
			MaMessage msg = WordMgr.getSomething(u);
			
			JSONObject resultJSON = new JSONObject();
			
			if (msg == null) {
				resultJSON.put("something", null);
			} else {
				resultJSON.put("something", msg.body);
			}
			
			return resultJSON.toString();
			
		});
    }
}
