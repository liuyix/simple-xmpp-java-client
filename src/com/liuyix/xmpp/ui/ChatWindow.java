package com.liuyix.xmpp.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

import com.liuyix.xmpp.GuiStarter;
import com.liuyix.xmpp.OutgoingMsgListener;
import com.liuyix.xmpp.Util;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.core.databinding.observable.Realm;


public class ChatWindow {
	
	private DataBindingContext m_bindingContext;

	protected Shell shell;
	private String username;//联系人的名字
	private String jid;
	private Image userImage;
	private Presence.Type statusType;
	private Presence.Mode statusMode;
	private String statusInfo;
	private StyledText msgBoardTxt;
	private Text sendMsgTxt;
	private GridData gd_sendMsgTxt;
	private OutgoingMsgListener sendMsgListener;
//	private static TextStyle YOURNAMESTYLE =  
	
	public ChatWindow(String username, String jid, Image userImage,
			Type statusType, Mode statusMode,String statusInfo,OutgoingMsgListener listener) {
		super();
		this.username = username;
		this.jid = jid;
		this.userImage = userImage;
		this.statusType = statusType;
		this.statusInfo = statusInfo;
		this.statusMode = statusMode;
		this.sendMsgListener = listener;
	}
	
	//debug only
	public ChatWindow() {
		this("TEST-USER","TEST@localhost",null,Presence.Type.available,Mode.available,"TEST:AVAILABLE",new OutgoingMsgListener() {
			
			@Override
			public void handleOutgoingMsg(String jid, String msg) {
				Util.showDebugMsg("ChatWindow#无参数构造函数:\n" + jid + ":\t" + msg);			
			}
		});
		
	}


	/*
	 * 初始化需要的信息：
	 * nickname
	 * jid
	 * image*--vCard支持,若没有返回默认头像
	 * status type
	 * status msg 
	 * 
	 * */
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		shell = new Shell(display,SWT.SYSTEM_MODAL | SWT.SHELL_TRIM | SWT.RESIZE);
		//监听关闭操作
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				//应该更新GuiStarter里面的数据结构
				GuiStarter.deleteChatWindow(getUsername());
			}
		});
		shell.setLayout(new FillLayout());
		shell.setSize(500, 400);
		shell.setText("与" + getUsername() + "聊天中");
		createContents();
		shell.open();
		shell.layout();
//		while (!shell.isDisposed()) {
//			if (!display.readAndDispatch()) {
//				display.sleep();
//			}
//		}
	}

	/**
	 * 内部方法
	 * 返回联系人的名称（nickname）
	 * 
	 * */
	private String getUsername() {
		//
		return this.username;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		SashForm form = new SashForm(shell,SWT.BORDER | SWT.VERTICAL);
		form.setLayout(new FillLayout(SWT.VERTICAL));
		
		createUserPanel(form);
		createMsgBoard(form);
		createSendBoard(form);
		
		form.setWeights(new int[]{20,50,30});
	}
	
	private void createUserPanel(SashForm form) {
		Composite composite = new Composite(form, SWT.BORDER);
		GridLayout gridLayout = new GridLayout(2,false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		composite.setLayout(gridLayout);
		
		Label userImageLbl = new Label(composite,SWT.NONE | SWT.SHADOW_OUT);
//		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		GridData gridData = new GridData(SWT.FILL,SWT.FILL,false,true,1,4);
//		gridData.heightHint = 40;
//		gridData.widthHint = image.getBounds().width + 10;
		userImageLbl.setLayoutData(gridData);
		userImageLbl.setImage(getUserImage());
		userImageLbl.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
							
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO 此处添加显示用户信息的功能
				Util.showDebugMsg("此处应该显示用户信息的窗口！");
				
			}
		});
		Label usernameLbl = new Label(composite,SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2);
//		gridData.heightHint = 20;
//		gridData.widthHint = 50;
		usernameLbl.setLayoutData(gridData);
		usernameLbl.setText(getUserInfo());
		
		
		final Label userStatusLbl = new Label(composite,SWT.BORDER);
		userStatusLbl.setText(this.statusInfo);
//		userStatusLbl.setEnabled(false);
		userStatusLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
	}
	//格式化显示
	private String getUserInfo() {
		if(this.username != null && this.jid!= null){
			return username + "(" + jid + ")";
		}
		return "ERROR";
	}

	private void createMsgBoard(SashForm form) {
		msgBoardTxt = new StyledText(form, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
//		text.setEditable(false);
		
	}

	private void createSendBoard(SashForm form) {
		// TODO
		Composite composite = new Composite(form, SWT.BORDER);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginTop = 0;
		composite.setLayout(gridLayout);
		
		ToolBar bar = new ToolBar(composite,SWT.PUSH);
		bar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false,6,1));
		ToolItem item1;
		int debugCnt = 5;
		for(int i=0;i<5;++i){
			item1 = new ToolItem(bar,SWT.PUSH);
			item1.setText("第" + (i+1) + "个");
		}
		
		
		sendMsgTxt = new Text(composite,SWT.MULTI | SWT.WRAP | SWT.NONE | SWT.V_SCROLL);
		GridData gridData;
		gd_sendMsgTxt = new GridData(SWT.FILL,SWT.FILL,true,true,6,4);
		gd_sendMsgTxt.heightHint = 50;
		sendMsgTxt.setLayoutData(gd_sendMsgTxt);
		sendMsgTxt.setEditable(true);
		sendMsgTxt.setText("写点什么吧！");
		
		GridData gridData_1 = new GridData(SWT.FILL,SWT.FILL,true,true,3,1);
		gridData_1.widthHint = 200;
		new Label(composite,SWT.NONE).setLayoutData(gridData_1);

		Button closeBtn = new Button(composite,SWT.PUSH | SWT.BORDER);
		gridData = new GridData(SWT.FILL,SWT.FILL,true,false,1,1);
		//		gridData.widthHint = 20;
		closeBtn.setLayoutData(gridData);
		closeBtn.setText("关闭");
		closeBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//TODO 更新GuiStarter里面的数据结构
				GuiStarter.deleteChatWindow(getUsername());
				shell.dispose();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			
				
			}
		});
		
		new Label(composite, SWT.NONE);
		
		
		Button sendBtn = new Button(composite,SWT.PUSH | SWT.BORDER);
		gridData = new GridData(SWT.FILL,SWT.FILL,true,false,1,1);
//		gridData.widthHint = 20;
		sendBtn.setLayoutData(gridData);
		sendBtn.setText("发送");
		sendBtn.addSelectionListener(new SendMsgOp());
	}
	
	private Image getUserImage(){
		if(userImage == null){
			Image userImage = new Image(Display.getCurrent(), "default-user-image.png");
			Image scaledImage = new Image(Display.getCurrent(),userImage.getImageData().scaledTo(80, 80));
			userImage.dispose();
			return scaledImage;
		}
		else return userImage;
	}

	//发送消息的操作
	private class SendMsgOp extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			//点击发送的操作：
			//应该有2个：一个是更新MsgBoard,一个是进行底层发送信息的操作			
			String toSendMsg = sendMsgTxt.getText();
			sendMsgTxt.setText("");
			Util.showDebugMsg("SendMsgOp:" + toSendMsg);
			if(toSendMsg != null){
				//TODO 包装信息！
				updateMsgBoard(toSendMsg, Util.getYourname());
				sendMsgListener.handleOutgoingMsg(jid, toSendMsg);
			}
		}
		
	}
	
	public void handleIncomingMsg(String msg) {
		//收到外部消息时会调用此方法
		if(msg != null){
			//TODO 格式化消息
			updateMsgBoard(msg,getUsername());
		}
	}
	
	/**
	 * 更新MsgBoard的方法，再次进行格式化文本的操作
	 * */
	private void updateMsgBoard(String msg, String username) {
		if(username.equals(Util.getYourname())){
			//自己更新的消息
//			TextStyle yournameStyle = new TextStyle(null, 
//					Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE),
//					Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//			StyleRange range = new StyleRange(yournameStyle);
			msgBoardTxt.append("我说：\n");
			msgBoardTxt.append(msg);
			msgBoardTxt.append("\n");
		}
		else{
			msgBoardTxt.append(username + "说:\n");
			msgBoardTxt.append(msg);
			msgBoardTxt.append("\n");
		}
		
	}

	/**
	 * 调试函数
	 * @param args
	 */
	public static void main(String[] args) {
//		Display display = Display.getDefault();
//		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
//			public void run() {
//				try {
//					ChatWindow window = new ChatWindow();
//					window.open();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
		new ChatWindow().open();
	}

	
}
