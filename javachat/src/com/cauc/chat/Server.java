package com.cauc.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Server extends JFrame {
	private SSLServerSocket serverSocket;
	private final int port = 9999;
	// 保存在线用户的用户名与Socket信息
	private final UserManager userManager = new UserManager();
	// “在线用户列表ListModel”,用于维护“在线用户列表”中显示的内容
	final DefaultTableModel onlineUsersDtm = new DefaultTableModel();
	// 用于控制时间信息显示格式
	// private final SimpleDateFormat dateFormat = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private final JPanel contentPane;
	private final JTable tableOnlineUsers;
	private final JTextPane textPaneMsgRecord;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				Server frame = new Server();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws Exception 
	 */
	public Server() throws Exception {
		SSLContext context = createSSLContext();
		SSLServerSocketFactory factory = context.getServerSocketFactory();
		//serverSocket = (SSLServerSocket) factory.createServerSocket(port, 20, InetAddress.getLocalHost());
		
		setTitle("\u670D\u52A1\u5668");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 561, 403);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPaneNorth = new JSplitPane();
		splitPaneNorth.setResizeWeight(0.5);
		contentPane.add(splitPaneNorth, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setPreferredSize(new Dimension(100, 300));
		scrollPaneMsgRecord.setViewportBorder(
				new TitledBorder(null, "\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneNorth.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setPreferredSize(new Dimension(100, 100));
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setPreferredSize(new Dimension(100, 300));
		splitPaneNorth.setRightComponent(scrollPaneOnlineUsers);

		onlineUsersDtm.addColumn("用户名");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("端口");
		onlineUsersDtm.addColumn("登录时间");
		onlineUsersDtm.addColumn("用户状态");
		tableOnlineUsers = new JTable(onlineUsersDtm);
		tableOnlineUsers.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				try {
					if (tableOnlineUsers.getSelectedColumn() == 0) {
						String userNameDelete = (String) tableOnlineUsers.getValueAt(tableOnlineUsers.getSelectedRow(),
								tableOnlineUsers.getSelectedColumn());
						System.out.println(userNameDelete);
						System.out.println(tableOnlineUsers.getSelectedRow());
						String deleteMsg = "是否删除用户" + userNameDelete;
						Object[] options = { "确认删除", "取消" };
						int chosen = JOptionPane.showOptionDialog(null, deleteMsg, "删除确认框", JOptionPane.OK_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (chosen == 0) {
							//System.out.println("我要删掉用户了！");
							userManager.removeUser(userNameDelete);
							//tableOnlineUsers.remove(tableOnlineUsers.getSelectedRow());
							onlineUsersDtm.removeRow(tableOnlineUsers.getSelectedRow());
						} 
					}
				} catch (HeadlessException e) {
					e.printStackTrace();
				} catch (ClassCastException e2) {
					JOptionPane.showInternalMessageDialog(null, "请点击用户名以踢出用户！");
				}
			}
		});
		
		
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true); // 让JTable充满它的容器
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton btnStart = new JButton("\u542F\u52A8");
		// "启动"按钮
		btnStart.addActionListener((e) -> {
			try {
				// 创建ServerSocket打开端口9999监听客户端连接
				// SSLServerSocket的create方法第二个参数是支持连接的最大客户端数
				serverSocket = (SSLServerSocket) factory.createServerSocket(port, 20, InetAddress.getLocalHost());
				// 在“消息记录”文本框中用红色显示“服务器启动成功X”和启动时间信息
				String msgRecord = dateFormat.format(new Date()) + " 服务器启动成功" + "\r\n";
				addMsgRecord(msgRecord, Color.red, 12, false, false);
				// 创建并启动“接受用户连接线程”，接受并处理客户端连接请求
				new Thread(() -> {
					while (true) {
						try {
							// 调用serverSocket.accept()方法接受用户连接请求
							Socket socket = serverSocket.accept();
							// 为新来的用户创建并启动“用户服务线程”
							// 并把serverSocket.accept()方法返回的socket对象交给“用户服务线程”来处理
							UserHandler userHandler = new UserHandler(socket);
							new Thread(userHandler).start();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}).start();
				btnStart.setEnabled(false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		panelSouth.add(btnStart);
	}
	
	//通过密钥库文件生成SSL安全的套接字工厂
	public SSLContext createSSLContext() throws Exception
	{
		String keyStoreFile = "test.keys";
		String passPhrase = "654321";
		KeyStore ks = KeyStore.getInstance("JKS");
		char[] password = passPhrase.toCharArray();
		ks.load(new FileInputStream(keyStoreFile), password);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, password);
		
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(kmf.getKeyManagers(), null, null);
		
		return sslContext;
	}

	// 向消息记录文本框中添加一条消息记录
	private void addMsgRecord(final String msgRecord, Color msgColor, int fontSize, boolean isItalic,
			boolean isUnderline) {
		final SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, msgColor);
		StyleConstants.setFontSize(attrset, fontSize);
		StyleConstants.setUnderline(attrset, isUnderline);
		StyleConstants.setItalic(attrset, isItalic);
		SwingUtilities.invokeLater(() -> {
			Document docs = textPaneMsgRecord.getDocument();
			try {
				docs.insertString(docs.getLength(), msgRecord, attrset);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		});
	}

	// 这个是监听线程
	class UserHandler implements Runnable {
		private final Socket currentUserSocket;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		public UserHandler(Socket currentUserSocket) {
			this.currentUserSocket = currentUserSocket;
			try {
				ois = new ObjectInputStream(currentUserSocket.getInputStream());
				oos = new ObjectOutputStream(currentUserSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					//Object object = ois.readObject();
					Message msg = (Message) ois.readObject();
					System.out.println(msg);
					// instanceof是一种关键字，用于测试前者的类型是否为后者
					if (msg instanceof UserStateMessage) {
						// 处理用户状态消息
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						// 处理聊天消息
						processChatMessage((ChatMessage) msg);
					} else if (msg instanceof FileTranferMessage) {
						// 处理文件传输请求消息
						processFileMessage((FileTranferMessage) msg);
					} else if (msg instanceof FileTranferReceiveMessage) {
						// 处理文件传输响应消息
						processFileReceiveMessage((FileTranferReceiveMessage) msg);
					}
					else {
						// 这种情况对应着用户发来的消息格式 错误，应该发消息提示用户，这里从略
						System.err.println("用户发来的消息格式错误!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("客户端退出");
					
//					for(int i=0;i<tableOnlineUsers.getRowCount();i++)
//					{
//						System.out.println("我在查找了！");
//						String userForCheck = (String) tableOnlineUsers.getValueAt(i, 0);
//						if (!userManager.hasUser(userForCheck)) {
//							userManager.removeUser(userForCheck);
//							onlineUsersDtm.removeRow(i);
//						}
//					}
					String userForCheck = userManager.fromSocketToUserName(currentUserSocket);
					userManager.removeUser(userForCheck);
					for(int i=0;i<tableOnlineUsers.getRowCount();i++)
					{
						if (userForCheck == (String) tableOnlineUsers.getValueAt(i, 0)) {
							onlineUsersDtm.removeRow(i);
						}
					}
					
					// 如果用户未发送下线消息就直接关闭了客户端，应该在这里补充代码，删除用户在线信息
				} else {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (currentUserSocket != null) {
					try {
						currentUserSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 向其它用户转发消息
		private void transferMsgToOtherUsers(Message msg) {
			String[] users = userManager.getAllUsers();
			for (String user : users) {
				if (userManager.getUserSocket(user) != currentUserSocket) {
					try {
						ObjectOutputStream o = userManager.getUserOos(user);
						synchronized (o) {
							o.writeObject(msg);
							o.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 处理用户状态消息
		private void processUserStateMessage(UserStateMessage msg) {
			System.out.println(msg);
			String srcUser = msg.getSrcUser();
			if (msg.isUserOnline()) { // 用户上线消息
				if (userManager.hasUser(srcUser)) {
					// 这种情况对应着用户重复登录，应该发消息提示客户端，这里从略

					System.err.println("用户重复登录");
					return;
				}
				// 向新上线的用户转发当前在线用户列表
				String[] users = userManager.getAllUsers();
				try {
					for (String user : users) {
						UserStateMessage userStateMessage = new UserStateMessage(user, srcUser, true);
						synchronized (oos) {
							oos.writeObject(userStateMessage);
							oos.flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 向所有其它在线用户转发用户上线消息
				transferMsgToOtherUsers(msg);
				// 将用户信息加入到“在线用户”列表中
				//System.out.println("想加dtm了！");
				onlineUsersDtm.addRow(new Object[] { srcUser, currentUserSocket.getInetAddress().getHostAddress(),
						currentUserSocket.getPort(), dateFormat.format(new Date()) });
				userManager.addUser(srcUser, currentUserSocket, oos, ois);
				// 用绿色文字将用户名和用户上线时间添加到“消息记录”文本框中
				String ip = currentUserSocket.getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "上线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
			} else { // 用户下线消息
				if (!userManager.hasUser(srcUser)) {
					// 这种情况对应着用户未发送上线消息就直接发送了下线消息，应该发消息提示客户端，这里从略
					System.err.println("用户未发送登录消息就发送了下线消息");
					return;
				}
				// 用绿色文字将用户名和用户下线时间添加到“消息记录”文本框中
				String ip = userManager.getUserSocket(srcUser).getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "下线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				// 在“在线用户列表”中删除下线用户
				userManager.removeUser(srcUser);
				for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
					if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
						onlineUsersDtm.removeRow(i);
					}
				}
				// 将用户下线消息转发给所有其它在线用户
				transferMsgToOtherUsers(msg);
			}
		}

		//处理文件传输请求消息的方法
		private void processFileMessage(FileTranferMessage msg)
		{
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			if (userManager.hasUser(srcUser)) {
				final String transferFile = dateFormat.format(new Date()) + " " + srcUser
						+ "想要传输文件" + msg.getFileName() + "给" + dstUser + "\r\n";
				addMsgRecord(transferFile, Color.BLACK, 13, false, false);
				ObjectOutputStream oos = userManager.getUserOos(dstUser);
				if (userManager.getUserSocket(dstUser)!=currentUserSocket) {
					try {
						synchronized (oos) {
							oos.writeObject(msg);
							oos.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//处理用户发来的文件传输响应消息
		private void processFileReceiveMessage(FileTranferReceiveMessage msg)
		{
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			boolean userPurpose = msg.getPurpose();
			if (userManager.hasUser(srcUser)) {
				StringBuffer fileTransferReceiveRecord = new StringBuffer();
				fileTransferReceiveRecord.append(dateFormat.format(new Date()));
				fileTransferReceiveRecord.append(" ");
				fileTransferReceiveRecord.append(dstUser);
				fileTransferReceiveRecord.append("的传输文件请求被");
				fileTransferReceiveRecord.append(srcUser);
				if (userPurpose) {
					fileTransferReceiveRecord.append("接受了");
				}
				else {
					fileTransferReceiveRecord.append("拒绝了");
				}
				fileTransferReceiveRecord.append("\r\n");
				addMsgRecord(fileTransferReceiveRecord.toString(), Color.BLACK, 13, false, false);
				//oos = userManager.getUserOos(dstUser);
				ObjectOutputStream oos = userManager.getUserOos(dstUser);
				if (userManager.getUserSocket(dstUser)!=currentUserSocket) {
					try {
						System.out.println("??");
						synchronized (oos) {
							oos.writeObject(msg);
							oos.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// 处理用户发来的聊天消息
		private void processChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (userManager.hasUser(srcUser)) {
				// 用黑色文字将收到消息的时间、发送消息的用户名和消息内容添加到“消息记录”文本框中

				if (msg.isPubChatMessage()) {
					// 将公聊消息转发给所有其它在线用户
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "对大家说: " + msgContent + "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
					transferMsgToOtherUsers(msg);
				} else {
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "对" + dstUser + "说: " + msgContent + "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
					try {
						//为什么要用对象输出流？按理说，把对象传输给目标用户，应该是目标用户收到对象，理应使用对象输入流啊
						//因为输入输出是针对某个进程来说的
						//ObjectInputStream ois = new ObjectInputStream(userManager.getUserOis(dstUser));
						ObjectOutputStream oos = userManager.getUserOos(dstUser);
						if (userManager.getUserSocket(dstUser)!=currentUserSocket) {
							// 将私聊消息转发给目标用户，这里未实现（已实现）
							synchronized (oos) {
								oos.writeObject(msg);
								oos.flush();
							} 
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				// 这种情况对应着用户未发送上线消息就直接发送了聊天消息，应该发消息提示客户端，这里从略
				System.err.println("用户未发送上线消息就直接发送了聊天消息");
				talkWithoutOnlineMessage errorMsg = new talkWithoutOnlineMessage(srcUser, "root");
				synchronized (oos) {
					try {
						oos.writeObject(errorMsg);
						oos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return;
			}
		}
	}

}

// 管理在线用户信息
class UserManager {
	private final Map<String, User> onLineUsers;

	public UserManager() {
		onLineUsers = new HashMap<String, User>();
	}

	// 判断某用户是否在线
	public boolean hasUser(String userName) {
		return onLineUsers.containsKey(userName);
	}

	// 判断在线用户列表是否空
	public boolean isEmpty() {
		return onLineUsers.isEmpty();
	}

	// 获取在线用户的Socket的的输出流封装成的对象输出流
	public ObjectOutputStream getUserOos(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOos();
		}
		return null;
	}

	// 获取在线用户的Socket的的输入流封装成的对象输入流
	public ObjectInputStream getUserOis(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOis();
		}
		return null;
	}

	// 获取在线用户的Socket
	public Socket getUserSocket(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getSocket();
		}
		return null;
	}

	// 添加在线用户
	public boolean addUser(String userName, Socket userSocket) {
		if ((userName != null) && (userSocket != null)) {
			onLineUsers.put(userName, new User(userSocket));
			return true;
		}
		return false;
	}
	
	public String fromSocketToUserName(Socket socket)
	{
		for (Map.Entry<String, User> entry : onLineUsers.entrySet()) {
			if (entry.getValue().getSocket().equals(socket)) {
				return entry.getKey();
			}
		}
		return "NullName";
	}

	// 添加在线用户
	public boolean addUser(String userName, Socket userSocket, ObjectOutputStream oos, ObjectInputStream ios) {
		if ((userName != null) && (userSocket != null) && (oos != null) && (ios != null)) {
			onLineUsers.put(userName, new User(userSocket, oos, ios));
			return true;
		}
		return false;
	}

	// 删除在线用户
	public boolean removeUser(String userName) {
		if (hasUser(userName)) {
			onLineUsers.remove(userName);
			return true;
		}
		return false;
	}

	// 获取所有在线用户名
	public String[] getAllUsers() {
		String[] users = new String[onLineUsers.size()];
		int i = 0;
		for (Map.Entry<String, User> entry : onLineUsers.entrySet()) {
			users[i++] = entry.getKey();
		}
		return users;
	}

	// 获取在线用户个数
	public int getOnlineUserCount() {
		return onLineUsers.size();
	}
}

class User {
	private final Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private final Date logonTime;

	public User(Socket socket) {
		this.socket = socket;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois, Date logonTime) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		this.logonTime = logonTime;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public ObjectInputStream getOis() {
		return ois;
	}

	public Date getLogonTime() {
		return logonTime;
	}

}
