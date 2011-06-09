package com.liuyix.xmpp;

import org.jivesoftware.smack.packet.Message;

/**
 * 用途：底层库和界面之间的通信接口
 * 当底层库收到了外界的Message时，调用该接口的方法。
 * 本接口是供GUIStarter使用，有GuiStarter再分发给其他的界面
 * 
 * **/
public interface IncomingMsgListener {
	void handleIncomingMsg(Message.Type type,Message msg);
}
