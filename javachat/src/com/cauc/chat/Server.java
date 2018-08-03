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
	// ���������û����û�����Socket��Ϣ
	private final UserManager userManager = new UserManager();
	// �������û��б�ListModel��,����ά���������û��б�����ʾ������
	final DefaultTableModel onlineUsersDtm = new DefaultTableModel();
	// ���ڿ���ʱ����Ϣ��ʾ��ʽ
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

		onlineUsersDtm.addColumn("�û���");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("�˿�");
		onlineUsersDtm.addColumn("��¼ʱ��");
		onlineUsersDtm.addColumn("�û�״̬");
		tableOnlineUsers = new JTable(onlineUsersDtm);
		tableOnlineUsers.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				try {
					if (tableOnlineUsers.getSelectedColumn() == 0) {
						String userNameDelete = (String) tableOnlineUsers.getValueAt(tableOnlineUsers.getSelectedRow(),
								tableOnlineUsers.getSelectedColumn());
						System.out.println(userNameDelete);
						System.out.println(tableOnlineUsers.getSelectedRow());
						String deleteMsg = "�Ƿ�ɾ���û�" + userNameDelete;
						Object[] options = { "ȷ��ɾ��", "ȡ��" };
						int chosen = JOptionPane.showOptionDialog(null, deleteMsg, "ɾ��ȷ�Ͽ�", JOptionPane.OK_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (chosen == 0) {
							//System.out.println("��Ҫɾ���û��ˣ�");
							userManager.removeUser(userNameDelete);
							//tableOnlineUsers.remove(tableOnlineUsers.getSelectedRow());
							onlineUsersDtm.removeRow(tableOnlineUsers.getSelectedRow());
						} 
					}
				} catch (HeadlessException e) {
					e.printStackTrace();
				} catch (ClassCastException e2) {
					JOptionPane.showInternalMessageDialog(null, "�����û������߳��û���");
				}
			}
		});
		
		
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true); // ��JTable������������
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton btnStart = new JButton("\u542F\u52A8");
		// "����"��ť
		btnStart.addActionListener((e) -> {
			try {
				// ����ServerSocket�򿪶˿�9999�����ͻ�������
				// SSLServerSocket��create�����ڶ���������֧�����ӵ����ͻ�����
				serverSocket = (SSLServerSocket) factory.createServerSocket(port, 20, InetAddress.getLocalHost());
				// �ڡ���Ϣ��¼���ı������ú�ɫ��ʾ�������������ɹ�X��������ʱ����Ϣ
				String msgRecord = dateFormat.format(new Date()) + " �����������ɹ�" + "\r\n";
				addMsgRecord(msgRecord, Color.red, 12, false, false);
				// �����������������û������̡߳������ܲ�����ͻ�����������
				new Thread(() -> {
					while (true) {
						try {
							// ����serverSocket.accept()���������û���������
							Socket socket = serverSocket.accept();
							// Ϊ�������û��������������û������̡߳�
							// ����serverSocket.accept()�������ص�socket���󽻸����û������̡߳�������
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
	
	//ͨ����Կ���ļ�����SSL��ȫ���׽��ֹ���
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

	// ����Ϣ��¼�ı��������һ����Ϣ��¼
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

	// ����Ǽ����߳�
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
					// instanceof��һ�ֹؼ��֣����ڲ���ǰ�ߵ������Ƿ�Ϊ����
					if (msg instanceof UserStateMessage) {
						// �����û�״̬��Ϣ
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						// ����������Ϣ
						processChatMessage((ChatMessage) msg);
					} else if (msg instanceof FileTranferMessage) {
						// �����ļ�����������Ϣ
						processFileMessage((FileTranferMessage) msg);
					} else if (msg instanceof FileTranferReceiveMessage) {
						// �����ļ�������Ӧ��Ϣ
						processFileReceiveMessage((FileTranferReceiveMessage) msg);
					}
					else {
						// ���������Ӧ���û���������Ϣ��ʽ ����Ӧ�÷���Ϣ��ʾ�û����������
						System.err.println("�û���������Ϣ��ʽ����!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("�ͻ����˳�");
					
//					for(int i=0;i<tableOnlineUsers.getRowCount();i++)
//					{
//						System.out.println("���ڲ����ˣ�");
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
					
					// ����û�δ����������Ϣ��ֱ�ӹر��˿ͻ��ˣ�Ӧ�������ﲹ����룬ɾ���û�������Ϣ
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

		// �������û�ת����Ϣ
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

		// �����û�״̬��Ϣ
		private void processUserStateMessage(UserStateMessage msg) {
			System.out.println(msg);
			String srcUser = msg.getSrcUser();
			if (msg.isUserOnline()) { // �û�������Ϣ
				if (userManager.hasUser(srcUser)) {
					// ���������Ӧ���û��ظ���¼��Ӧ�÷���Ϣ��ʾ�ͻ��ˣ��������

					System.err.println("�û��ظ���¼");
					return;
				}
				// �������ߵ��û�ת����ǰ�����û��б�
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
				// ���������������û�ת���û�������Ϣ
				transferMsgToOtherUsers(msg);
				// ���û���Ϣ���뵽�������û����б���
				//System.out.println("���dtm�ˣ�");
				onlineUsersDtm.addRow(new Object[] { srcUser, currentUserSocket.getInetAddress().getHostAddress(),
						currentUserSocket.getPort(), dateFormat.format(new Date()) });
				userManager.addUser(srcUser, currentUserSocket, oos, ois);
				// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
				String ip = currentUserSocket.getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "������!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
			} else { // �û�������Ϣ
				if (!userManager.hasUser(srcUser)) {
					// ���������Ӧ���û�δ����������Ϣ��ֱ�ӷ�����������Ϣ��Ӧ�÷���Ϣ��ʾ�ͻ��ˣ��������
					System.err.println("�û�δ���͵�¼��Ϣ�ͷ�����������Ϣ");
					return;
				}
				// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
				String ip = userManager.getUserSocket(srcUser).getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "������!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				// �ڡ������û��б���ɾ�������û�
				userManager.removeUser(srcUser);
				for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
					if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
						onlineUsersDtm.removeRow(i);
					}
				}
				// ���û�������Ϣת�����������������û�
				transferMsgToOtherUsers(msg);
			}
		}

		//�����ļ�����������Ϣ�ķ���
		private void processFileMessage(FileTranferMessage msg)
		{
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			if (userManager.hasUser(srcUser)) {
				final String transferFile = dateFormat.format(new Date()) + " " + srcUser
						+ "��Ҫ�����ļ�" + msg.getFileName() + "��" + dstUser + "\r\n";
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
		
		//�����û��������ļ�������Ӧ��Ϣ
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
				fileTransferReceiveRecord.append("�Ĵ����ļ�����");
				fileTransferReceiveRecord.append(srcUser);
				if (userPurpose) {
					fileTransferReceiveRecord.append("������");
				}
				else {
					fileTransferReceiveRecord.append("�ܾ���");
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
		
		// �����û�������������Ϣ
		private void processChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (userManager.hasUser(srcUser)) {
				// �ú�ɫ���ֽ��յ���Ϣ��ʱ�䡢������Ϣ���û�������Ϣ������ӵ�����Ϣ��¼���ı�����

				if (msg.isPubChatMessage()) {
					// ��������Ϣת�����������������û�
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "�Դ��˵: " + msgContent + "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
					transferMsgToOtherUsers(msg);
				} else {
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "��" + dstUser + "˵: " + msgContent + "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
					try {
						//ΪʲôҪ�ö��������������˵���Ѷ������Ŀ���û���Ӧ����Ŀ���û��յ�������Ӧʹ�ö�����������
						//��Ϊ������������ĳ��������˵��
						//ObjectInputStream ois = new ObjectInputStream(userManager.getUserOis(dstUser));
						ObjectOutputStream oos = userManager.getUserOos(dstUser);
						if (userManager.getUserSocket(dstUser)!=currentUserSocket) {
							// ��˽����Ϣת����Ŀ���û�������δʵ�֣���ʵ�֣�
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
				// ���������Ӧ���û�δ����������Ϣ��ֱ�ӷ�����������Ϣ��Ӧ�÷���Ϣ��ʾ�ͻ��ˣ��������
				System.err.println("�û�δ����������Ϣ��ֱ�ӷ�����������Ϣ");
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

// ���������û���Ϣ
class UserManager {
	private final Map<String, User> onLineUsers;

	public UserManager() {
		onLineUsers = new HashMap<String, User>();
	}

	// �ж�ĳ�û��Ƿ�����
	public boolean hasUser(String userName) {
		return onLineUsers.containsKey(userName);
	}

	// �ж������û��б��Ƿ��
	public boolean isEmpty() {
		return onLineUsers.isEmpty();
	}

	// ��ȡ�����û���Socket�ĵ��������װ�ɵĶ��������
	public ObjectOutputStream getUserOos(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOos();
		}
		return null;
	}

	// ��ȡ�����û���Socket�ĵ���������װ�ɵĶ���������
	public ObjectInputStream getUserOis(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOis();
		}
		return null;
	}

	// ��ȡ�����û���Socket
	public Socket getUserSocket(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getSocket();
		}
		return null;
	}

	// ��������û�
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

	// ��������û�
	public boolean addUser(String userName, Socket userSocket, ObjectOutputStream oos, ObjectInputStream ios) {
		if ((userName != null) && (userSocket != null) && (oos != null) && (ios != null)) {
			onLineUsers.put(userName, new User(userSocket, oos, ios));
			return true;
		}
		return false;
	}

	// ɾ�������û�
	public boolean removeUser(String userName) {
		if (hasUser(userName)) {
			onLineUsers.remove(userName);
			return true;
		}
		return false;
	}

	// ��ȡ���������û���
	public String[] getAllUsers() {
		String[] users = new String[onLineUsers.size()];
		int i = 0;
		for (Map.Entry<String, User> entry : onLineUsers.entrySet()) {
			users[i++] = entry.getKey();
		}
		return users;
	}

	// ��ȡ�����û�����
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
