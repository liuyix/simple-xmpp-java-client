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
public class TransferManager {


	//快速传送文件
	Connection connection;
	FileTransferManager fileTransferManager;	
	InBandBytestreamManager ibbManager;
	IncomingFileReqListener listener;
	/**
	 * @exception IllegalArgumentException
	 * 				connection为空或者用户没有登录
	 * 
	 * */	
	public TransferManager(Connection conn,IncomingFileReqListener listener){
		if(conn == null){
			throw new IllegalArgumentException("connection is NULL");
		}
		else if(conn.isAuthenticated() != true){
			throw new IllegalArgumentException("user has not connected!");
		}
		this.connection = conn;
		fileTransferManager = new FileTransferManager(connection);
		this.listener = listener;
		ibbManager = InBandBytestreamManager.getByteStreamManager(conn);
		fileTransferManager.addFileTransferListener(new FileTranserHandler());
		
		
	}

	void send(String recver,File file) throws XMPPException{
		OutgoingFileTransfer outTransfer = fileTransferManager.createOutgoingFileTransfer(recver);
		outTransfer.sendFile(file, "");
	}
	
	void send(String recver,String filepath) throws XMPPException{
		File toSendFile = new File(filepath);
		send(recver,toSendFile);
	}

	/**
	 * @param listener the listener to set
	 */
	void setListener(IncomingFileReqListener listener) {
		this.listener = listener;
	}
	
	private class FileTranserHandler implements FileTransferListener {

		@Override
		public void fileTransferRequest(FileTransferRequest request) {
			if(listener == null){
				request.reject();
				return;
			}
			File filePath = listener.handleFileTranserRequest(request.getRequestor(),request.getFileName(),request.getFileSize());
			if(filePath != null){
				IncomingFileTransfer ifTransfer = request.accept();
				try {
					ifTransfer.recieveFile(filePath);
				} catch (XMPPException e) {
					// 
	//					e.printStackTrace();
					Util.showErrMsg("FILE RECV:\t" + e.getMessage());
					e.printStackTrace();
				}
			}
		}

	}
	
}
