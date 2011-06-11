package com.liuyix.xmpp;

import java.io.File;

public interface IncomingFileReqListener {
	File handleFileTranserRequest(String username,String filename,long filesize);
}
