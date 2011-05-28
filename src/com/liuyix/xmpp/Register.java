package com.liuyix.xmpp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;

/**
 * 注册信息类，获取注册需要的所有信息并完成注册。
 * */
public class Register {
	
	Connection conn;
	Registration regInfo;
	Map<String,String> attribs;
	String instruction;
	Form regForm = null;
	/**
	 * 初始化Register，并获取注册需要的表单信息（调用getAttribs方法）。
	 * @param conn 初始化参数
	 * 
	 * */
	//FIXME 重构Register类，将错误输出改为Util类
	public Register(Connection conn){
		if(conn == null || conn.isConnected()==false || conn.isAuthenticated()==true){
			if(conn == null){
				System.err.println("Connection is NULL");
			}
			else if(conn.isConnected() == false){
				System.err.println("Connection is not connected");
			}
			else if(conn.isAuthenticated() == true){
				System.err.println("Connection is NULL");
			}
			System.err.println("RegisterInfo Error!@construction");
		}
		else{
			this.conn = conn;
			getAttribs();
		}
	}
	/**
	 * 过滤得到Register信息包</br>
	 * 建立一个过滤包类-->发送空register-->收register包-->取得attribs,form
	 * */
	private void getAttribs(){
		PacketFilter filter = new AndFilter(new PacketTypeFilter(IQ.class),new IQTypeFilter(IQ.Type.RESULT));
		PacketCollector regCollector = conn.createPacketCollector(filter);
		//发送空的registration
		conn.sendPacket(new Registration());
		//等待包
		Packet recvPacket = regCollector.nextResult();
		Registration recvReg = (Registration)recvPacket;
		attribs = recvReg.getAttributes();
		regInfo = new Registration();
		regInfo.setAttributes(attribs);
		if(recvPacket.getExtension("jabber:x:data")!=null){
			//有FORM项
			regForm = Form.getFormFrom(recvPacket);
		}
		else regForm = null;
		return ;
	}
	/**
	 * 得到注册信息
	 * @param reader 得到注册信息的输入流
	 * @param writer 输出需要的注册信息的输出流
	 * */
	private void getRegInfo(BufferedReader reader,BufferedWriter writer){
		String inputStr = null;
		try {
			if(regForm == null){
				//无FORM则输出register			
					for(Map.Entry<String, String>m:attribs.entrySet()){					
							writer.append(m.getKey() + "\n");
							//不flush则无法输出
							writer.flush();
							inputStr = reader.readLine();
							regInfo.getAttributes().put(m.getKey(), inputStr);
					}
			}
			else {
				//TODO 重构注册信息的显示，要更清晰，可考虑独立出一个方法
				Form formToSend = regForm.createAnswerForm();
				for(Iterator<FormField> itr =regForm.getFields();itr.hasNext();){
					FormField field = itr.next();
					writer.append(field.getDescription() + "\n");
					writer.append(field.getLabel() + "\n");
					writer.append(field.isRequired()?"Required":"No required");
					Iterator<String> formFielditr = field.getValues();
					if(formFielditr != null){
						writer.append("Options:\n");
						while(formFielditr.hasNext()){
							writer.append(formFielditr.next() + "\n");
						}
					}
					writer.flush();
					inputStr = reader.readLine();
					formToSend.setAnswer(field.getVariable(), inputStr);
				}//end for
				//将填写好的表单附加到regInfo后面
				regInfo.addExtension(formToSend.getDataFormToSend());
				regInfo.setType(IQ.Type.SET);
			}
		}catch (IOException e) {
//			e.printStackTrace();
			System.err.println("getRegInfo exception.\n" + e.getMessage());
		}//end try..catch
	}
	
	/**
	 * RegisterHandler:调用其他函数，完成注册
	 * @author cnliuyix
	 * @param reader 读入数据流
	 * @param writer 提示信息数据流
	 * */
	public void registerHandler(BufferedReader reader,BufferedWriter writer){
		if(reader == null || writer == null){
			throw new IllegalArgumentException("parameters is null!");
		}
		else{
			getRegInfo(reader, writer);
			conn.sendPacket(regInfo);
		}
	}	
}
