package com.liuyix.xmpp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

/**
 * 负责Roster相关的部分
 * 
 * */
//TODO 重构去掉所有的System.out
//TODO 增加功能：组管理
public class RosterManager {
	
	Roster roster = null;
	Connection conn;
	
	
	public RosterManager(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Connection is NULL!");
		} else if (conn.isAuthenticated() != true) {
			throw new IllegalArgumentException("Connection is not auth");
		}
		//记住connection
		this.conn = conn;
		roster = conn.getRoster();
		initJid2UserMap(roster);
		roster.addRosterListener(new UpdateJid2UserMapListener());
	}//end construction
	
	//初始化jid2user数据结构
	private void initJid2UserMap(Roster roster) {
		for(RosterEntry entry : roster.getEntries()){
			Util.updateMap(entry);
			Util.showDebugMsg("\ninitJid2UserMap:");
			Util.showDebugMsg("\nuser:" + entry.getUser());
			Util.showDebugMsg("\ngroup:");
			for(RosterGroup group : entry.getGroups()){
				Util.showDebugMsg(group.getName());
			}
			
		}
	}

	public Collection<RosterEntry> getEntries(){
		return roster.getEntries();
//		return null;
	}
	
	/**
	 * 添加好友：若group不存在则创建
	 * @param jid 用户JID
	 * @param nickname 用户的昵称
	 * @param groups 要加入的组别
	 * @exception IllegalArgumentException 若参数为空字符或null则抛出异常
	 * */
	public void addFriend(String jid,String nickname,String []groups){
		if(jid==""||nickname==""||groups==null){
			throw new IllegalArgumentException("addFriend@FriendManager");
		}
		try {
			String userGroup;
			for(int i=0;i<groups.length;++i){
				userGroup = groups[i];
				if(roster.getGroup(userGroup) == null){
					roster.createGroup(userGroup);
				}
			}			
			roster.createEntry(jid, nickname, null);
		} catch (XMPPException e) {
			// TODO 添加RosterEntry时遇到的Exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除好友
	 * 
	 * */
	public void deleteFriend(String jid){
		try {
			roster.removeEntry(roster.getEntry(jid));
		} catch (XMPPException e) {
			// TODO remove RosterEntry遇到的Exception 
			e.printStackTrace();
		}
	}
	
	/**
	 * 打印出所有的花名册信息
	 * @param writer 输出流
	 * @exception IllegalArgumentException 若writer为空则抛出
	 * */
	public void showRosterInfo(BufferedWriter writer){
		if(!checkRosterWriter(writer)){
			System.err.println("showRosterInfo failed:writer/roster is NULL ");
			return;
//			throw new IllegalArgumentException("calling showRosterInfo");
		}
		Collection<RosterEntry> rosterCollection = this.getEntries();
		for(RosterEntry entry:rosterCollection){
			try {
				writer.append("Name:\t");
				writer.append(entry.getName());
				writer.append("\nJID:\t");
				writer.append(entry.getUser());
				writer.append("\nType:");
				writer.append(entry.getType().toString());
				writer.append("\n");
				writer.flush();
			} catch (IOException e) {
				// 在BufferdWriter中显示Roster时可能遇到的IOException 
				e.printStackTrace();
			}
		}
	}//end showRosterInfo
	
	/**
	 * 调用显示在线用户的信息
	 * @param writer 输出流
	 * */
	public void showOnlineUser(BufferedWriter writer){
		if(!checkRosterWriter(writer)){
			System.err.println("showOnlineUser parameter is null!");
			return;
		}
		//遍历roster，对每个用户应用getPresence方法
		Collection<RosterEntry> collection = roster.getEntries();
		ArrayList<Presence> availUsers = new ArrayList<Presence>();
		Presence presenceTmp;
		for(RosterEntry entry:collection){
			presenceTmp = roster.getPresence(entry.getUser());
			if(presenceTmp.isAvailable()){
				availUsers.add(presenceTmp);
			}
		}
		
		try {
			for(Presence ptmp:availUsers){
				writer.append("JID:\t" + ptmp.getFrom() + "\n");
				writer.flush();
			}
		} catch (IOException e) {
			// 写入输出流时可能会出现的exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 内部的公共类
	 * 功能：检查BufferedWriter是否正确
	 * */
	private boolean checkRosterWriter(BufferedWriter writer){
		return writer==null||roster==null ?
				false:true;
	}
	
	public Roster getRoster(){
		return roster;
	}
	
	/**
	 * @author cnliuyix
	 * 该监听接口负责更新Util中user和jid的相互查询的数据结构
	 *
	 */
	private class UpdateJid2UserMapListener implements RosterListener {

		/* (non-Javadoc)
		 * @see org.jivesoftware.smack.RosterListener#entriesAdded(java.util.Collection)
		 */
		@Override
		public void entriesAdded(Collection<String> collection) {
			updateMap(collection,"entriesAdded");
		}

		/* (non-Javadoc)
		 * @see org.jivesoftware.smack.RosterListener#entriesDeleted(java.util.Collection)
		 */
		@Override
		public void entriesDeleted(Collection<String> arg0) {
			//FUTURE 删除操作则不更新

		}

		/* (non-Javadoc)
		 * @see org.jivesoftware.smack.RosterListener#entriesUpdated(java.util.Collection)
		 */
		@Override
		public void entriesUpdated(Collection<String> addrs) {
			updateMap(addrs,"entriesUpdated");
		}

		/* (non-Javadoc)
		 * @see org.jivesoftware.smack.RosterListener#presenceChanged(org.jivesoftware.smack.packet.Presence)
		 */
		@Override
		public void presenceChanged(Presence presence) {
		//好友状态更新
		//得到最佳的状态（单用户多个终端登录，其中一个下线的问题）
		String user = presence.getFrom();
		Presence bestPresence = roster.getPresence(user);
//		System.out.println(user + " is " + presence);
		System.out.println("now:" + user + " status:" + bestPresence);				
	}

	}
	private void updateMap(Collection<String> collection,String method) {
		//
		for(String addr : collection){
			Util.showDebugMsg("entriesAdded: " + addr);
			RosterEntry entry = roster.getEntry(addr);
			if(entry == null){
				Util.showErrMsg(method + "更新出错！");
				return;
			}
			Util.updateMap(entry);
		}
	}


}
