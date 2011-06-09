package com.liuyix.xmpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Collection;

import org.jivesoftware.smack.packet.Message;

/**
 * 负责全局存储的类，便于扩展和统一更改变化。</br>
 * FUTURE 使用SQLite作为数据库</br>
 * 5-27</br>
 * 创建StorageManager类（仅提供用户名即可），创建时就把所有的目录和文件建好（user/msg/;user/date.log）</br>
 * 对外接口：store(Message)：用于存储msg，存放位置为/user/msg/[from]/[date]</br>
 * 存放的格式（定义为一个方法，方便以后扩展）暂且为时间+姓名：消息内容</br>
 * 对外接口2：storeLog(String)给Util类使用，用于记录调试信息。FUTURE 独立一个Log类，封装Log</br>
 * <strong>创建一个interface:</strong>具备应有的方法，之后提供若干实现：1.简单的fs数据实现 2.sqlite数据库实现 3.远程mysql实现</br>
 * */
public class StorageManager {
	//所有的StorageManager共用一个Storage，避免错误
	static Storage storage = null;
	/**
	 * 构造函数，具体操作：建立文件、目录（若不存在）
	 * */
	public StorageManager() {
		super();
//		String name = "error_no_name";
//		if(username==null||username.equals("")){
//			Util.showErrMsg("构造函数参数不正确");
//		}
//		else{
//			name = username;
//		}
//		//userDir = mkdir(BASEDIR,username);
		//应该保证全局的唯一性
		if(storage == null)
			storage = new FsStorage(Util.getYourname());
	}
	

//	/**
//	 * 根据Type创建适当的文件
//	 * 初始化IO--File以BufferedWriter
//	 * */
//	private void initIO(StorageType type) {
//		try {
//			switch(type){
//			case LOG:
//				createLogStoreFile();
//				break;
//			case MESSAGE:
//				createMsgStoreFile();
//				break;
//			default:
//				Util.showErrMsg("未识别类型-initIO@Storage");
//				//无法识别则转为标准IO
//				createTmpFile();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			Util.showErrMsg("StorageManager","initIO","创建文件初始化失败,启用标准IO");
//		}
//		
//	}
	/**
	 * 供Conversation调用的，专门存储Message的方法
	 * @param msg 需要存储Message实例
	 * */
	//TODO 未实现：根据message的不同类型存储！
	public void store(Message msg){
		//TODO 方法类同于tolog方法，返回的是错误号
		//需要检查msg.body()是否为空
//		int errcode = storage.store(msg);
//		if(errcode == 0){
////			Util.showDebugMsg("Msg存储成功");
//		}
//		else{
//			Util.showErrMsg("Msg存储失败，错误代码：" + errcode);
//			Util.showErrMsg("错误信息：" + storage.getErrMsg(errcode));
//		}
//		if(writer == null){
//			Util.showErrMsg("StorageManager", "store", "writer为null");
//			return;
//		}
//		try {
//			String toStoreInfo = getFormatMsg(msg);
////			Util.showDebugMsg(toStoreInfo);
//			writer.append(toStoreInfo);
//			writer.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//			Util.showErrMsg("StorageManager", "store", "存储过程失败！");
//		}
	}
	
	public void write2log(String log){
		/* 一个应用接口的情境：
		 * 调用storage.tolog(String)返回errcode
		 * errcode == 0:无错误
		 * errcode != 0:代表错误号
		 * storage.getErrMsg(errcode)取回错误信息
		 * */
		int errcode = storage.tolog(log);
		if(errcode == 0){
			Util.showDebugMsg("log存储成功");
		}
		else{
			Util.showErrMsg("log存储失败。");
			Util.showErrMsg("错误原因:\t" + storage.getErrMsg(errcode));
		}
		
	}
	
}
