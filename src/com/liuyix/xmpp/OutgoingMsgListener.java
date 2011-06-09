package com.liuyix.xmpp;

//负责监听由界面发送的消息
public interface OutgoingMsgListener {
	/**
	 * @param username 联系人jid
	 * @param msg 要发送的信息
	 * 
	 * */
	public void handleOutgoingMsg(String jid,String msg);	
}
