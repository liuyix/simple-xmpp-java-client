package com.liuyix.xmpp;

import java.util.Collection;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;

//import org.jivesoftware.smack.Connection;

class Util {
//	static BufferedWriter writer = new BufferedWriter(new FileWriter(new File("JClient.log")));
	private static String username;
	public static String getUsername(){
		return username;
	}
	public static void setUsername(String name){
		if(name == null || name.equals("")){
			username = "noname";
			Util.showErrMsg("setUsername error!");
		}
		else
			username = name;
	}
	/**
	 * 统一的debug信息输出，方便以后的扩展</br>
	 * <strong>此方法为*行输出*</strong>
	 * @param msg 输出信息
	 * */
	public static void showDebugMsg(String msg){
		System.out.println(msg);
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
}
