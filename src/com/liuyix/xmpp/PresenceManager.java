package com.liuyix.xmpp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;


/*管理全局的所有的用户（包括自己）的Presence，
 *功能：可以查询用户（including yourself）Presence
 *		可以更新自己的状态信息 
 * 
 * 
 * */

public class PresenceManager implements PresenceListener{

	private Connection connection;
	private HashMap<String,Presence> contactPresences;
	public static final String[] PRESENCE = new String[]{
		"在线",
		"离开",
		"忙碌"
	};
	//存储着所有用户的Presence信息
	//这应该属于RosterManager实现的内容，因为Presence都是联系人发出的。
//	private Map<String,Presence> presences;
	
	public PresenceManager(Connection conn) {
		super();
		if (conn == null || conn.isAuthenticated() != true) {
			Util.printErrMsg("PresenceManager init ERROR:Connection ");
			if (conn == null)
				Util.showErrMsg("NULL");
			else
				Util.showErrMsg("NOT AUTH");
			return;
		}
		// Connection已通过验证
		this.connection = conn;
		//TODO 添加一个监听端口，用于监听所有的Presence消息！
		connection.addPacketListener(new AllPrensenceListener(), new PacketTypeFilter(Presence.class));
		initContactPresences(connection.getRoster().getEntries());
//		initPresences(connection.getRoster().getEntries());

	}


	private void initContactPresences(Collection<RosterEntry> collection) {
		for(RosterEntry entry : collection){
			updatePresence(null, entry.getName());
		}
		
	}


	@Override
	public void updatePresence(Presence presence, String username) {
		//TODO 需要更新
		if(contactPresences == null){
			contactPresences = new HashMap<String,Presence>();
		}
		contactPresences.put(username, presence);
	}
	
	//监听所有的presence信息
	private class AllPrensenceListener implements PacketListener {

		@Override
		public void processPacket(Packet packet) {
			Presence presence = (Presence)packet;
			Util.showPacketInfo("AllPresenceListener", packet);
			updatePresence(presence, Util.getUsername(packet.getFrom()));

		}

	}

	/**
	 * 返回指定用户(username)的Presence
	 * @param username 用户的username，而不是jid
	 * @return 若参数为空，返回Null;否则返回presence
	 * */
	public Presence getPresence(String username) {
		if(username == null)
			return null;
		else if(contactPresences.containsKey(username)){
			Presence presence = contactPresences.get(username);
			if(presence == null){
				//TODO 全面更新Presence,不再返回原始的Presence,新建一个新的表达方式
				return new Presence(Type.unavailable, null, -1, null);
			}
			else
				return presence;
		}else{
			//该username不在联系人列表中
			return null;
		}
	}
}
