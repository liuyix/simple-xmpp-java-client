/**
 * GUI程序的总控制程序
 */
package com.liuyix.xmpp;


import java.io.File;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Message.Type;

import com.liuyix.xmpp.ui.ChatWindow;
import com.liuyix.xmpp.ui.LoginListener;
import com.liuyix.xmpp.ui.LoginWindow;
import com.liuyix.xmpp.ui.MainWindow;

/**
 * @author cnliuyix
 * 
 */
public class GuiStarter implements IncomingMsgListener,OutgoingMsgListener,ChatRequestListener,IncomingFileReqListener{
	static Log log = LogFactory.getLog(GuiStarter.class);
	private boolean enableDebug = true;
	// 非常重要的成员
	Connection connection;
	String username;
	String password;
	// 基本功能的实例
	RosterManager rosterManager;
	ConversationManager conversation;
	PresenceManager presenceManager;
	TransferManager transferManager;
	String mucAddr;
	LoginWindow loginWindow;
	static Map<String, ChatWindow> chatWindowMap;// key:username,value:对应的chatWindow
	Collection<Message> incomingMsgCollection;
	boolean isQuit = false;
	private Shell topShell;
	private boolean hasFileTransferRequest;
	
	//FIXME 简单实现一个传输文件对话框
	private String fileTransferUsername;
	private String fileTransferFilename;
	private long filesize;
	private File toSaveLocation;

	public static void main(String[] args) {
		new GuiStarter(true).run();
	}
	
	//TODO dirty hack
	public static void deleteChatWindow(String username){
//		log.debug("");
		if(chatWindowMap.containsKey(username)){
			chatWindowMap.remove(username);
		}
	}

	public GuiStarter(boolean debug) {
//		log.debug("");
		enableDebug = debug;
		chatWindowMap = new java.util.concurrent.ConcurrentHashMap<String, ChatWindow>();
		incomingMsgCollection = new java.util.concurrent.CopyOnWriteArrayList<Message>();
		hasFileTransferRequest = false;
	}

	private void run() {
		try {
			// TODO 添加非debug的形式
			// Display.getDefault().
			LoginWindow loginWindow = new LoginWindow(true, "mick", "mick",
					"localhost");
			loginWindow.addLoginListener(new LoginHandler(loginWindow));
//			log.info("call loginWindow");
			loginWindow.open();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 已经成功登录
		Util.setUsername(connection.getUser());
		rosterManager = new RosterManager(connection);
		conversation = new ConversationManager(connection);
		conversation.setIncomingMsgListener(this);
		presenceManager = new PresenceManager(connection);
		transferManager = new TransferManager(connection, this);
//		MainWindow mainWindow = new MainWindow(connection.getUser(), username,
//				rosterManager.getRoster(),this);
		Display.getDefault().syncExec(new MainWindowBuilder(this));
		
//		shell = mainWindow.open();
		Display display = Display.getDefault();
		//多线程问题产生点
		while (topShell!=null&&!topShell.isDisposed()) {
			
//			if (!display.readAndDispatch()&&incomingMsgCollection.isEmpty()&&hasFileTransferRequest==false) {
////				log.debug("mainWindow:sleep");
//				display.sleep();
//			}			
//			else if(incomingMsgCollection.isEmpty()!=true){
//				for (Message msg : incomingMsgCollection) {
//					handleChat(msg);
//				}
//				incomingMsgCollection.clear();
//			}
//			else if(hasFileTransferRequest){
//				createTransferQueryDialog(fileTransferUsername, fileTransferFilename,filesize);
//				hasFileTransferRequest = false;
//			}
			if(!display.readAndDispatch()){
				display.sleep();
			}
		}
		
//		Socks5Proxy.getSocks5Proxy().stop();		
//		Socks5BytestreamManager.getBytestreamManager(connection).disableService();

		connection.disconnect();
		System.exit(0);
//		closeApp();
		return;
	}

//	private void closeApp() {
//		this.connection.disconnect();
//	}

	/**
	 * 登录服务器,不操作用户登录
	 * 
	 * @throws XMPPException
	 *             登录时遇到的XMPP error
	 * @return 登录服务器是否成功
	 * */
	private boolean enterServ(boolean debugEnable, String host)
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
				// TODO 未实现：注册界面
				// register(System.in, System.out);
			}
			/* 判断是否已经登录成功 */
			// while (connection.isAuthenticated() != true) {
			// //TODO 登录未成功的操作
			// boolean needToRegister = !userLogin();
			// if (needToRegister) {
			// // 输入为空，询问是否需要注册
			// String answer = getInput("需要注册?", false);
			// if (answer == "y" && answer == "Y") {
			// register(System.in, System.out);
			// } else
			// break;
			// }
			// }
		}
		// return connection.isAuthenticated();
		return connection.isConnected();

	}

	/**
	 * 登录处理程序
	 * 
	 * */
	private class LoginHandler implements LoginListener {
		LoginWindow window;

		public LoginHandler(LoginWindow loginWindow) {
			this.window = loginWindow;
		}

		@Override
		public void handleLoginInfo(String name, String passwd, String server,boolean debug) {
			// Util.showDebugMsg("\nname:" + name + "\npasswd:" + passwd +
			// "\nserver:" + server);
			enableDebug = debug;
			try {
				if (enterServ(enableDebug, server) != true) {
					// TODO 登录服务器未成功
					Util.printErrMsg("登录服务器未成功！");
				} else {
					// 登录服务器成功
					if (userLogin(name, passwd) != true) {
						// 用户登录不成功
					} else {// 用户登录成功
						username = name;
						System.out.println("用户登录成功！");
						window.close();

					}
				}
			} catch (XMPPException e) {
				// TODO 发生XMPP错误
				e.printStackTrace();
			}
		}

	}

	private boolean userLogin(String name, String passwd) {
		if (connection != null) {
			try {
				connection.login(name, passwd);
				if (connection.isAuthenticated())
					return true;
			} catch (XMPPException e) {
				// 登录失败 XMPP错误
				if (enableDebug)
					e.printStackTrace();
				else
					Util.showErrMsg("userLogin:XMPP ERROR:"
							+ e.getXMPPError().getCode());
			}
		}
		return false;
	}

	@Override
	// 当收到外部消息时会调用该方法
	public void handleIncomingMsg(Type type, Message msg) {
		if(msg.getBody() == null)
			return;
//		Util.showDebugMsg("GuiStater#processMsg" + "\tmsg type:"
//				+ msg.getType());		
		if (type == Type.chat) {
			// handleChat(msg);
//			incomingMsgCollection.add(msg);
//			Display.getDefault().wake();
			Display.getDefault().asyncExec(new CreateChatWindowRunner("TEST", "TEST-JID", new Presence(Presence.Type.unavailable), this));

		} else if (type == Type.error) {
			handleMsgError(msg);
		} else if (type == Type.groupchat) {
			handleGroupchat(msg);
		} else {
//			Util.showDebugMsg("processMessage#GuiStarter");
		}
	}

	private void handleGroupchat(Message msg) {
		// TODO 处理群组信息

	}

	private void handleMsgError(Message msg) {
		// TODO 处理错误信息

	}

	private void handleChat(Message msg) {
		String username = Util.getUsername(msg.getFrom());
		Util.showDebugMsg("\nhandleChat:" + "\nusername:" + username + "\n"
				+ msg.getBody());
		if (msg != null && msg.getBody() != null) {
			updateChatWindow(username, msg);
		}

	}

	/**
	 * 更新/建立 chatWindow的方法</br> 1.尝试查询是否有建立的chatWindow，若有则调用其中的方法更新
	 * 2.若没有ChatWindow，则调用createChatWindow <strong>msg一定不能为空</strong>
	 * */
	private void updateChatWindow(String username, Message msg) {
		if (msg == null || msg.getBody() == null)
			return;
		Util.showDebugMsg("\nupdateChatWindow:\nusername:" + username);
		ChatWindow chatWindow = chatWindowMap.get(username);
		if (chatWindow == null)
			createChatWindow(username, msg.getFrom());
		// 已经建立了一个聊天窗口，则调用该ChatWindow的处理接收消息的方法
		chatWindow = chatWindowMap.get(username);
		
//		Util.showDebugMsg("Msg:" + msg == null ? "NULL" : "NOT NULL");
//		Util.showDebugMsg("chatWindow:" + chatWindow==null?"NULL" : "NOT NULL");
		 chatWindow.handleIncomingMsg(msg.getBody());

	}
	

	/**
	 * 建立chatWindow的方法
	 * */
	private void createChatWindow(String username, String jid) {
		Util.showDebugMsg("createChatWindow....");
		Presence presence = presenceManager.getPresence(username);

		 //TODO 照片的传递
//		 ChatWindow chatWindow = new ChatWindow(username,jid,null,presence.getType(),
//		 presence.getMode(),presence.getStatus());
//		 chatWindowMap.put(username, chatWindow);
		 
//		Display.getCurrent().asyncExec(new Runnable() {
//
//			@Override
//			public void run() {
////				 chatWindow = new
////				 ChatWindow(username,jid,null,Presence.Type.available,
////				 Presence.Mode.available,"INFO");
//				ChatWindow chatWindow = new ChatWindow();
//				if (chatWindow == null)
//					Util.showErrMsg("creatingChatWindow failed!");
//
//				chatWindowMap.put("jack", chatWindow);
//				chatWindow.open();
//
//			}
//		});
		// Display.getDefault().asyncExec(null);
		// Display.getDefault().asyncExec(new
		Display.getDefault().asyncExec(new CreateChatWindowRunner(username,jid,presence,this));
	}

	/**
	 * 建立ChatWindow的线程
	 * */
	private class CreateChatWindowRunner implements Runnable {
		String username;
		String jid;
		Presence presence;
		OutgoingMsgListener listener;
		public CreateChatWindowRunner(String username, String jid,
				Presence presence,OutgoingMsgListener listener) {
			super();
			this.username = username;
			this.jid = jid;
			this.presence = presence;
			this.listener = listener;
		}

		@Override
		public void run() {
			String statusInfo = presence.getStatus();
			if(statusInfo == null)
				statusInfo = "没有设置！";
			 ChatWindow chatWindow = new ChatWindow(topShell,username,jid,null,
			 presence.getType(),
			 presence.getMode(),statusInfo,listener);
//			new ChatWindow().open();
			 chatWindowMap.put(username, chatWindow);
			 chatWindow.open();
		}

	}

	@Override
	//由ChatWindow界面调用
	public void handleOutgoingMsg(String jid, String msg) {
		conversation.sendMsg(jid, msg);		
	}

	@Override
	//负责处理MainWindow得到的chat请求
	public void handleChatRequest(String username, String jid) {
		ChatWindow chatWindow = getChatWindow(username,jid);
//		chatWindow.getShell().setFocus();
	}
	public ChatWindow getChatWindow(String username, String jid) {
		ChatWindow chatWindow = chatWindowMap.get(username);
		if (chatWindow == null)
			createChatWindow(username, jid);
		// 已经建立了一个聊天窗口，则调用该ChatWindow的处理接收消息的方法
		chatWindow = chatWindowMap.get(username);
		return chatWindow;
	}

	@Override
	public File handleFileTranserRequest(String username,String filename, long filesize) {
		log.debug("username:" + username + "\nfilename:" + filename + "\nsize:" + filesize);
//		boolean accept = createTransferQueryDialog(username,filename,filesize);
		sendTransferRequest(username,filename,filesize);
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
		log.debug("toSaveLocation" + toSaveLocation==null?"null":toSaveLocation.getAbsolutePath());
		return toSaveLocation;
	}

	private void sendTransferRequest(String username, String filename,
			long filesize) {
		this.filesize = filesize;
		this.fileTransferFilename = filename;
		this.fileTransferUsername = username;
		hasFileTransferRequest = true;
		Display.getDefault().wake();
	}

	private boolean createTransferQueryDialog(String username, String filename,
			long filesize) {
		String msg = username + " send " + filename + "size:" + filesize + "\n"; 
		boolean accept = MessageDialog.openQuestion(topShell,"是否接收文件",msg);
		if(accept){
//			toSaveLocation = new File("/home/cnliuyix/tmp");
			toSaveLocation = getFileSaveLocation(filename);
		}
		synchronized (this) {
			this.notify();
		}
		
		return accept;
	}

	private File getFileSaveLocation(String filename) {
		//通过filename创建一个选择保存位置的对话框
//		String
		String toSaveFilename =  createFileDialog(filename);
		log.debug("filename:" + toSaveFilename);
		return new File(toSaveFilename);
	}

	private String createFileDialog(String filename) {
		// 创建一个文件对话框
		FileDialog dialog = new FileDialog(topShell, SWT.SAVE);
		dialog.setFilterPath(null);
		if(filename != null && !filename.equals("")){
			dialog.setFileName(filename);		
		}
		else{
//			dialog.setFilterExtensions(new String[]{"*.*"})	;		
		}
		return dialog.open();
	}

	@Override
	public void handleSendMsgRequest(String jid, String msg) {
		conversation.sendMsg(jid, msg);	
	}
	
	private class MainWindowBuilder implements Runnable{
		
		ChatRequestListener listener;
		
		public MainWindowBuilder(ChatRequestListener listener) {
			super();
			this.listener = listener;
		}

		@Override
		public void run() {
			MainWindow mainWindow = new MainWindow(connection.getUser(), username,
					rosterManager.getRoster(),listener);
			topShell = mainWindow.open();
//			Display display = Display.getDefault();
//			//多线程问题产生点
//			while (!shell.isDisposed()) {
//				if(!display.readAndDispatch()){
//					display.sleep();
//				}
//			}
		}
		
	}
}
