package com.liuyix.xmpp.ui;

/**
 * 使用Event-Listener方法完成界面和底层的交互：
 * 若界面跟着底层，那么就实现一个监听端口，同时在底层更新时调用该端口，反之也一样！
 * **/



import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

import com.liuyix.xmpp.ChatRequestListener;
import com.liuyix.xmpp.PresenceManager;
import com.liuyix.xmpp.Util;


public class MainWindow {
	private static Log log = LogFactory.getLog(MainWindow.class);
	protected Shell shell;

	private String jid;
	private String username;
	private Roster roster;
	private ChatRequestListener chatReqListener;
	private ResourceManager resourceManager;
	
	public MainWindow(String jid, String username, Roster roster,ChatRequestListener chatReqListener) {
		super();
		this.jid = jid;
		this.username = username;
		this.roster = roster;
		this.chatReqListener = chatReqListener;
		resourceManager = ResourceManager.getInstance();
		
	}
	
	/** 
	 * @deprecated 只可用于测试！
	 * */
	public MainWindow() {
		this("TEST","TEST",null,null);

	}	

	/**
	 * debug only
	 * 
	 * **/
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			Shell shell = window.open();
			
			while(shell.isDisposed()!=true){
				if(Display.getDefault().readAndDispatch()!=true){
					Display.getDefault().sleep();
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
		Display display = Display.getDefault();
		
		shell = new Shell(display,SWT.SYSTEM_MODAL | SWT.SHELL_TRIM);
		
		shell.setSize(300, 600);
		shell.setText("JClient");
		shell.setImage(resourceManager.getImage(ResourceManager.logo));
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				
			}
		});
		
//		//FUTURE 设置位置
//		Rectangle rec = display.getPrimaryMonitor().getBounds();
//		shell.setLocation(rec.height, rec.width);
		
		createMenubar();
		
		createContents();
		int windowX =  Display.getDefault().getBounds().width - shell.getBounds().width;
		int windowY = (Display.getDefault().getBounds().height - shell.getBounds().height)/2;
//		log.debug("shell width=" + shell.getBounds().width + ",height=" + shell.getBounds().height);
//		log.debug("display x:" + Display.getDefault().getBounds().width + ",y:" + Display.getDefault().getBounds().height);
		shell.setBounds(
				windowX, 
				windowY,
				shell.getBounds().width,shell.getBounds().height);
		shell.open();
		shell.layout();
		return shell;
//		while (!shell.isDisposed()) {
//			if (!display.readAndDispatch()) {
//				Util.showDebugMsg("~~~~");
//				
//				display.sleep();
//			}
//		}
	}

	private void createMenubar() {
		//新建一个menuBar并加到shell上面
		Menu menuBar = new Menu(shell, SWT.BAR);		
		shell.setMenuBar(menuBar);
		
		createMainmenu(menuBar);	
	}

	private void createMainmenu(Menu menuBar) {
		//新建一个MenuItem,加到MenuBar上
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("主菜单");
		
		//新建MenuItem要弹出的Menu，并设置其对应关系
		Menu menu = new Menu(shell,SWT.DROP_DOWN);
		item.setMenu(menu);
		
		MenuItem subItem = new MenuItem(menu,SWT.NONE);
		subItem.setText("设置");
		
		
		subItem = new MenuItem(menu,SWT.SEPARATOR);
		
		subItem = new MenuItem(menu,SWT.NONE);
		subItem.setText("退出");
		subItem.addSelectionListener(new SelectionAdapter() {
			//TODO 菜单项“退出”操作
		});
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		SashForm form = new SashForm(shell,SWT.BORDER | SWT.VERTICAL);
		form.setLayout(new FillLayout());
		
		createUserPanel(form);
		createRosterPanel(form);
		
		form.setWeights(new int[]{20,80});
		
	}

	private void createUserPanel(SashForm form) {
		//TODO 获取用户的信息予以显示
		Image image = getUserImage();
		String userInfo = getUserInfo();
		Composite composite = new Composite(form, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2,false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		composite.setLayout(gridLayout);
		
		Label userImageLbl = new Label(composite,SWT.NONE | SWT.SHADOW_OUT);
//		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		GridData gridData = new GridData(SWT.FILL,SWT.FILL,false,true,1,3);
//		gridData.heightHint = 40;
//		gridData.widthHint = image.getBounds().width + 10;
		userImageLbl.setLayoutData(gridData);
		userImageLbl.setImage(image);
		userImageLbl.addMouseListener(new MouseListener() {			
			@Override
			public void mouseUp(MouseEvent e) {				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				//TODO 显示用户信息的对话框
				Util.showDebugMsg("此处应该显示用户信息编辑的对话框！");
			}
		});
//		
////		userImageLbl.setVisible(false);
//		
		Label usernameLbl = new Label(composite,SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
//		gridData.heightHint = 20;
//		gridData.widthHint = 50;
		usernameLbl.setLayoutData(gridData);
		usernameLbl.setText(userInfo);
		
		
		final Text userStatusTxt = new Text(composite,SWT.SINGLE | SWT.BORDER | SWT.LEFT);
		userStatusTxt.setText(getStatusInfo());
		userStatusTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		userStatusTxt.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.detail == SWT.TRAVERSE_RETURN){
					//TODO 更新状态
					String status = userStatusTxt.getText();
					System.out.println("更新状态！更新状态为" + status);
				}				
			}
		});
		
		final Combo statusCmb = new Combo(composite,SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		statusCmb.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		statusCmb.setItems(PresenceManager.PRESENCE);
		statusCmb.select(0);
		statusCmb.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				//TODO 用户状态改变时的操作
				Util.showDebugMsg("你选择了" + PresenceManager.PRESENCE[statusCmb.getSelectionIndex()]);
			}
		});
		
	}
	
	// TODO 获取用户的状态信息文本
	private String getStatusInfo() {
		
		return "not set";
	}

	// TODO 获取用户名
	private String getUserInfo() {		
		return this.username + "(" + this.jid + ")";
	}

	/**
	 * 该方法会重新调整图像大小
	 * TODO 获取用户图像，若没有则返回默认图像
	 * */
	private Image getUserImage() {
		Image userImage = resourceManager.getImage(ResourceManager.default_user_image);
		Image scaledImage = new Image(Display.getCurrent(),userImage.getImageData().scaledTo(80, 80));
		return scaledImage;
	}
	
	// TODO 联系人列表
	private void createRosterPanel(SashForm form) {
		Tree tree = new Tree(form,SWT.BORDER | SWT.VIRTUAL);
		tree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				//TODO 
				TreeItem selectedItem = (TreeItem)e.item;
				String userInfo = selectedItem.getText();
				//FIXME 应该使用Map结构记录所有的显示的user-jid数据结构
				if(userInfo.indexOf("(")!=-1 && userInfo.indexOf(')')!=-1){
					String username = userInfo.substring(0, userInfo.indexOf("("));
					String userJid = userInfo.substring(userInfo.indexOf("(")+1,userInfo.indexOf(")"));
					Util.showDebugMsg("\nSelect:username" + username + "  userJid:" + userJid);
					if(chatReqListener != null)
						chatReqListener.handleChatRequest(username,userJid);
				}
			}
			
		});
		int rootItemCnt = 10,subRootItemCnt = 30;
//		TreeItem item,subItem;
//		for(int i=0;i<rootItemCnt;++i){
//			item = new TreeItem(tree,SWT.NULL);
//			item.setText("好友组" + i);
//			for(int j=0;j<subRootItemCnt;++j){
//				subItem = new TreeItem(item,SWT.NULL);
//				subItem.setText("好友-" + i + j);
//			}
//		}	
		updateRoster(tree);
	}
	
	/**
	 * 根据成员roster更新树结构
	 * */
	private void updateRoster(Tree tree) {
		if(this.roster == null){
			return;			
		}
		tree.clearAll(true);
		Collection<RosterEntry> entries;
		Collection<RosterGroup> groups;
//		entries = roster.getEntries();
		groups = roster.getGroups();
		TreeItem treeItem,subTreeItem;
		for(RosterGroup group : groups){
			treeItem = new TreeItem(tree,SWT.NULL);
			treeItem.setText(group.getName());
			for(RosterEntry entry : group.getEntries()){
				subTreeItem = new TreeItem(treeItem, SWT.NULL);
				subTreeItem.setText(entry.getName() + "(" + entry.getUser() + ")");
			}
			treeItem.setExpanded(true);
		}		
	}

	void setChatReqListener(ChatRequestListener chatReqListener) {
		this.chatReqListener = chatReqListener;
	}
	
	


}
