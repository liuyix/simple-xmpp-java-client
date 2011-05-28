package com.liuyix.xmpp;

import java.io.File;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * TODO 文件传输的功能完善：要有发送文件的方法以及传输文件保存位置选择的方法
 * 
 * 
 * */
public class FileTrans {
	//快速传送文件
	Connection connection;
	FileTransferManager fileTransferManager;	
	InBandBytestreamManager ibbManager;
	/**
	 * @exception IllegalArgumentException
	 * 				connection为空或者用户没有登录
	 * 
	 * */	
	public FileTrans(Connection conn){
		if(conn == null){
			throw new IllegalArgumentException("connection is NULL");
		}
		else if(conn.isAuthenticated() != true){
			throw new IllegalArgumentException("user has not connected!");
		}
		this.connection = conn;
		fileTransferManager = new FileTransferManager(connection);
		ibbManager = InBandBytestreamManager.getByteStreamManager(conn);
		fileTransferManager.addFileTransferListener(new FileTransferListener() {
			
			@Override
			public void fileTransferRequest(FileTransferRequest request) {
				// 
				IncomingFileTransfer ifTransfer = request.accept();
				try {
					ifTransfer.recieveFile(new File("tmp"));
				} catch (XMPPException e) {
					// 
//					e.printStackTrace();
					Util.showErrMsg("FILE RECV:\t" + e.getMessage());
					e.printStackTrace();
				}
			}
		});
		
	}

	void send(String recver,File file) throws XMPPException{
		OutgoingFileTransfer outTransfer = fileTransferManager.createOutgoingFileTransfer(recver);
		outTransfer.sendFile(file, "des");		
	}
	
}
