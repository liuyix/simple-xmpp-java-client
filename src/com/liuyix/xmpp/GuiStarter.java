/**
 * GUI程序的总控制程序
 */
package com.liuyix.xmpp;

import java.util.Scanner;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.liuyix.xmpp.ui.LoginListener;
import com.liuyix.xmpp.ui.LoginWindow;
import com.liuyix.xmpp.ui.MainWindow;

/**
 * @author cnliuyix
 * 
 */
public class GuiStarter {

	private boolean enableDebug = true;
	// 非常重要的成员
	Connection connection;
	String username;
	String password;
	// 基本功能的实例
	RosterManager rosterManager;
	ConversationManager conversation;
	PresenceManager presenceManager;
	String mucAddr;
	LoginWindow loginWindow;
	

	public static void main(String[] args) {
		new GuiStarter(true).run();
	}

	public GuiStarter(boolean debug) {
		enableDebug = debug;
	}

	private void run() {
		try {
			//TODO 添加非debug的形式
			LoginWindow loginWindow = new LoginWindow(true,"mick","mick","localhost");
			loginWindow.addLoginListener(new LoginHandler(loginWindow));
			loginWindow.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//已经成功登录
		Util.setUsername(connection.getUser());
		rosterManager = new RosterManager(connection);
		conversation = new ConversationManager(connection);
		presenceManager = new PresenceManager(connection);
		MainWindow mainWindow = new MainWindow(connection.getUser(),username,rosterManager.getRoster());
		mainWindow.open();
	}
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
				//TODO 未实现：注册界面
//				register(System.in, System.out);
			}
			/* 判断是否已经登录成功 */
//			while (connection.isAuthenticated() != true) {
//				//TODO 登录未成功的操作
//				boolean needToRegister = !userLogin();
//				if (needToRegister) {
//					// 输入为空，询问是否需要注册
//					String answer = getInput("需要注册?", false);
//					if (answer == "y" && answer == "Y") {
//						register(System.in, System.out);
//					} else
//						break;
//				}
//			}
		}
//		return connection.isAuthenticated();
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
		public void handleLoginInfo(String name, String passwd, String server) {
//			Util.showDebugMsg("\nname:" + name + "\npasswd:" + passwd + "\nserver:" + server);
			try {
				if(enterServ(enableDebug,server)!=true){
					//TODO 登录服务器未成功
					Util.printErrMsg("登录服务器未成功！");
				}
				else{
					//登录服务器成功
					if(userLogin(name,passwd)!=true){
						//用户登录不成功
					}
					else{//用户登录成功
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
		if(connection!=null){
			try {
				connection.login(name, passwd);
				if(connection.isAuthenticated())
					return true;
			} catch (XMPPException e) {
				//登录失败 XMPP错误
				if(enableDebug)
					e.printStackTrace();
				else
					Util.showErrMsg("userLogin:XMPP ERROR:" + e.getXMPPError().getCode());
			}
		}
		return false;
	}	
	
}
