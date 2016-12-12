package com.gorcer.jMarishko.api;


public class MUser {
	public String Login;
	public Integer Respect=0;
	public Dialog dialog = null;
	public Dialog dialogLike = null;
	
	public int id=0;
	public long wait_time=0;
	public boolean isAllow=false;
	
	public MUser LinkedUser = null;


	MUser(String UserName)
	{
		this.Login = UserName;		
		this.id = DBManager.processUser(UserName);
		this.dialog = new Dialog(this);
		this.dialogLike = new Dialog(this);
	
	}
	
	public void addLog(String msg, char type)
	{
		MaMessage msg2 = new MaMessage(msg);
		addLog(msg2, type);
	}
	
	public void addLog(MaMessage msg, char type)
	{
		/*
		 * dulicate
		 * if (dialog.size()>0)
		{
			if (dialog.get(dialog.size()-1).type==type)
			{
				updLog(dialog.size()-1, WordMgr.AppendMessage(dialog.get(dialog.size()-1), msg));
			}
		}*/
		
		msg.type = type;
		msg.owner = this;
		
		dialog.add(msg);
	}
	
	private void updLog(int pos, MaMessage appendMessage) {
		// TODO Auto-generated method stub
		dialog.set(pos, appendMessage);
	}

	public MaMessage getLastMsg()
	{
		if (this.dialog.size()>0)
		return this.dialog.lastElement();
		else
		return(null);
	}

	public void thinkAboutSomething() {

		this.wait_time = System.currentTimeMillis()/1000 + MaConfig.Wait4Something;
		WordMgr.addUser2WaitSomething(this);
		
		
		
	}

	public void stopWait4Something() {
		this.wait_time=0;
		
	}

	public MaMessage getLastMsg(char c) {
		// TODO Auto-generated method stub
		MaMessage lastMsg = null;
		
		for (MaMessage msg : this.dialog)
		{
			if (msg.type == c)
				lastMsg = msg;
		}
		return(lastMsg);
	}
}
