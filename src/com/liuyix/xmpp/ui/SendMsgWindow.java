package com.liuyix.xmpp.ui;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.liuyix.xmpp.ChatRequestListener;

public class SendMsgWindow {
	private static Log log = LogFactory.getLog(SendMsgWindow.class);
	protected Shell shell;
	private GridData gridData_1;
	private Text recvTxt;
	private Text msgContentTxt;
	private ChatRequestListener listener;
	private Shell topShell;
	
	
	public SendMsgWindow(Shell topShell,ChatRequestListener listener) {
		super();
		this.listener = listener;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SendMsgWindow window = new SendMsgWindow(null,null);
			Shell sendMsgWindowShell = window.open();
			while(sendMsgWindowShell.isDisposed()!=true){
				if(Display.getCurrent().readAndDispatch()!=true){
					Display.getCurrent().sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public Shell open() {
		if(topShell == null){
			Display display = Display.getDefault();
			shell = new Shell(display,SWT.SHELL_TRIM);
		}
		else
			shell = new Shell(topShell,SWT.SHELL_TRIM);
		shell.setSize(450, 300);
		shell.setText("发送消息");
		createContents();
		shell.open();
		shell.layout();
//		shell.pack();
		return shell;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		GridLayout gridLayout = new GridLayout(4,true);
		gridLayout.marginBottom = gridLayout.marginTop = 
			gridLayout.marginLeft = gridLayout.marginRight = 
				gridLayout.marginWidth = gridLayout.marginHeight = 0; 
		 gridLayout.marginTop = 10;
		 gridLayout.marginLeft = gridLayout.marginRight = 5;
		shell.setLayout(gridLayout);
		
		Label recvLbl = new Label(shell,SWT.NONE);
		recvLbl.setText("发送到：");
		GridData gData = new GridData(SWT.RIGHT,SWT.CENTER,false,false,1,1);
		gData.heightHint = 20;
		gData.widthHint = 50;
		recvLbl.setLayoutData(gData);
		
		recvTxt = new Text(shell,SWT.SINGLE);
		recvTxt.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		
		Button chooseBtn = new Button(shell,SWT.PUSH);
		chooseBtn.setText("选择联系人");
		chooseBtn.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false,1,1));
		
		Label contentLbl = new Label(shell,SWT.NONE);
		contentLbl.setText("内容:");
		contentLbl.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false,1,1));
		
		new Label(shell,SWT.NONE).setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,3,1));
		
		msgContentTxt = new Text(shell,SWT.MULTI | SWT.V_SCROLL);
		msgContentTxt.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,4,4));
		
		GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,false,2,2);
//		gridData.exclude = true;
//		gridData.widthHint = 80;
		new Label(shell,SWT.NONE).setLayoutData(gridData);
		
		Button closeBtn = new Button(shell,SWT.PUSH);
		closeBtn.setText("关闭");
		gridData = new GridData(SWT.FILL,SWT.FILL,true,false,1,2);
		gridData.widthHint = 20;
		closeBtn.setLayoutData(gridData);
		closeBtn.addSelectionListener(new SelectionAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		
		Button sendBtn = new  Button(shell,SWT.PUSH);
		sendBtn.setText("发送");
		gridData_1 = new GridData(SWT.FILL,SWT.FILL,true,false,1,2);
		gridData_1.heightHint = 30;
		gridData_1.widthHint = 20;
		sendBtn.setLayoutData(gridData_1);
		sendBtn.addSelectionListener(new SelectionAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				sendMsg();
				shell.dispose();
			}
			
		});
	}

	protected void sendMsg() {
		String userJid = recvTxt.getText();
		String sendMsg = msgContentTxt.getText();
		if(userJid!=null&&sendMsg!=null){
			log.debug("\nuserJid:" + userJid + "\nsendMsg" + sendMsg);
			if(listener != null)
				listener.handleSendMsgRequest(userJid,sendMsg);
		}
		
		
	}

}
