package com.liuyix.xmpp;

public interface ChatRequestListener {
	void handleChatRequest(String username,String jid);
	/**
	 * 指定发送人发送信息的方法
	 * */
	public void handleSendMsgRequest(String jid,String msg);
}
