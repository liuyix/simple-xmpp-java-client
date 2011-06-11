package com.liuyix.xmpp.ui;

import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * GUI的统一资源管理,采用singlten模式 
 * 
 * */
public class ResourceManager {

	private ImageRegistry imageRegistry;
//	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("resource.propertites");
	public static int logo = 0;
	public static int icon_user_online = 1;
	public static int login_image = 2;
	public static int icon_add_user = 3;	
	public static int icon_create_chat = 4;
	public static int default_user_image = 5;
	public static int icon_multi_users = 6;
	public static int icon_user_busy = 7;
	public static int icon_user_offline = 8;
	public static int icon_users = 9;
	public static int icon_user_unavailable = 10;
	
	public static String[] images = {
		"logo.png",
		"user_online.png",
		"login-image.png",
		"add_user.png",	
		"create_chat.png",
		"default-user-image.png",
		"multi_users.png",
		"user_busy.png",
		"user_icon.png",
		"users_icons.png",
		"user_unavailable.png"
	};
	
	private static final String BASEDIR = "resources/";
	
	private ResourceManager(){
		initResource();
	}
	
	
	private static ResourceManager resourceMangager;
	
	public static ResourceManager getInstance(){
		if(resourceMangager == null){
			resourceMangager = new ResourceManager();
			return resourceMangager;
		}
		else return resourceMangager;
	}
	/**
	 * 初始化资源
	 * */
	private void initResource() {
		imageRegistry = new ImageRegistry(Display.getDefault());
		for(int i=0;i<images.length;++i){
			imageRegistry.put(images[i], new Image(Display.getDefault(),BASEDIR+images[i]));
		}
		
	}
	
	/**
	 * 对外接口，若没有资源则返回null
	 * */
	public Image getImage(int name){
		if(name >= 0 && name < images.length){
			return imageRegistry.get(images[name]);
		}
		else return null;
	}
	
	
}
