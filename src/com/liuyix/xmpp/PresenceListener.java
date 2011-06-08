package com.liuyix.xmpp;

import org.jivesoftware.smack.packet.Presence;

/***
 * 用于提醒状态更新的接口，可以用于2个方面：
 * 1.联系人状态更新时，相应的界面更新和提示
 * 2.界面上自己更新了状态，相应的发送Presence更新自己的状态
 * */
public interface PresenceListener {
	public void updatePresence(Presence presence,String username);
}
