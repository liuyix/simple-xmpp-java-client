package com.liuyix.xmpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;

//import org.jivesoftware.smack.Connection;

public class Util {
//	static BufferedWriter writer = new BufferedWriter(new FileWriter(new File("JClient.log")));
	private static String yourname;
	//user与jid的查询，key是jid,value是user
	private static Map<String,String> jid2user = new java.util.concurrent.ConcurrentHashMap<String,String>();
//	private static Map<String,String> user2jid = new java.util.concurrent.ConcurrentHashMap<String, String>();
	public static String getYourname(){
		return yourname;
	}
	public static void setUsername(String name){
		if(name == null || name.equals("")){
			yourname = "noname";
			Util.showErrMsg("setUsername error!");
		}
		else
			yourname = name;
	}
	/**
	 * 统一的debug信息输出，方便以后的扩展</br>
	 * <strong>此方法为*行输出*</strong>
	 * @param msg 输出信息
	 * */
	public static void showDebugMsg(String msg){
		if(msg.equals("NOT NULL")){
			System.out.println("showDebugMsg:NOT NULL");
		}
		else System.out.println(msg);
	}
	
	/**
	 * 统一的error信息输出，方便以后的扩展</br>
	 * <strong>此方法为*行输出*</strong>
	 * @param err 输出信息
	 * */	
	public static void showErrMsg(String err){
		System.err.println(err);
	}
	/**
	 * <strong>非行输出</strong>的debug信息输出
	 * @param msg 输出信息
	 * */
	public static void printDebugMsg(String msg){
		System.out.print(msg);
	}
	/**
	 * <strong>非行输出</strong>的debug信息输出
	 * @param msg 输出信息
	 * */
	public static void printErrMsg(String msg){
		System.err.print(msg);
	}
	
	/**
	 * 格式化的debug信息
	 * @param className
	 * @param methodName
	 * @param msg
	 * */
	//TODO 需要重构，有许多相似代码
	public static void showDebugMsg(String className,String methodName,String msg){
		System.out.println("#DEBUG#");
		System.out.println("Class:" + className + "Method:" + methodName);
		System.out.println(msg);
	}
	/**
	 * 格式化的error信息
	 * @param className
	 * @param methodName
	 * @param msg
	 * 
	 * */	
	public static void showErrMsg(String className,String methodName,String msg){
		System.err.println("#DEBUG#");
		System.err.println("Class:" + className + "Method:" + methodName);
		System.err.println(msg);	
	}
	
	/**
	 * 用于分析Packet的公用方法
	 * */
	public static void showPacketInfo(String title, Packet packet) {
		String testInfo = title;		
		testInfo += "\nXmlns:\t" +packet.getXmlns();
		Collection<PacketExtension> pktExts = packet.getExtensions();
		testInfo += "\nPacketExtensions:";
		if(pktExts.size() != 0){
			for(PacketExtension ext : pktExts){
				testInfo += "\nNamespace:\t" 
						+ext.getNamespace() 
						+ "\tElementname:\t"
						+ ext.getElementName();
			}
		}
		else testInfo += "无";
		testInfo += "\nProperties:";
		Collection<String> properties = packet.getPropertyNames();
		if(properties.size() == 0){
			testInfo += "无";
		}
		else{
			for(String property : properties){
				testInfo += "\n" + property;
			}
		}
//		testInfo += "\nXMPP Error:" + packet.getError().getCode();
		Util.showDebugMsg(testInfo);
	}
	/**
	 * 用于分析smack收到的message的详细信息
	 * */
	public static void showMsgInfo(String title, Message recvMsg) {
		String msgInfo = "\n" + title;
		msgInfo  +=  "\nFrom:\t" + recvMsg.getFrom()
						+ "\nType:\t" +recvMsg.getType()
						+ "\nBody:\t" + recvMsg.getBody()
						+ "\nSubject:\t" + recvMsg.getSubject();
		Util.showDebugMsg(msgInfo);
		
	}
	/**
	 * 全局的查询jid对应的username的方法<br>
	 * @return 参数为null，返回null;<br>
	 * 			若未找到指定的username，返回jid，并更新数据结构<br>
	 * 			若找到，则返回username<br>
	 * @param jid 要查找的jid
	 * 
	 * */
	public static String getUsername(String jid) {
		String username;
		if(jid!=null){
			int slashLoc  = jid.lastIndexOf('/');
			if(slashLoc == -1)
				username =  jid2user.get(jid);
			else{
//				Util.showDebugMsg("Util.getUsername:" + jid.substring(0, slashLoc));
				username = jid2user.get(jid.substring(0, slashLoc));
			}
			if(username == null){
				Util.showErrMsg("未找到指定用户！");
				//若没有找到指定用户则，username指定为jid
				updateMap(jid, jid);
			}
			return username;
		}
		Util.showErrMsg("Util.getUsername:参数为null!");
		return null;
	}
	
//	/**
//	 * 
//	 * 
//	 * @return 不存在username则返回null
//	 * */
//	public static String getJidByUsername(String username){
//		String jid;
//		if(username != null){
//			jid = user2jid.get(username);
//			return jid;
//		}
//		else
//			return null;
//		
//	}
	
	protected static void updateMap(String jid, String username) {
		if(jid!=null&&username!=null){
			jid2user.put(jid, username);
//			user2jid.put(username, jid);
			Util.showDebugMsg("\nupdateMap\njid: " + jid + "\nuser: " + username);
		}
		else{
			Util.showErrMsg("updateMap参数错误！");
		}
	}
	
	protected static void updateMap(RosterEntry entry) {
		if(entry==null){
			Util.showErrMsg("updateMap-Entry 参数错误");
			return;
		}
		else{
			updateMap(entry.getUser(),entry.getName());
		}
	}
	
}
