package com.gorcer.jMarishko.api;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Timer;


public class WordMgr {

	public static Vector<MUser> Wait4Somthing = null;
	private static Timer timer = null;
	
	/**
	 * Получить ответ
	 * 
	 * @param user
	 * @param msg
	 * @return
	 */
	static public MaMessage getAnswer(MUser user, MaMessage msg)
	{		
		MaMessage ans = DBManager.getAnswer(msg, user, true);
		
		if (ans==null) //Ответ не найден
		{
			if (Math.round(Math.random()*10)==4) // В 1 из 10 случаев говорим коронную фразу Маришки.
			{
			ans = new MaMessage("Я не знаю что ответить.");						
			DBManager.processMessageByBody(ans, user);
			}
			
			user.thinkAboutSomething();
		}
		
		if (ans!=null)
		user.addLog(ans,'<');
		
		return ans;
	}
	
	/**
	 * Обработка системных сообщений
	 * 
	 * @param msg
	 * @return
	 */
	static private boolean SystemMessage(MaMessage msg)
	{
		//дописать
		return false;
	}

	public static void processUserMessage(MUser user, MaMessage msg) {
		
		if (SystemMessage(msg))
		{		
			return;
		}
		
		
		
		MaMessage lm = user.getLastMsg();		
		
			
		
		// Если пользователь досылает сообщения, объединять их в одно
		/*if (lm!=null)		
		if (lm.type=='>')
		{
		// msg = WordMgr.AppendMessage(lm, msg);
		 lm = lm.owner.getLastMsg('<');
		}*/
    	
			
		msg.id=DBManager.processMessageByMask(msg, user);
		
		
		int link_id=0;
		
		// Первое сообщение от пользователя. Приветствие.
		if (lm==null)	{
			processHello(user, msg);
			
			}
		else
		if (lm.type=='<') //Сообщение от пользователя не первое и ему был дан ответ ботом
		{
		if (likeSomething(lm, msg)) // Если похоже на "случайное сообщение", т.е. пользователь долго не отвечал, а потом ответил.	
			processSomething(user, msg);
		else	
		link_id=LinkMessages(lm, msg); //Обработка ответа  (связывание)
		}
		
		if (link_id != 0)
		DBManager.processDialog(user,link_id);
		
		user.addLog(msg, '>');
		
	}

	public static MaMessage  AppendMessage(MaMessage lm, MaMessage msg) {
		// TODO Auto-generated method stub
		
		msg = new MaMessage(lm.body + "\r\n" + msg.body);
		msg.owner =lm.owner;
		msg.type = lm.type;
				
		return(msg);	
	}

	/**
	 * Обработка случайной фразы в диалоге, считается что фраза начинает новую тему
	 * 
	 * @param user
	 * @param msg
	 */
	private static void processSomething(MUser user, MaMessage msg) {
		DBManager.addSomething(msg);
		
		int link_id=LinkMessages(null,msg);
		
		if (link_id!=0)
		DBManager.processDialog(user,link_id);
	}

	/**
	 * Проверка что сообщение - случайная фраза
	 * 
	 * @param lm
	 * @param msg
	 * @return
	 */
	private static boolean likeSomething(MaMessage lm, MaMessage msg) {
		
		return (msg.saytime-lm.saytime>MaConfig.likeSomethingTime);
	}

	/**
	 * Обработка приветствия
	 * 
	 * @param user
	 * @param msg
	 */
	private static void processHello(MUser user, MaMessage msg) {

		DBManager.addHello(msg);
		int link_id=LinkMessages(null,msg);
		
		if (link_id!=0)
		DBManager.processDialog(user,link_id);		
	}

	/**
	 * Связь двух фраз
	 * @param msg1
	 * @param msg2
	 * @return
	 */
	private static int LinkMessages(MaMessage msg1, MaMessage msg2) {
	//	if (msg1==null || msg2==null ) return 0;
		
		return DBManager.linkMessages(msg1, msg2);
		
	}

	
	/**
	 * Получение маски - нормализация сообщения для удобного сравнения
	 * @param txt
	 * @return
	 */
	static public String getMask(String txt)
	{
		String msk = "";
		msk=txt;
		String str="";
		
		String cms = "),-(:-!@#$%^&*=_+/|][{}~`.?><'";
		cms = cms + Character.toChars(13).toString()+Character.toChars(10).toString()+Character.toChars(0).toString();
		
		msk=msk.toLowerCase();
		
		//Удаляем повторяющиеся символы
		msk=CropRepeatSymbols(msk);

		//Удаляем слова-паразиты
		msk=CropParasiteWords(msk);
		
		//Удаляем лишние символы
		String msk2 = msk;
		for (int i=0;i<cms.length();i++)
		{
			str="";
			str+= cms.charAt(i); 
			msk2=msk2.replace(str, "");
			
		}
		
		if (msk2.length()!=0)
			msk=msk2;
			
		
		
		msk = getPhoneticMask(msk);
		
//		System.out.println(msk);		
		return(msk);
	}

	/**
	 * Фонетическая нормализация текста
	 * @param str
	 * @return
	 */
	private static String getPhoneticMask(String str) {

		String result="";
		int lng=0;
		int stp=4;
		int wpos=1;
		String tstr=str;
		int wpose=0;
		String word="";
		double cnt=0;
		long marg =0;
		char[] arrnword ={};
		
		while (wpos!=-1) 
		{			
			wpose=tstr.indexOf(" ");

			if (wpose!=-1)
			word = tstr.substring(0, wpose);
			else
			word = tstr.substring(0, tstr.length());
				
			tstr=tstr.substring(wpose+1, tstr.length());
			
			lng=word.length();
			
			if (lng<=4)
			{
				result=result+word;
				wpos=wpose;
				continue;
			}

			cnt = Math.ceil((lng-1)/stp);
			
			marg = Math.round(((lng-(cnt*stp))-stp) / 2);

			String nword = "";
			
			for (int j=0;j<lng;j++)				
			nword=nword+"x";
			
			arrnword = nword.toCharArray();
			
			arrnword[0]=word.charAt(0);
			arrnword[1]=word.charAt(1);
			
			
			
			for (int i=1;i<=cnt;i++)	 
				arrnword[(int)((i*stp)+marg)-1]=word.charAt((int)((i*stp)+marg)-1);
			 
			  
			  arrnword[word.length()-1]=word.charAt(word.length()-1);
			  arrnword[word.length()-2]=word.charAt(word.length()-2);  
		
			  nword = new String(arrnword);
			  
			  
			 wpos=wpose;
			 result=result+nword;
		}
		return result;
	}

	/**
	 * Вырезаем слова паразиты
	 * 
	 * @param msk
	 * @return
	 */
	private static String CropParasiteWords(String msk) {
		
		String result=null;
		Vector<String> parasite = new Vector();
		
		int i=0;
		parasite.add(" а ");
		parasite.add(" и ");
		parasite.add(" ну ");
		parasite.add(" кароче ");
		parasite.add(" короче ");
		parasite.add(" но ");
		parasite.add(" фу ");
		parasite.add(" ваще ");
		parasite.add(" ещё ");
		parasite.add(" гы ");
		parasite.add(" о да ");
		parasite.add(" э ");
		parasite.add(" угу ");
		parasite.add(" ой ");
		
		//Смайлы
		
		parasite.add(":-)");
		parasite.add(";-)");
		parasite.add(":-(");
		parasite.add(":-*");
		parasite.add("*TIRED*");
		
		parasite.add("*DRINK*");
		parasite.add("*SORRY*");
		parasite.add("*OK*");
		parasite.add("*SUP*");
		parasite.add("*STOP*");

		parasite.add("*JOKINGLY*");
		parasite.add("*THUMBSUP*");
		parasite.add("*KISSED*");
		parasite.add("*INLOVE*");
		parasite.add("*STOP*");
		parasite.add("@} - > - -");
		parasite.add(":  -[");
		parasite.add(":-[");
		parasite.add(": - *");
		parasite.add(": -$");
		parasite.add("@ =");
		parasite.add(": -)");
		parasite.add("]: - >");
		parasite.add("( *)( *)");
		parasite.add("% -)");
		parasite.add(":)");
		parasite.add("]: - >");
		
		i=parasite.size();
		result = msk;		
		
		for(int j=0;j<i;j++)
		{
			result=result.replace(parasite.get(j), "");	
		}
		
		if (result.length()==0)
			result=msk;
		
		return(result);
	}

	/**
	 * Вырезаем повторяющиеся символы
	 * 
	 * @param msk
	 * @return
	 */
	private static String CropRepeatSymbols(String msk) {
		String result="";
		char last = 0;
		char[] msk_arr = msk.toCharArray();
		
		
		for(int i=0;i<msk.length();i++)
		{
		 if (i==0) { last=msk_arr[i];result+=last;}
		 
		 if (msk_arr[i]!=last)
		 {
			 last=msk_arr[i];
			 result+=last; 
		 }
		}
		
		if (result=="")
			result=msk;
		
		return (result);
		
	}

	/**
	 * Получить приветственное сообщение
	 * @param u
	 * @return
	 */
	public static MaMessage getHello(MUser u) {
		
		MaMessage msg = DBManager.getHello(u);
		
		if (msg != null)
			u.addLog(msg,'<');
		
		return msg;
		
	}

	/**
	 * Добавляет пользователя в список ожидающих случайной фразы
	 * @deprecated из skype-версии
	 * 
	 * @param user
	 */
	public static void addUser2WaitSomething(MUser user) {

		if (Wait4Somthing==null)
			Wait4Somthing = new Vector<MUser>();
		
		Wait4Somthing.add(user);
		
		initTimer();
		
	}

	/**
	 * @deprecated
	 */
	private static void initTimer() {
		
		if (timer==null)
		{
			timer = new javax.swing.Timer( MaConfig.SomethingTimer, new ActionListener()
			  {
			      public void actionPerformed(ActionEvent e)
			      {
			    	WordMgr.processWait4SomethingList();
			    	
			      }
			  } );
		}
		
		
		timer.start();
		
		
	}

	/**
	 * @deprecated
	 */
	protected static void processWait4SomethingList() {

	/*	MUser u=null;
		int i = 0;
		
		while(i!=WordMgr.Wait4Somthing.size())
		{
		
			u = WordMgr.Wait4Somthing.get(i);
			if (u.wait_time<=System.currentTimeMillis())
			{
				if (u.wait_time!=0) // Если пользователь ещё не ответил.
				Marishko.saySomething(u);
			 
			 WordMgr.Wait4Somthing.remove(i);
			 u.wait_time=0;
			}
			else
		    i++;			
		}
		
		if(i==0)
			timer.stop();*/
		
	}

	/**
	 * Поулчить случайную фразу
	 * @param u
	 * @return
	 */
	public static MaMessage getSomething(MUser u) {
		MaMessage msg = DBManager.getSomething(u, false);
		
		if (msg != null)
			u.addLog(msg,'<');
		
		return msg;
	}

	public static void See2Learn(MUser u, MaMessage msg) {
		// TODO Auto-generated method stub
		processUserMessage (u, msg);
		
		if (u.dialog.id!=u.LinkedUser.dialog.id)
			u.LinkedUser.dialog.id = u.dialog.id;
		
		
		u.LinkedUser.addLog(new MaMessage(msg.body), '<');
	}

	public static MaMessage AppendIfItNeed(MUser u, MaMessage msg) {
		// TODO Если пользователь досылает сообщения, объединять их в одно и выставлять признак
	

		MaMessage lm = u.getLastMsg();		
		
		
		if (lm!=null)		
		if (lm.type=='>')
		{
		 msg = WordMgr.AppendMessage(lm, msg);
			    		 
		}
		
		return msg;
	}
	
	
	
}
