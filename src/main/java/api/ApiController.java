package api;

import spark.Spark;

public class ApiController {
	public static void main(String[] args) {
        

		Spark.exception(Exception.class, (exception, request, response) -> {
		    exception.printStackTrace();
		});
		
		Spark.get("/say/:phrase/:userName", (request, response) -> {
			
			if (DBManager.connection == null)
				DBManager.Connect();
			
			String phrase =  request.params(":phrase");
			String UserName = request.params(":userName");
			
			MaMessage msg = new MaMessage(phrase);
        	msg = DBManager.loadMessageAttr(msg);
        	
        	MUser Author = UserMgr.getUserByName(UserName);
        	msg.owner = Author;
        	
        	WordMgr.processUserMessage(Author, msg);
			
    		//---Answer
        	MaMessage Answer = WordMgr.getAnswer(Author,msg);
    		
        	if (Answer == null) {
        		return "There is no answer on your question";
        	}
    		
			return "Your question is: " + request.params(":phrase") + "<br/>"+
					"Your answer is " + Answer.body;
		});
    }
}
