package com.liuyix.xmpp;

import java.util.Collection;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

public class PresenceManager implements PresenceListener{

	private Connection connection;
	public static final String[] PRESENCE = new String[]{
		"在线",
		"离开",
		"忙碌"
	};

	public PresenceManager(Connection conn) {
		super();
		if (conn == null || conn.isAuthenticated() != true) {
			Util.printErrMsg("Conversation init ERROR:Connection ");
			if (conn == null)
				Util.showErrMsg("NULL");
			else
				Util.showErrMsg("NOT AUTH");
			return;
		}
		// Connection已通过验证
		this.connection = conn;

	}

	@Override
	public void updatePresence(Presence presence, String username) {
		
	}
		
}
