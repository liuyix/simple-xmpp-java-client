package com.liuyix.xmpp.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

public class LoginWindow {

	protected Shell shlJclient;
	protected Display display;
	private Text userText;
	private Text pwdTxt;
	private Text servTxt;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			LoginWindow window = new LoginWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlJclient.open();
		shlJclient.layout();
		while (!shlJclient.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlJclient = new Shell(SWT.SYSTEM_MODAL |SWT.SHELL_TRIM);
		shlJclient.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		shlJclient.setSize(205, 432);
		shlJclient.setText("JClient");
		shlJclient.setImage(new Image(Display.getDefault(), "icon00.png"));
		shlJclient.setLayout(new FillLayout(SWT.VERTICAL));
		
		Image logo = new Image(Display.getCurrent(), "im-1.png");
//		System.out.println("width=" + logo.getImageData().width);
//		System.out.println("Height:" + logo.getImageData().height);
		//动态设置顶层shell的大小————根据图片的宽度
		shlJclient.setSize(logo.getImageData().width,logo.getImageData().height * 2);
		Label picLable = new Label(shlJclient, SWT.NONE);
		picLable.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		picLable.setImage(logo);
		Composite loginInfo = new Composite(shlJclient, SWT.NONE);
		
		GridLayout gl_loginInfo = new GridLayout(3, true);
		loginInfo.setLayout(gl_loginInfo);
		
		Label userLbl = new Label(loginInfo, SWT.NONE);
		userLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		userLbl.setText("用户名：");
		
		new Label(loginInfo,SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		
		userText = new Text(loginInfo, SWT.BORDER);
		userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label pwdLbl = new Label(loginInfo, SWT.CENTER);
		pwdLbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		pwdLbl.setText("密码:");
		
		new Label(loginInfo,SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		
		pwdTxt = new Text(loginInfo, SWT.BORDER | SWT.PASSWORD);
		pwdTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label servLabel = new Label(loginInfo, SWT.NONE);
		servLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		servLabel.setText("服务器：");
		
		new Label(loginInfo,SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		
		servTxt = new Text(loginInfo, SWT.BORDER);
		servTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		new Label(loginInfo, SWT.NONE);
		
		Button button = new Button(loginInfo, SWT.NONE);
		button.setText("登录");
		new Label(loginInfo, SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		new Label(loginInfo, SWT.NONE);
		
//		shlJclient.setSize(logo.getImageData().width,loginInfo.getBounds().height + logo.getImageData().height);
	}
}
