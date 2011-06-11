package com.liuyix.xmpp;

//import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;

import com.liuyix.xmpp.ConversationManager.Type; //import org.jivesoftware.smack.packet.IQ;
import com.liuyix.xmpp.ui.LoginListener;
import com.liuyix.xmpp.ui.LoginWindow;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.Packet;
//import org.jivesoftware.smack.packet.PacketExtension;
//import org.jivesoftware.smack.packet.Presence;
//import org.jivesoftware.smack.packet.PrivacyItem;
//import org.jivesoftware.smack.packet.Registration;
//import org.jivesoftware.smack.packet.XMPPError;
//import org.jivesoftware.smack.packet.Presence.Type;
//import org.jivesoftware.smackx.Form;
//import org.jivesoftware.smackx.FormField;
//import org.jivesoftware.smackx.ServiceDiscoveryManager;
//import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
//import org.jivesoftware.smackx.filetransfer.FileTransfer;
//import org.jivesoftware.smackx.filetransfer.FileTransferListener;
//import org.jivesoftware.smackx.filetransfer.FileTransferManager;
//import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
//import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
//import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

/**
 * 该类主要的功能是得到一个<strong>可用的connection</strong>，从而可以调用其他类
 * 
 * @author cnliuyix
 * 
 */
public class Starter {

	// 非常重要的成员
	Connection connection;
	String username;
	String password;
	// 基本功能的实例
	RosterManager rosterManager;
	ConversationManager conversation;
	Scanner scanner = null;
	String mucAddr;
	LoginWindow loginWindow;
	/**
	 * 配置socks5代理
	 * 
	 * @param name
	 *            帐号
	 * @param pass
	 *            密码
	 * */
	public Starter(String name, String pass) {
		this.username = name;
		this.password = pass;
		// Util.setUsername(name);
		SmackConfiguration.setLocalSocks5ProxyPort(7776);
		SmackConfiguration.setLocalSocks5ProxyEnabled(true);

	}

	/**
	 * 无帐号密码的启动
	 * */
	public Starter() {
		this("", "");
	}

	/**
	 * 启动客户端,程序的主要逻辑
	 * 
	 * @param debug_enable
	 *            true则启动debug
	 * @param host
	 *            主机地址
	 * */
	private void run(boolean debug_enable, String host) {
		try {
			boolean hasLogined = login(debug_enable, host);
			if (hasLogined) {
				// ==========已经成功登录===============//

				Util.setUsername(connection.getUser());
				rosterManager = new RosterManager(connection);
				conversation = new ConversationManager(connection);
				String inputMsg = "";
				Writer writer = new BufferedWriter(new OutputStreamWriter(
						System.out));
				showOptions(writer);
				boolean toQuit = false;
				while (!toQuit) {
					try {
						inputMsg = getInput(false);
						int option = Integer.valueOf(inputMsg);
						switch (option) {
						case 1:
							showOptions(writer);
							break;
						case 2:
							showRosterInfo_debug();
							break;
						case 3:
							chatWithJid();
							break;
						case 4:
							joinChatroom();
							break;
						case 5:
							msg2Chatroom();
							break;
						case 6:
							queryJid();
							break;
						case 7:
							fileTransfer();
							break;
						case 0:
							showUnreadMsgs();
							break;
						case 999:
							toQuit = true;
							break;
						default:
							Util.showErrMsg("未知命令！");
						}
					} catch (NumberFormatException e) {
						// e.printStackTrace();
						System.out.println("未知命令！请重试");
					} catch (Exception e) {
						// 截获所有的异常
						e.printStackTrace();
					}
				}
				// showRosterInfo_debug();
				// 显示在线用户的JID--成功
				// rosterManager.showOnlineUser(new BufferedWriter(new
				// OutputStreamWriter(System.out)));
				// addFriend_debug("admin",3);
				// 开始聊天
				// String debug_jid = "jack@localhost";
				// mkChat_unchecked(debug_jid);
				// String muc_addr = "muc1@conference.localhost";
				// String nickname = username;
				// conversation.joinChatroomByAddr("mu@conference.localhost",
				// username);
				// conversation.showChatroomInfo(new BufferedWriter(new
				// OutputStreamWriter(System.out)));
				// conversation.sendMUCMsg("Hello,World!");
				// 将chat和muc调整为统一的接口。
				// talk(muc_addr,ConversationManager.Type.MUC);

				// //============所有操作结束=============//
				// getInput();
			} else {
				Util.showErrMsg("Login failed.The program quit.");
			}
		} catch (XMPPException e) {
			// 
			e.printStackTrace();
		} finally {
			System.err.println("Programming is quiting...");
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}
		}
		System.exit(0);

	}
	
	private void fileTransfer() {
		
		String jid = getInput("输入用户完整jid", false);
		// 该文件传输可行，但是要注意必须是完整的JID才可以！
		try{
		 new TransferManager(connection,null).send(jid,new File("/home/cnliuyix/music/xmpp.mp3"));
		}catch(XMPPException e){
			e.printStackTrace();
		}
		
	}

	//查询用户jid对应的用户
	private void queryJid() {
		String jid = getInput("输入要查询的jid:",false);
		System.out.println(Util.getUsername(jid));
	}

	/**
	 * 显示未读信息
	 * */
	private void showUnreadMsgs() {
		Map<String, CopyOnWriteArrayList<Message>> unreadMsgs = null;
		if (conversation.hasUnreadMsg()) {
			unreadMsgs = conversation.retrieveIncomingMsg();
			for (Map.Entry<String, CopyOnWriteArrayList<Message>> map : unreadMsgs
					.entrySet()) {
				String jid = map.getKey();
				List<Message> list = map.getValue();
				if (!list.isEmpty()) {
					System.out.println(jid + ":");
					for (Message msg : list) {
						if (msg.getBody() != null)
							System.out.println(msg.getBody());
					}
				}
			}
		} else {
			System.out.println("未读消息为空！");
		}

	}

	/**
	 * 向聊天室发送信息
	 * */
	private void msg2Chatroom() {
		if (mucAddr != "") {
			talk(mucAddr, Type.MUC);
		} else {
			Util.showErrMsg("操作错误：没有登录？");
		}
	}

	/**
	 * 交互登录chatroom
	 * */
	private void joinChatroom() {
		String addr = getInput("输入聊天室地址", false);
		String nickName = getInput("昵称:(可为空)", true);
		if (nickName.equals("")) {
			nickName = username;
		}
		int errcode = conversation.joinChatroomByAddr(addr, nickName);
		if (errcode == 0) {
			System.out.println("加入聊天室成功！");
			mucAddr = addr;
		} else {
			mucAddr = "";
			if (errcode == -1) {
				System.out.println("错误原因未知");
			} else {
				System.out.println("错误代码：" + errcode);
			}
		}

	}

	/**
	 * 交互的chat
	 * */
	private void chatWithJid() {
		String addr = getInput("发信人地址:", false);
		talk(addr, Type.CHAT);
	}

	/**
	 * 公共的功能类，用于从标准IO获得输入
	 * 
	 * @param acceptNull
	 *            是否允许空输入
	 * @return 标准输入得到的字符串
	 * */
	private String getInput(boolean acceptNull) {
		if(scanner == null)
			scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		while (input == null || input.equals("") && !acceptNull) {
			System.out.println("输入为空！重试！");
			input = scanner.nextLine();
		}
		return input;
	}

	private String getInput(String msg, boolean acceptNull) {
		System.out.print(msg);
		return getInput(acceptNull);
	}

	/**
	 * 展示功能，纯显示功能。
	 * 
	 * @param writer
	 * */
	private void showOptions(Writer writer) {
		try {
			writer.append("\n请选择功能:" + "\n1.显示功能列表" + "\n2.显示roster"
					+ "\n3.发送chat" + "\n4.登录聊天室" + "\n5.发送聊天室信息" + "\n6.qureyJid" +"\n0.阅读未读信息"
					+ "\n999.退出程序\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 完成登录的操作
	 * 
	 * @throws XMPPException
	 *             登录时遇到的XMPP error
	 * */
	private boolean login(boolean debugEnable, String host)
			throws XMPPException {
		if (debugEnable) {
			Connection.DEBUG_ENABLED = true;
		} else
			Connection.DEBUG_ENABLED = false;
		ConnectionConfiguration connConfig = new ConnectionConfiguration(host);
		connection = new XMPPConnection(connConfig);
		connection.connect();
		// 服务器登录成功
		if (connection != null) {
			if (username == "") {
				Util.showDebugMsg("Registering...");
				register(System.in, System.out);
			}
			/* 判断是否已经登录成功 */
			while (connection.isAuthenticated() != true) {
				boolean needToRegister = !userLogin();
				if (needToRegister) {
					// 输入为空，询问是否需要注册
					String answer = getInput("需要注册?", false);
					if (answer == "y" && answer == "Y") {
						register(System.in, System.out);
					} else
						break;
				}
			}
		}
		return connection.isAuthenticated();
	}

	/**
	 * 文字聊天的统一接口，可以进行单人chat，或者muc
	 * 
	 * @param jid
	 *            对方的地址，一个单人的JID，或者是一个MUC的JID
	 * @param talkType
	 *            talk类型，chat或者muc
	 * */
	private void talk(String jid, ConversationManager.Type talkType) {
		// TODO 没有检查jid是否正确
		if (talkType != Type.CHAT && talkType != Type.MUC) {
			Util.showErrMsg("talk@Starter,Type类型不支持");
			return;
		}
		if (conversation == null)
			conversation = new ConversationManager(connection);
		String toSendMsg = "";
		while (true) {
			// 每个String不同，必须使用Object.equals方法
			toSendMsg = getInput("我说:", true);
			// System.out.println("\n");
			if (talkType == Type.CHAT)
				conversation.sendMsg(jid, toSendMsg);
			else {
				// 发送信息到指定聊天室
				conversation.sendMUCMsg(jid, toSendMsg);
			}
			if (toSendMsg.equals(new String("\\q"))) {
				Util.showDebugMsg("talk end.");
				return;
			}
		}

	}

	/**
	 * 注册method
	 * 
	 * @param in
	 *            注册信息的输入流
	 * @param out
	 *            注册信息提示内容的输出流
	 * @return boolean 注册是否成功
	 * */
	private void register(InputStream in, PrintStream out) {
		// 
		Register reg = new Register(connection);
		reg.registerHandler(
				new BufferedReader(new InputStreamReader(System.in)),
				new BufferedWriter(new OutputStreamWriter(System.out)));
	}

	/**
	 * 用户登录：通过标准IO输入帐号信息
	 * 
	 * @param in
	 *            输入流（登录信息的获取）
	 * @param out
	 *            反馈信息流（登录失败的信息反馈）
	 * @return 若输入为空返回false
	 * @throws XMPPException
	 *             登录错误抛出XMPPException
	 * */
	private boolean userLogin(InputStream in, PrintStream out)
			throws XMPPException {
		out.println("输入用户名和密码");
		this.username = getInput(false);
		this.password = getInput(false);
		// 判断输入是否为空
		if (username == "" || password == "") {
			return false;
		} else {
			connection.login(username, password);
			return true;
		}
	}

	/**
	 * 用户登录操作</br> 有username,password直接登录，没有则在标准IO输入
	 * 
	 * @return 若输入为空则返回false
	 * @throws XMPPException
	 *             登录错误抛出XMPPException
	 * */
	private boolean userLogin() throws XMPPException {
		if (username != "" && password != "") {
			connection.login(username, password);
			return true;
		} else
			return userLogin(System.in, System.out);
	}

	/**
	 * 测试聊天功能
	 * 
	 * @param jid
	 *            JID
	 * @deprecated 使用统一的talk
	 * */
	private void mkChat_unchecked(String jid) {
		if (conversation == null)
			conversation = new ConversationManager(connection);
		String toSendMsg = "";
		while (true) {
			// 每个String不同，必须使用Object.equals方法
			toSendMsg = getInput(true);
			conversation.sendMsg(jid, toSendMsg);
			if (toSendMsg.equals(new String("."))) {
				Util.showDebugMsg("chat end.");
				return;
			}
		}
	}

	/**
	 * 简化的启动客户端方法，默认server地址为localhost
	 * 
	 * @param debug_enable
	 *            true则debug
	 * */
	private void run(boolean debug_enable) {
		run(debug_enable, "localhost");
	}

	/**
	 * 显示好友列表
	 * */
	private void showRosterInfo_debug() {
		rosterManager.showRosterInfo(new BufferedWriter(new OutputStreamWriter(
				System.out)));
		// 调用的是rosterManger的方法
		// rosterManager.showRosterInfo(null);
	}

	/**
	 * 添加好友的*测试*操作：新建Friend_[1-groupCnt]分组,
	 * 
	 * @param name
	 *            用户的名字，无需加localhost
	 * @param groupCnt
	 *            有自动添加的分组
	 * */
	// TODO 该方法为测试方法，有许多magic number
	private void addFriend_debug(String name, int groupCnt) {
		// 添加好友,需要3个参数：显示的用户名称，用户名，要加入的用户组
		String userAddr = name + "@localhost";
		String userNickname = name;
		String[] groups = new String[groupCnt];
		for (int i = 0; i < groupCnt; ++i) {
			groups[i] = "Friend_" + i;
		}
		rosterManager.addFriend(userAddr, userNickname, groups);
	}

	public static void main(String[] args) {
		// 
		new Starter("regTest4", "regTest4").run(true, "localhost");
//		try {
//			LoginWindow loginWindow = new LoginWindow();
//			loginWindow.addLoginListener(new LoginListener(){
//
//				@Override
//				public void handleLoginInfo(String name, String passwd,
//						String server) {
//					//
//					
//				}
//				
//			});
//			loginWindow.open();
//			
//		} catch (Exception e) {
//			//
//			e.printStackTrace();
//		}
		// 服务发现
		// 调出discoMangager
		// ServiceDiscoveryManager discoManager =
		// ServiceDiscoveryManager.getInstanceFor(conn);
		// DiscoverInfo discoInfo =
		// discoManager.discoverInfo("jack@localhost/hello");
		//			
		// Iterator<DiscoverInfo.Identity> discoIdItr =
		// discoInfo.getIdentities();
		// while(discoIdItr.hasNext()){
		// DiscoverInfo.Identity discoId = discoIdItr.next();
		// Util.showDebugMsg("ID Name:" + discoId.getName());
		// Util.showDebugMsg("ID Type:" + discoId.getType());
		// Util.showDebugMsg("ID Category:" + discoId.getCategory());
		// }
		// Iterator<DiscoverInfo.Feature> discoFeatureItr =
		// discoInfo.getFeatures();
		// while(discoFeatureItr.hasNext()){
		// DiscoverInfo.Feature feature = discoFeatureItr.next();
		// Util.showDebugMsg("Feature:" + feature.getVar());
		// }
		// fileTransferManager.addFileTransferListener(new
		// FileTransferListener() {
		//				
		// @Override
		// public void fileTransferRequest(FileTransferRequest request) {
		//					
		// IncomingFileTransfer ifTransfer = request.accept();
		// try {
		// ifTransfer.recieveFile();
		// } catch (XMPPException e) {
		//						
		// e.printStackTrace();
		// }
		// }
		// });
		//			
		//			

		// //PrivacyList的使用
		// //第一步是取得PrivacyListManager实例
		// PrivacyListManager privacyListManager =
		// PrivacyListManager.getInstanceFor(conn);
		// //新的的privacylist名字
		// String newListName = "firstList";
		// //删除Privacylist
		// privacyListManager.deletePrivacyList(newListName);
		// //创建一个PrivacyItem--PrivacyItem为规则的抽象，表示一则规则（可以实现按不同规则屏蔽或者放行）
		// PrivacyItem pitem = new PrivacyItem("jid",false,10);
		// pitem.setValue("jack@localhost");
		// //将规则放入一个List集合作为参数传给PrivacyListManager
		// ArrayList<PrivacyItem> pItemList = new ArrayList<PrivacyItem>();
		// Util.showDebugMsg("Privacy Item:\n" + pitem.toXML());
		// pItemList.add(pitem);
		// //PrivacListManger将新建的PrivacyItemList传给server
		// privacyListManager.createPrivacyList(newListName, pItemList);
		// //从server端同步PrivacyList
		// privacyListManager.setDefaultListName(newListName);
		// //PrivacyLists的遍历
		// int privacyListLength = 0;
		// PrivacyList[] privacyList = privacyListManager.getPrivacyLists();
		// for(int i=0;i<privacyList.length;++i){
		// Util.showDebugMsg("PrivacyList[" + i + "]\t");
		// List<PrivacyItem> plist_tmp = privacyList[i].getItems();
		// for(int j=0;j<plist_tmp.size();++j){
		// Util.showDebugMsg(plist_tmp.get(j).getType() + "\t" +
		// plist_tmp.get(j).getValue());
		// }
		// }
		// //Default PrivacyList
		// //在获得defaultList之前必须确定存在defaultList，否则会出现"item-not-found"的提示
		// PrivacyList defaultList = privacyListManager.getDefaultList();
		// Util.showDebugMsg("DefaultList:\t" + defaultList.toString());
		// //修改DefaultList
		// PrivacyItem newPrivacyItem = new PrivacyItem("jid",false,1);
		// newPrivacyItem.setValue("jack@localhost");
		// newPrivacyItem.setFilterPresence_out(true);
		// newPrivacyItem.setFilterMessage(false);
		// Util.showDebugMsg("newPrivacyItem:\t" + newPrivacyItem.toXML());
		// defaultList.getItems().add(newPrivacyItem);
		//			
		//			
		// privacyListManager.setDefaultListName(newListName);

		// 登录成功后进行会话聊天，方法是新建chat--必须实现的是一个MessageListener用来对收到的信息接受
		// final Chat chat = conn.getChatManager().createChat(friendAddr, new
		// MessageListener(){
		// // final Chat chat =
		// conn.getChatManager().createChat("invalid@localhost", new
		// MessageListener(){
		//
		// @Override
		// public void processMessage(Chat chat, Message msg) {
		//					
		// //要先检查是否有错误！
		// XMPPError error = msg.getError();
		// if(error!=null){
		// System.err.println("Chat Errorcode:\t" + error.getCode());
		// System.err.println("Chat Msg:\t" + error.getMessage());
		// System.err.println("Chat Condition:\t" + error.getCondition());
		// System.err.println("Chat Type:\t" + error.getType());
		// }
		// //对收到的信息输出到标准输出
		// else
		// Util.showDebugMsg(friendAddr + ":\t" + msg.getBody());
		// }
		//				
		// });
		// // chat.sendMessage("This is a text from mick!");
		// //之后是从标准输入输入数据
		// while(true){
		// String msg = getInput();
		// if(msg.equals(new String("/p")))
		// //改变在线状态，使用的Presence方法
		// conn.sendPacket(new Presence(Type.available, "WOW!!",
		// 1,Presence.Mode.available));
		// else if(msg.equals(new String("/q"))){
		// // privacyListManager.declineDefaultList();
		// }
		// else if(msg.equals(new String("/f"))){
		// //发文件
		// OutgoingFileTransfer outFileTransfer =
		// fileTransferManager.createOutgoingFileTransfer(friendAddr);
		// outFileTransfer.sendFile(new File("/home/cnliuyix/music/xmpp.mp3"),
		// "music!");
		// FileTransfer.Status status = null;
		// FileTransfer.Status previousStatus = null;
		// while(true){
		// previousStatus = status;
		// status = outFileTransfer.getStatus();
		// if(previousStatus != status)
		// Util.showDebugMsg("Status:\t" + status.toString());
		// if(status == FileTransfer.Status.initial
		// || status == FileTransfer.Status.in_progress
		// || status == FileTransfer.Status.negotiated
		// || status == FileTransfer.Status.negotiating_stream
		// || status == FileTransfer.Status.negotiating_transfer
		// )
		// continue;
		// else{
		// if(status == FileTransfer.Status.error){
		// // Util.showDebugMsg("Errormsg:\t" + outFileTransfer.getError());
		// FileTransfer.Error fileTransferErr = outFileTransfer.getError();
		// if(fileTransferErr != null){
		// Util.showDebugMsg("Errormsg:\t" + fileTransferErr);
		// }
		// else{
		// Util.showDebugMsg("Errormsg NULL");
		// }
		// }
		// break;
		// }
		// }
		// }
		// chat.sendMessage(msg);
		// }

	}

}
