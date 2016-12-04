package api;




public class MaMessage {

	public char type; // u < b
	public int id=0;
	public String mask="";
	public String body="";
	public int isHello=0;
	public int isSomething=0;
	public long saytime=0; 
	public int attrID=0;
	public MUser owner=null;
	
	
	public MaMessage prev, next = null;
	
	
	
	public void init(String msg)
	{
		init(msg, "");
	}
	
	public void init(String msg, String msk)
	{
		
		msg = msg.replace("'", "");
		setBody(msg);
		
		
		
		if (msk=="")
		this.mask = WordMgr.getMask(msg);
		else
		this.mask = msk;
		
		this.saytime = System.currentTimeMillis()/1000;
	}
	
	private void setBody(String msg) {
		// TODO Auto-generated method stub
		
		if (msg.length()>100) 
		msg = msg.substring(0, 100);
		
		body=msg;
	}

	MaMessage(String msg, String msk)
	{
		super();
		init(msg, msk);
	}
	
	MaMessage(String msg)
	{
		super();
		init(msg);
	}

	public String getBody() {
		// TODO Auto-generated method stub
		return body;
	}
	
	
}
