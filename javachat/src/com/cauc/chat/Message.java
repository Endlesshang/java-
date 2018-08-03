package com.cauc.chat;

import java.io.File;
import java.io.Serializable;

public class Message implements Serializable {
	private String srcUser;
	private String dstUser;

	public Message(String srcUser, String dstUser) {
		this.srcUser = srcUser;
		this.dstUser = dstUser;
	}

	public String getSrcUser() {
		return srcUser;
	}

	public void setSrcUser(String srcUser) {
		this.srcUser = srcUser;
	}

	public String getDstUser() {
		return dstUser;
	}

	public void setDstUser(String dstUser) {
		this.dstUser = dstUser;
	}
}

class ChatMessage extends Message {
	private String msgContent;

	public ChatMessage(String srcUser, String dstUser, String msgContent) {
		super(srcUser, dstUser);
		this.msgContent = msgContent;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public boolean isPubChatMessage() {
		return getDstUser().equals("");
	}
}

class UserStateMessage extends Message {
	private boolean userOnline;

	public UserStateMessage(String srcUser, String dstUser, boolean userOnline) {
		super(srcUser, dstUser);
		this.userOnline = userOnline;
	}

	public boolean isUserOnline() {
		return userOnline;
	}

	public boolean isUserOffline() {
		return !userOnline;
	}

	public void setUserOnline(boolean userOnline) {
		this.userOnline = userOnline;
	}

	public boolean isPubUserStateMessage() {
		return getDstUser().equals("");
	}
}

class FileTranferMessage extends Message
{
	private String fileName;
	private long fileLen;
	public FileTranferMessage(String srcUser, String dstUser,String fileName, long fileLen) 
	{
		super(srcUser, dstUser);
		this.fileName = fileName;
		this.fileLen = fileLen;
	}
	
	String getFileName()
	{
		return fileName;
	}
	long getLen()
	{
		return fileLen;
	}
}

class FileTranferReceiveMessage extends Message
{
	private boolean wannaReceive;
	private int portForTransfer;
	private String fileName;
	private long fileLen;
	public FileTranferReceiveMessage(String srcUser, String dstUser, boolean wannaReceive, int portForTransfer, String fileName, long fileLen) {
		super(srcUser, dstUser);
		this.wannaReceive = wannaReceive;
		this.portForTransfer = portForTransfer;
		this.fileName = fileName;
	}
	
	boolean getPurpose()
	{
		return wannaReceive;
	}
	
	int getPort()
	{
		return portForTransfer;
	}
	
	String getFileName()
	{
		return fileName;
	}
	
	long getFileLen()
	{
		return fileLen;
	}
}

class talkWithoutOnlineMessage extends Message
{
	private String dstUser;
	private String srcUser;
	public talkWithoutOnlineMessage(String dstUser, String srcUser) {
		super(srcUser, dstUser);
	}
}
