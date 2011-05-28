/**
 * 
 */
package com.liuyix.xmpp;

import java.io.File;
import java.io.PrintStream;

import org.jivesoftware.smack.packet.Message;

/**
 * 实现了Storage接口，以文件系统作为一个简单的层次数据库，存储Message和Log
 * 文件的存储形式：[programme-dir|custom]/data/[user]/msg/[friend]/[date].txt
 * @author cnliuyix
 *
 */
class FsStorage implements Storage {

	//暂时将根目录确定化
	static final File BASEDIR = new File("./data");
	static final String MSGDIR = "msg";
	static final String LOGDIR = "log";
	File userDir;
	File logFile;
	File msgDir;
	PrintStream msgStream;
	FsStorage(String username) {
		// TODO Auto-generated constructor stub
	}

//	/**
//	 * 建立目录的功能类
//	 * 建立文件的错误处理策略：
//	 * 1.若存在username文件，则改用username.d目录
//	 * 2.若存在username.d文件，则将其重命名为username.d.[date]并发出错误提醒
//	 * */
//	private File mkdir(File basedir, String dirname) {
//		File newDir = new File(basedir,dirname);
//		if(newDir.exists()&&newDir.isFile()){
//			//存在一个同名的文件,创建一个username.d目录
//			newDir = new File(BASEDIR,dirname+".d");
//			if(newDir.exists()&&newDir.isFile()){
//				//TODO 重命名该文件，并建立dir
//				Util.showErrMsg(newDir+"：存在同名文件");
//			}
//		}
//		else if(newDir.exists()!=true){
//			//不存在username目录
//			try {
//				if(!newDir.mkdirs()){
//					Util.showErrMsg("建立目录失败！");
//					msgStream = System.out;
//				}
//			} catch (Exception e) {
//				// 创建目录遇到异常
//				e.printStackTrace();
//				//将msg的log
//				msgStream = System.out;
//			}
//		}		
//		return null;
//	}
	
//	/**
//	 * 将Message格式化为String便于存储
//	 * 
//	 * */
//	private String getFormatMsg(Message msg){
//		String toStoreInfo = "\nFrom:" + msg.getFrom();
//		toStoreInfo += "\nTo:" + msg.getTo();
//		toStoreInfo += "\nMsg:" + msg.getBody();
////		toStoreInfo += "\nXML:" + msg.toXML();
//		toStoreInfo += "\n##\n";
//		return toStoreInfo;
//	}
	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see com.liuyix.xmpp.Storage#getErrMsg(int)
	 */
	@Override
	public String getErrMsg(int errcode) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.liuyix.xmpp.Storage#store(org.jivesoftware.smack.packet.Message)
	 */
	@Override
	public int store(Message msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.liuyix.xmpp.Storage#tolog(java.lang.String)
	 */
	@Override
	public int tolog(String log) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

}
