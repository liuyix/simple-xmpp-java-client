/**
 *统一的存储接口 
 */
package com.liuyix.xmpp;

import org.jivesoftware.smack.packet.Message;

/**
 * @author cnliuyix
 *
 */
/* 一个应用接口的情境：
 * 调用storage.tolog(String)返回errcode
 * errcode == 0:无错误
 * errcode != 0:代表错误号
 * storage.getErrMsg(errcode)取回错误信息
 * */
interface Storage {

	/**
	 * 对外接口：存储msg
	 * @param msg
	 * @return 返回errcode，若ErrorCode为0则表示成功，否则则是相关的errid
	 */
	int store(Message msg);
	/**
	 * 对外接口：根据errcode取得错误信息
	 * @param errcode 相关的errorcode
	 * @return 对应的错误信息，<strong>若errcode错误，则返回null</strong>
	 * */
	String getErrMsg(int errcode);
	/**
	 * 对外接口：存储log信息的方法，返回值相同
	 * @param log 要存储的log信息
	 * @return 相关的errcode，为0表示操作成功，若为非0值，可以使用<code>getErrorMsg</code>方法得到
	 * */
	int tolog(String log);
	


}
