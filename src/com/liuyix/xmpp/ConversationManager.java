/**
 * 负责所有的对话管理（chat，muc）
 * 对外只提供发送消息和接受消息的方法
 * 
 * 添加一个ChatManagerListener，这样每次创建一个Chat都会监听到。之后在该Listener里创建MessageListener，即可以监听所有的信息了。
 * 必须保证一个Connection只能有一个实例，因此措施是private构造函数
 * CAUTION: 写入文件可能有资源争用的问题，到时候遇到了再解决。方法就是在写入操作上加入synchronized
 * FIXME: 1.存储信息的完善 2.相关的聊天室管理--2011-5-26
 * TODO MUC要加入功能：1.登录时历史信息的设置 2.私聊 3.列出聊天室的中的所有成员
 */
package com.liuyix.xmpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.NotFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.MUCInitialPresence;


//未读信息设想：建立一个msglistener接口，外部类实现该接口，本类当有消息进来时，就调用该接口
/* 5-26：
 * 1.不需要使用event-listener模型，因为该模型实时性很高，而该方法正要避免的就是实时性，类同于collector，因此不需要建立Listener类。
 * 实现的方法就是一个数据结构+外部的一个flag查看，一个取信息方法，这样即可！
 * 2.使用了未读消息，则delayMsg不再区别于其他的message对待了。因此可以删除相关的代码了。
 * 5-26 15：50
 * 在未读信息上应该采用一个抽象的数据结构，因为我要实现的这个数据结构必须要有特点：读的时候就不能再写入，写可以并发
 * 5-26 20:00
 * 公共接口的方法，一定要检查参数
 * 不能有异常的一律要接到所有的异常
 * 5-27
 * 大幅度的更新了存储部分的代码，加入了2个PacketListener用来截获所有进出的Message用以存储
*/
/**
 * @author cnliuyix 
 * 所有的对话管理，包括<strong>chat</strong>,<strong>muc</strong>
 */
public class ConversationManager {

	/**
	 * 截获所有的非Error类型的Message用以存储
	 *
	 */
	private class AllIncomingMsgListener implements PacketListener {

		/* (non-Javadoc)
		 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
		 */
		@Override
		public void processPacket(Packet packet) {
			Message msg = (Message)packet;
			msgStorageManager.store(msg);
		}
	}

	/**
	 * 截获所有的发出去的msg信息，用以存储
	 * @author cnliuyix
	 *
	 */
	private class AllOutgoingMsgListener implements PacketListener {

		/* (non-Javadoc)
		 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
		 */
		@Override
		public void processPacket(Packet packet) {
			Message outgoingMsg = (Message)packet;
			msgStorageManager.store(outgoingMsg);
		}

	}

	/**
	 * 用于捕获message error
	 * */
	private class MsgErrorListener implements PacketListener {

		@Override
		public void processPacket(Packet packet) {
			Message errorMsg = (Message)packet;
//			Util.showPacketInfo("MessageError", packet);
//			Util.showMsgInfo("MessageError", errorMsg);
			XMPPError error = packet.getError();
			if(error != null){
				Util.showErrMsg("\n信息发送失败。错误类型：" + error.getType().toString() + "\t错误代码："+ error.getCode());
			}
		}

	}

	public enum Type {
		CHAT, MUC
	}

	/**
	 * 定义的文字聊天类型,在Starter中talk使用 Type of talks,used by "talk" in
	 * com.liuyix.xmpp.Starter
	 * */
	ChatManager chatManager = null;
	// 好友--chat,每个JID对应一个唯一的chat
	Map<String, Chat> chatMap = new ConcurrentHashMap<String, Chat>();

	private static final String FILENAME = "MSG";
//	private boolean hasUnreadMsgs = false;//可以使用map.isEmpty替代
	private File outFile;// 输出文件
	private BufferedWriter writer;// 输出流
	private StorageManager msgStorageManager;
	// FIXME 目前只能加入一个聊天室
	// CAUTION muc不是Null，则必须是joined的
	private MultiUserChat muc;
	private Connection connection;
	// 通过已经登录的connection得到登录用户名
	private String mucDefaultNickname;
	// 当没有指定nickname时，mucNickname等于mucDefaultNickname
	private String mucNickname;
	// 必须要在每次建立muc时清空或者新建
//	private Collection<Message> mucHistoryMsgs;
//	private Collection<Message> chatHistoryMsgs;
	//使用的java.util.concurrent中的线程安全的集合类
	private final Map<String,CopyOnWriteArrayList<Message>> unreadMsgs= 
		new ConcurrentHashMap<String, CopyOnWriteArrayList<Message> >();
//	private UnreadMsgs 
	

	public ConversationManager(Connection conn) {
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
		//添加的一个listener，截获所有的message with error的信息
		connection.addPacketListener(new MsgErrorListener(), 
				new AndFilter(new PacketTypeFilter(Message.class),new MessageTypeFilter(Message.Type.error)));
		//添加监听所有的发送的msg，用来存储为聊天记录
		connection.addPacketSendingListener(new AllOutgoingMsgListener(), new PacketTypeFilter(Message.class));
		//添加监听所有的收到的非error类型的message
		connection.addPacketListener(new AllIncomingMsgListener(),
				new AndFilter(new PacketTypeFilter(Message.class),
							new NotFilter(new MessageTypeFilter(Message.Type.error))));
		chatManager = conn.getChatManager();
		// 添加监听端口，用于控制所有的chat会话
		chatManager.addChatListener(new ChatListener());
		// 此处必须要加上PacketFilter，否则无法截获
		chatManager.addOutgoingMessageInterceptor(new OutgoingMsgMonitor(),
				new PacketTypeFilter(Message.class));
		outFile = new File(FILENAME);
		//建立存储实例
		msgStorageManager = new StorageManager();
		mucDefaultNickname = conn.getUser();
		mucNickname = mucDefaultNickname;
	}

	// static public Conversation getInstance(Connection conn){
	//		
	// return null;
	// }

	/**
	 * 外部API： 用于给指定的用户发送信息
	 * 
	 * */
	public void sendMsg(String jid, String text) {
		if(!checkParam(jid) || !checkParam(text))
			return;
		if (!isValidJID(jid)) {
			Util.showErrMsg("ERROR:JID is not valid.sendMsg");
			return;
		}

		if (!chatMap.containsKey(jid)) {
			creatChat(jid);
		}
		if (chatMap.containsKey(jid)) {
			try {
				chatMap.get(jid).sendMessage(text);
			} catch (XMPPException e) {
				// TODO 未实现：给指定JID发送text时的exception
				e.printStackTrace();
			}
		} else {// 建立chat没有成功
			Util.showErrMsg("chat建立失败,消息未发送");
		}

	}

	/**
	 * 建立一个chat
	 * 
	 * @param jid
	 *            一个指定的jid
	 * */
	private Chat creatChat(String jid) {
		Chat chat = chatManager.createChat(jid, new MessageListener() {
			@Override
			public void processMessage(Chat chat, Message msg) {
				// TODO 未完成：收到消息---未读信息
				// 应该放入“未读消息”中
				Util.showDebugMsg("\n"
								+ chat.getParticipant()
								+ ":\t"
								+ msg.getBody());
			}
		});
		// 加入到chatMap
		chatMap.put(jid, chat);
		return chat;
	}

	/**
	 * 加入指定的聊天室,默认的nickname为登录名，其他都为默认
	 * 
	 * @param roomAddr
	 *            聊天室名称，如muc@conference.localhost,<strong>需添加聊天服务器地址</strong>
	 * @param nickname
	 *            加入时的昵称
	 * @return 若成功则返回0,否则返回错误代码,若错误错误未知则返回-1---2011-5-26 18时
	 * */
	public int joinChatroomByAddr(String roomAddr, String nickname) {
//		if(!checkParam(roomAddr)|| !checkParam(nickname)){
//			Util.showErrMsg("joinChatroomByAddr:参数为空！！");
//			return -1;
//		}
		muc = new MultiUserChat(connection, roomAddr);		
		try {
			muc.join(nickname);
			if (muc.isJoined()) {
				muc.addMessageListener(new AllMUCMsgListener(
						new BufferedWriter(new OutputStreamWriter(System.out)),
						true));
//				mucHistoryMsgs = new ArrayList<Message>();
				return 0;
			}
		} catch (XMPPException e) {
			// 加入聊天室失败
//			Util.showErrMsg("XMPPError:" + e.getXMPPError().getCode() + "\n"
//					+ e.getXMPPError().getMessage());
			// muc失败应该将muc置空
			muc = null;
			return e.getXMPPError().getCode();
		} catch (Exception e) {
			// 其他异常
			e.printStackTrace();
		}
		return -1;
	}
	
	
	/**
	 * 离开聊天室，如果没有加入则do nothing
	 * */
	public void leaveChatroom(){
		if(!isValid(muc)){
//			clearDelayMsg(Type.MUC);
			muc.leave();
		}
		else{
			Util.showErrMsg("MUC NOT Login!");
		}
	}

	/**
	 * 输出聊天室的相关信息：subject,nickname,room name
	 * 
	 * @param writer
	 *            　输出流
	 * */
	public void showChatroomInfo(BufferedWriter writer) {
		if(writer==null){
			Util.showErrMsg("showChatRoomInfo:参数不正确");
			return ;
		}
		try {
			if (!isValid(muc)) {
				writer.append("You have not joined a chatroom");
				writer.flush();
				return;
			}
			String info = getFormatChatroomInfo();
			Util.showDebugMsg("chatroom info:\n" + info);
			writer.append(info);
			writer.flush();
		} catch (IOException e) {
			// TODO showChatroomInfo的输出流异常
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * ConversationManager中的功能方法：判断muc是否有效
	 * @return <code>true</code>已登录
	 * */
	private boolean isValid(MultiUserChat muc) {
		return muc != null && muc.isJoined();
	}
	

	/**
	 * 取得muc的相关信息，并格式化为String实例</br> 如果muc无效则输出空字符串
	 * */
	private String getFormatChatroomInfo() {
		if (!isValid(muc))
			return "";
		String info = "Chatroom name: " + muc.getRoom() + "\nSubject:"
				+ muc.getSubject() + "\nYour nickname:" + muc.getNickname()
				+ "\n";
		return info;
	}

	/**
	 * 发送群聊信息--若没有登录返回false
	 * 
	 * @param mucAddr
	 *            完整的muc地址
	 * @param msg
	 *            发送的信息
	 * @return 发送信息是否成功
	 * */
	public boolean sendMUCMsg(String mucAddr, String msg) {
		if (!isValid(muc)) {
			return false;
		}
		// 此处必须要加入判断是否为null，因为可能没有成功加入聊天室
		if (isValid(muc)) {
			try {
				muc.sendMessage(msg);
				return true;
			} catch (XMPPException e) {
				// 发送不成功
				Util.showErrMsg("XMPPError:" + e.getXMPPError().getCode());						
			} catch (Exception e) {
				e.printStackTrace();
				Util.showErrMsg("发送不成功");
			}
		}
		return false;
	}

	/**
	 * 检测JID是否有效
	 * */
	private boolean isValidJID(String jid) {
		// TODO 未完成：检测JID参数是否有效
		if(jid==null || jid.equals(""))
			return false;
		return true;
	}

	/**
	 * 写入聊天记录</br> 自己处理IO异常。
	 * 
	 * @param str
	 *            写入的数据
	 * @deprecated 用统一的StorageManager负责持久存储
	 * */
	private void writeLog(String str) {
		if (writer == null) {
			try {
				writer = new BufferedWriter(new FileWriter(outFile));
			} catch (IOException e) {
				// 初始化异常
				e.printStackTrace();
				Util.showErrMsg("writer初始化错误,重定向到System.out");
				// 将writer重定向为System.out
				writer = new BufferedWriter(new OutputStreamWriter(System.out));
			}
		}
		try {
			writer.append(str);
			writer.flush();
		} catch (IOException e) {
			// 
			e.printStackTrace();
			Util.showErrMsg("writer写入异常！");
		}
	}

	/**
	 * 实现了ChatManagerListener接口</br> 用来为每个chat添加一个chatListener，从而保存聊天记录。
	 * */
	private class ChatListener implements ChatManagerListener {
		@Override
		/**
		 * 工作：输出信息，添加接口
		 * */
		public void chatCreated(Chat chat, boolean createdLocaly) {
			// TODO 未实现：监听所有的chat会话，实现时应该将所有的chat都保存到持久存储单元
			// 持久存储单元：可以是一个sqlite，可以是文本集，或者是server端
			// 目前先考察其动作，使用Util.showDebugMsg
//			Util.showDebugMsg(getChatInfo(chat, createdLocaly));
			// 添加ChatListener接口
			chat.addMessageListener(new IncomingMsgMonitor());
		}

		/**
		 * 由<code>ChatManagerListener</code>中要实现的方法<code>chatCreated</code> 调用
		 * 
		 * @param chat
		 *            chatCreated参数chat
		 * @param islocal
		 *            chatCreated参数islocal
		 * */
		private String getChatInfo(Chat chat, boolean islocal) {
			String info = "#LOG#:Chat Created.\n";
			info += (islocal ? "Send to" : "Recv from") + "  ";
			info += chat.getParticipant() + "\n";
			return info;
		}

	}

	/**
	 * 实现了MessageListener,从而对<strong>所有</strong>chat信息的记录
	 * 
	 * */
	private class IncomingMsgMonitor implements MessageListener {

		@Override
		public void processMessage(Chat chat, Message msg) {
			if (msg.getBody() != null) {
				//收到了新消息
				addIncomingMsgs(msg);
			}
		}

	}

	/**
	 * // * 所有送出的Msg的监听
	 * */
	private class OutgoingMsgMonitor implements PacketInterceptor {

		@Override
		public void interceptPacket(Packet packet) {
			// Util.showDebugMsg("#TEST#OutgoingMsgMonitor");
			// 试探是否有PacketExtension
			// Collection<PacketExtension> pktExtCol = packet.getExtensions();
			// Util.showDebugMsg("PacketExt:");
			// for(PacketExtension ext:pktExtCol){
			// Util.showDebugMsg("namespace:\t" + ext.getNamespace());
			// }
			// //试探一下是否具有Property
			// Collection<String> StrCollection = packet.getPropertyNames();
			// Util.showDebugMsg("Packet Property:");
			// for(String str : StrCollection){
			// Util.showDebugMsg(str);
			// }
			Message msg = (Message) packet;
			if (msg.getBody() != null) {
//				msgStorageManager.store(msg);//被
				
			}
			// Util.showDebugMsg("Body:\t" + msg.getBody());
			// Util.showDebugMsg("Thread ID:\t" + msg.getThread());
			// Util.showDebugMsg("Subject:\t" + msg.getSubject());
			// String toStoreInfo = "Type:"
		}

	}

	/**
	 * MUC的接收消息的PacketListener，<strong>可以接收到所有的（包括自己和历史消息）的信息</strong></br>
	 * 此方法还执行存储动作。
	 * */
	private class AllMUCMsgListener implements PacketListener {
		/**
		 * 构造函数：必须要给定一个输出流，而且显示指定debug开关
		 * 
		 * @param writer
		 *            指定的信息的输出流
		 * @param debug
		 *            是否开启debug
		 * */
		BufferedWriter writer;
		boolean debug;

		// TODO 未实现：debug的开关功能未实现
		private AllMUCMsgListener(BufferedWriter writer, boolean debug) {
			super();
			this.writer = writer;
			this.debug = debug;
		}

		@Override
		public void processPacket(Packet packet) {
			// Util.showPacketInfo("Received MUC INCOMING PACKET", packet);
			// Util.showMsgInfo("Received MUC INCOMING MSG",(Message)packet);
			// DelayInfo delayInfo = (DelayInfo)packet.getExtension("delay",
			// "urn:xmpp:delay");
			// if(delayInfo != null){
			// Util.showDebugMsg("Find DelayInfo!");
			// Util.showDebugMsg("Time stamp:\t" + delayInfo.getStamp());
			// }
			// if(packet.getFrom().contains(defaultNickname)){
			// Util.showDebugMsg("这是自己发的消息！");
			// }
			// 首先接收所有的历史消息
			Message msg = (Message) packet;
			//检测是否会出现debug信息
			if(packet.getError() != null){
				Util.showErrMsg("#MUC#:FIND XMPPError!");
			}
			
//			if(msg.getBody() != null)
//			if (isDelayMsg(msg)) {
//				// 此处可能会出现bug，因为在add前没有检验mucHistoryMsgs是否可用，因此解决方法之一就是保证mucHistoryMsgs始终可用！
//				mucHistoryMsgs.add(msg);
//			} else {// 没有delay msg了
//				handleDelayMsgs(Type.MUC, true, new BufferedWriter(
//						new OutputStreamWriter(System.out)));
//				if (isOthersMsg(msg)) {
//					String mucName = "聊天室[" + muc.getRoom() + "]";
//					Util.showDebugMsg(mucName + ":\t" + msg.getBody());
//				}
//			}
			//2011-5-26
			if(isOthersMsg(msg)&&msg.getBody()!=null){
				addIncomingMsgs(msg, muc.getRoom());
			}
		}
	}

	/**
	 * @deprecated 不区分是否为delayMsg了
	 * 判断该msg是否为delay msg
	 * 
	 * */
	private boolean isDelayMsg(Message msg) {
		DelayInfo delayInfo = (DelayInfo) msg.getExtension("delay",
				"urn:xmpp:delay");
		if (delayInfo == null)
			return false;
		return true;
	}
	

	/**
	 * 更新HashMap:将未读信息添加到hashmap
	 * */
	private void addIncomingMsgs(Message msg) {
		addIncomingMsgs(msg, msg.getFrom());
	}
	//根据指定的id增加信息
	private void addIncomingMsgs(Message msg,String id){
//		Util.showDebugMsg("adding incoming msg!!");
		CopyOnWriteArrayList<Message> list;
		if(!unreadMsgs.containsKey(id)){
			list = new CopyOnWriteArrayList<Message>();
			list.add(msg);
			unreadMsgs.put(id, list);
		}
		else{
			list = unreadMsgs.get(msg.getFrom());
			list.add(msg);
		}
		
	}

	/**
	 * 判断该msg是否是自己发送出去的信息，方法：getFrom中是否contains nickname,或者自己的jid
	 * */
	private boolean isOthersMsg(Message msg) {
		//
		String fromAddr = msg.getFrom();
		return !(fromAddr.contains(mucNickname) || fromAddr.contains(connection
				.getUser()));
	}

//	/**
//	 * @deprecated 已经被未读消息机制取代
//	 * 处理delayMsg:添加到incomingMsg，然后清空</br> 如果已经清空，则do nothing!
//	 * 5-26更新
//	 * */
//	private void handleDelayMsgs(Type type, boolean showMsg,
//			BufferedWriter writer) {
//		if (showMsg) {
//			showDelayMsgs(type, writer);
////			showDelayMsgsOnList(type);
//		}
//		clearDelayMsg(type);
//	}
//	/**
//	 * 将delay msg增加到incomingMsgs里面
//	 * */
//	private void showDelayMsgsOnList(Type type) {
//		if(type == Type.MUC){
//			String roomName = muc.getRoom();
//			
//			for(Message msg:mucHistoryMsgs){
////				addIncomingMsgs(msg, roomName);
//			}
//		}
//		else if(type == Type.CHAT){
//			//TODO 未实现
//			
//		}
//		else{
//			Util.showErrMsg("showDelayMsgsOnList:类型不支持！");
//		}
//		
//	}

//	/**
//	 * 清空未读的信息，该方法<strong>没有存储动作！</strong>
//	 * */
//	private void clearDelayMsg(Type type) {
//		if(chatHistoryMsgs == null || mucHistoryMsgs == null){
//			Util.showErrMsg("HistoryMsgs is NULL!");
//		}
//		if (type == Type.CHAT) {
//			chatHistoryMsgs.clear();
//		} else if (type == Type.MUC) {
//			for (Message msgToStore : mucHistoryMsgs) {
//				//MUC的存储delay msg在接收时必须执行，此时不执行存储
////				if (msgToStore.getBody() != null)
////					msgStorageManager.store(msgToStore);
//			}
//			mucHistoryMsgs.clear();
//		} else {
//			Util.showErrMsg("未知类型！");
//		}
//	}

//	/**
//	 * 在writer上输出delayMsg 若没有delayMsg则do nothing
//	 * */
//	private void showDelayMsgs(Type type, BufferedWriter writer) {
//		if (!hasDelayMsgs(type)) {
//			return;
//		}
//		Collection<Message> delayMsgs;
//		try {
//			if (type == Type.MUC) {
//				writer.append("\nMUC DELAY MESSAGES:");
//				delayMsgs = mucHistoryMsgs;
//			} else {
//				writer.append("\nCHAT DELAY MESSAGE:");
//				delayMsgs = chatHistoryMsgs;
//			}
//			for (Message msg : delayMsgs) {
//				writer.append("\nFrom:\t" + msg.getFrom() + "\nSubject:\t"
//						+ (msg.getSubject() == null ? "无" : msg.getSubject())
//						+ "\nBody:\t" + msg.getBody());
//			}
//			writer.flush();
//		} catch (IOException e) {
//			// TODO 未处理，IO错误
//			e.printStackTrace();
//		}
//	}

//	/**
//	 * delayMsg是否存在
//	 * */
//	private boolean hasDelayMsgs(Type type) {
//		return type == Type.MUC ? (mucHistoryMsgs != null && !mucHistoryMsgs
//				.isEmpty()) : (chatHistoryMsgs != null && !chatHistoryMsgs
//				.isEmpty());
//	}
	
	/**
	 * 对外的接口，用于查询是否有incoming msg.
	 * */
	public boolean hasUnreadMsg(){
		return !unreadMsgs.isEmpty();
	}
	
	/**
	 * *注意*该方法的副作用：清空unreadMsgs！
	 * @return 如果没有未读信息则返回null
	 * */
	public Map<String,CopyOnWriteArrayList<Message>> retrieveIncomingMsg(){
		if(!unreadMsgs.isEmpty()){
			Map<String,CopyOnWriteArrayList<Message>> copyData = new ConcurrentHashMap<String, CopyOnWriteArrayList<Message> >(unreadMsgs);
			unreadMsgs.clear();
			return copyData;
		}
		return null; 
	}
	
	/**
	 * 检查参数的内部功能函数
	 * */
	private boolean checkParam(String str){
		return str!=null&&!str.equals("");
	}

}
