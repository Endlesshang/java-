package com.cauc.chat;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerSocketSet {
	private ServerSocket serverSocketForFileTransfer;
	private String sender;
	private String receiver;
	private int port;
	public ServerSocketSet(String sender, String receiver) throws IOException {
		this.receiver = receiver;
		this.sender = sender;
		//this.serverSocketForFileTransfer = serverSocketForFileTransfer;
		serverSocketForFileTransfer = new ServerSocket(0);
		port = serverSocketForFileTransfer.getLocalPort();
	}
	public ServerSocket getServerSocket()
	{
		return serverSocketForFileTransfer;
	}
	public String getSender()
	{
		return sender;
	}
	public String getReceiver()
	{
		return receiver;
	}
	public int getPort()
	{
		return port;
	}
	public void serverClose() throws IOException
	{
		serverSocketForFileTransfer.close();
	}
}
