package com.cauc.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.JTabbedPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Client extends JFrame {
	private final int port = 9999;
	private SSLSocket socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	private String localUserName;
	private LinkedList<ServerSocketSet> serverSocketSets;
	private LinkedList<FileList> fileLists;

	// “在线用户列表ListModel”,用于维护“在线用户列表”中显示的内容
	private final DefaultListModel<String> onlinUserDlm = new DefaultListModel<String>();
	// 用于控制时间信息显示格式
	// private final SimpleDateFormat dateFormat = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private final JPanel contentPane;
	private final JTextField textFieldUserName;
	private final JPasswordField passwordFieldPwd;
	private final JTextField textFieldMsgToSend;
	private final JList<String> listOnlineUsers;
	private final JTabbedPane msgtabbedPane;
	private final JButton btnLogon;
	private final JButton btnSendMsg;
	private final JButton btnSendFile;
	private final JTextPane totalmsgtextPane;
	private LinkedList<tabNameToCountumber> tabNameToCountumbers;
	private int whisperCount;
	private JScrollPane totalMsgscrollPane;
	private long fileLen;
	private JTextField ServerIPaddrTextField;
	private JLabel lblNewLabel;
	private String userName;

	public static void main(String[] args) {

		EventQueue.invokeLater(() -> {
			try {
//				Client frame = new Client();
//				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	public Client(String userName) throws Exception{
		localUserName = userName;
		tabNameToCountumbers = new LinkedList<>();
		serverSocketSets = new LinkedList<>();
		fileLists = new LinkedList<>();
		whisperCount = 0;
		setTitle("\u5BA2\u6237\u7AEF");

		// 打开密钥库
		//String keyStoreFile = "test.keys";
		String passPhrase = "654321";
		char[] password = passPhrase.toCharArray();
		String trustStoreFile = "test.keys";

		// 指定密钥库类型
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(trustStoreFile), password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		// 安全套接字协议的实现
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, tmf.getTrustManagers(), null);

		// 声明可以生出安全连接的socket工厂
		SSLSocketFactory factory = sslContext.getSocketFactory();
		//socket = (SSLSocket)factory.createSocket(host, port)

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", false);
				try {
					synchronized (oos) {
						oos.writeObject(userStateMessage);
						oos.flush();
					}
					System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (NullPointerException e2) {
					System.exit(0);
				}
			}
		});
		
		setBounds(100, 100, 632, 419);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panelNorth = new JPanel();
		panelNorth.setBorder(new EmptyBorder(0, 0, 5, 0));
		contentPane.add(panelNorth, BorderLayout.NORTH);
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.X_AXIS));

		JLabel lblUserName = new JLabel("\u7528\u6237\u540D\uFF1A");
		panelNorth.add(lblUserName);

		textFieldUserName = new JTextField();
		panelNorth.add(textFieldUserName);
		textFieldUserName.setColumns(10);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panelNorth.add(horizontalStrut);

		JLabel lblPwd = new JLabel("\u53E3\u4EE4\uFF1A");
		panelNorth.add(lblPwd);

		passwordFieldPwd = new JPasswordField();
		passwordFieldPwd.setColumns(10);
		panelNorth.add(passwordFieldPwd);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panelNorth.add(horizontalStrut_1);

		lblNewLabel = new JLabel("ip\uFF1A");
		panelNorth.add(lblNewLabel);

		ServerIPaddrTextField = new JTextField();
		panelNorth.add(ServerIPaddrTextField);
		ServerIPaddrTextField.setColumns(10);

		btnLogon = new JButton("\u767B\u5F55"); // “登录”按钮

		panelNorth.add(btnLogon);

		JSplitPane splitPaneCenter = new JSplitPane();
		splitPaneCenter.setResizeWeight(1.0);
		contentPane.add(splitPaneCenter, BorderLayout.CENTER);

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setViewportBorder(
				new TitledBorder(null, "\u5728\u7EBF\u7528\u6237", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneCenter.setRightComponent(scrollPaneOnlineUsers);

		listOnlineUsers = new JList<String>(onlinUserDlm);
		listOnlineUsers.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getClickCount() == 2) {
					boolean isInTabbedPane = false;
					for (tabNameToCountumber tabNameToCountumber : tabNameToCountumbers) {
						if (tabNameToCountumber.tabName.equals(listOnlineUsers.getSelectedValue())) {
							isInTabbedPane = true;
						}
					}
					if (!isInTabbedPane && listOnlineUsers.getSelectedValue() != "") {
						addTabbedPanel(listOnlineUsers.getSelectedValue());
					}
				}
			}
		});
		scrollPaneOnlineUsers.setViewportView(listOnlineUsers);

		msgtabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPaneCenter.setLeftComponent(msgtabbedPane);

		totalMsgscrollPane = new JScrollPane();
		msgtabbedPane.addTab("综合聊天消息", null, totalMsgscrollPane, null);

		totalmsgtextPane = new JTextPane();
		totalmsgtextPane.setBackground(new Color(255, 255, 255));
		totalMsgscrollPane.setViewportView(totalmsgtextPane);

		JPanel panelSouth = new JPanel();
		panelSouth.setBorder(new EmptyBorder(5, 0, 0, 0));
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));

		textFieldMsgToSend = new JTextField();
		panelSouth.add(textFieldMsgToSend);
		textFieldMsgToSend.setColumns(10);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		panelSouth.add(horizontalStrut_2);

		btnSendMsg = new JButton("\u53D1\u9001\u6D88\u606F"); // “发送消息”按钮

		panelSouth.add(btnSendMsg);

		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		panelSouth.add(horizontalStrut_3);

		btnSendFile = new JButton("\u53D1\u9001\u6587\u4EF6");
		btnSendFile.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser jFileChooser = new JFileChooser();
				File file = null;
				int value = jFileChooser.showOpenDialog(Client.this);
				if (value == JFileChooser.APPROVE_OPTION) {
					file = jFileChooser.getSelectedFile();
					fileLen = file.length();
					try {
						String dstUser = msgtabbedPane.getTitleAt(msgtabbedPane.getSelectedIndex());
						fileLists.add(new FileList(file.getAbsolutePath(), dstUser));
						FileTranferMessage ftMsg = new FileTranferMessage(localUserName, dstUser, file.getName(), file.length());
						synchronized (oos) {
							oos.writeObject(ftMsg);
							oos.flush();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		panelSouth.add(btnSendFile);
		
		
		try {
			socket = (SSLSocket) factory.createSocket(InetAddress.getLocalHost(), port);
			// 将socket的输入流和输出流分别封装成对象输入流和对象输出流
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e1) {
			JOptionPane.showMessageDialog(null, "找不到服务器主机");
			System.exit(0);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "服务器I/O错误，服务器未启动？");
			System.exit(0);
		}
		UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", true);
		try {
			//System.out.println("我肯定发了啊！");
			oos.writeObject(userStateMessage);
			oos.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		

		// 将发送文件按钮设为不可用状态
		btnSendFile.setEnabled(false);
		// 将发送消息按钮设为不可用状态
		btnSendMsg.setEnabled(false);

		btnLogon.addActionListener((e) -> {
			if (btnLogon.getText().equals("登录")) {
				localUserName = textFieldUserName.getText().trim();
				if (localUserName.length() > 0) {
					// 与服务器端建立Socket连接，如果抛出异常，则弹出对话框通知用户，并退出
					try {
						//byte[] serverIPAddr = ServerIPaddrTextField.getText().getBytes();
						//socket = new Socket(serverIPAddr, port);
						//System.out.println(InetAddress.getByName(ServerIPaddrTextField.getText()).getHostAddress());
						//socket = (SSLSocket) factory.createSocket(InetAddress.getByName(ServerIPaddrTextField.getText()).getHostAddress(), port);
						//socket = (SSLSocket) factory.createSocket(ServerIPaddrTextField.getText(), port);
						socket = (SSLSocket) factory.createSocket(InetAddress.getLocalHost(), port);
						// 将socket的输入流和输出流分别封装成对象输入流和对象输出流
						oos = new ObjectOutputStream(socket.getOutputStream());
						ois = new ObjectInputStream(socket.getInputStream());
					} catch (UnknownHostException e1) {
						JOptionPane.showMessageDialog(null, "找不到服务器主机");
						System.exit(0);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "服务器I/O错误，服务器未启动？");
						System.exit(0);
					}
					// 向服务器发送用户上线信息，将自己的用户名和IP地址发送给服务器
//					UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", true);
//					try {
//						//System.out.println("我肯定发了啊！");
//						oos.writeObject(userStateMessage);
//						oos.flush();
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
					// 在“消息记录”文本框中用红色添加“XX时间登录成功”的信息
					String msgRecord = dateFormat.format(new Date()) + " 登录成功\r\n";
					addMsgRecord(msgRecord, Color.red, 12, false, false, totalMsgscrollPane);
					// 创建并启动“后台监听线程”,监听并处理服务器传来的信息
					new Thread(new ListeningHandler()).start();
					// 将“登录”按钮设为“退出”按钮
					btnLogon.setText("退出");
					// 将发送文件按钮设为可用状态
					btnSendFile.setEnabled(true);
					// 将发送消息按钮设为可用状态
					btnSendMsg.setEnabled(true);
				}
			} else if (btnLogon.getText().equals("退出")) {
				if (JOptionPane.showConfirmDialog(null, "是否退出?", "退出确认",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					// 向服务器发送用户下线消息
//					UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", false);
//					try {
//						synchronized (oos) {
//							oos.writeObject(userStateMessage);
//							oos.flush();
//						}
//						System.exit(0);
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
				}
			}

		});
		btnSendMsg.addActionListener((e) -> {
			String msgContent = textFieldMsgToSend.getText();
			if (msgtabbedPane.getSelectedIndex() == 0) {
				// 将消息文本框中的内容作为公聊消息发送给服务器
				ChatMessage chatMessage = new ChatMessage(localUserName, "", msgContent);
				try {
					synchronized (oos) {
						oos.writeObject(chatMessage);
						oos.flush();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// 在“消息记录”文本框中用蓝色显示发送的消息及发送时间
				String msgRecord = dateFormat.format(new Date()) + "向大家说:" + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.blue, 12, false, false, totalMsgscrollPane);
			} else if ((msgContent.equals("/exit")) && (msgtabbedPane.getSelectedIndex() != 0)) {
				int numTab = msgtabbedPane.getSelectedIndex();
				tabNameToCountumbers.remove(numTab - 1);
				msgtabbedPane.remove(msgtabbedPane.getSelectedIndex());
			} else {

				try {
					String whisperUser = msgtabbedPane.getTitleAt(msgtabbedPane.getSelectedIndex());
					ChatMessage whisperMessage = new ChatMessage(localUserName, whisperUser, msgContent);
					synchronized (oos) {
						oos.writeObject(whisperMessage);
						oos.flush();
					}
					String whisperRecord = dateFormat.format(new Date()) + "你对" + whisperUser + "说：" + msgContent + "\r\n";

					addMsgRecord(whisperRecord, Color.GREEN, 13, false, false,
							(JScrollPane) msgtabbedPane.getComponentAt(msgtabbedPane.getSelectedIndex()));
				} catch (IllegalStateException e2) {
					JOptionPane.showMessageDialog(null, "请输入正确的私聊语句。");
				} catch (IOException e2) {
					e2.printStackTrace();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}
		});
	}

	// 添加私聊tabpanel的方法
	private void addTabbedPanel(String whisperObject) {
		tabNameToCountumbers
				.add(new tabNameToCountumber(whisperCount, whisperObject, new JScrollPane(), new JTextPane()));
		tabNameToCountumber tabNum = tabNameToCountumbers.getLast();
		JScrollPane whisperScrollPane = tabNum.jScrollPaneForWhisper;
		JTextPane whisperTextPane = tabNum.jTextPaneForWhisper;
		whisperScrollPane.setBackground(new Color(255, 255, 255));
		whisperScrollPane.setViewportView(whisperTextPane);
		msgtabbedPane.addTab(whisperObject, whisperScrollPane);
	}

	// 向指定消息记录文本框中添加一条消息记录
	private void addMsgRecord(final String msgRecord, Color msgColor, int fontSize, boolean isItalic,
			boolean isUnderline, JScrollPane addedJScrollPane) {
		final SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, msgColor);
		StyleConstants.setFontSize(attrset, fontSize);
		StyleConstants.setUnderline(attrset, isUnderline);
		StyleConstants.setItalic(attrset, isItalic);
		System.out.println(addedJScrollPane.getViewport().getView());
		JTextPane jTextPane = (JTextPane) addedJScrollPane.getViewport().getView();

		Document docs = jTextPane.getDocument();
		try {
			docs.insertString(docs.getLength(), msgRecord, attrset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	// 后台监听线程
	class ListeningHandler implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					Message msg = null;
					try {
						synchronized (ois) {
							msg = (Message) ois.readObject();
						}
					} catch (EOFException e) {
						System.out.println("这是正常的！");
					}
					if (msg instanceof UserStateMessage) {
						// 处理用户状态消息
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						// 处理聊天消息
						processChatMessage((ChatMessage) msg);
					} else if (msg instanceof FileTranferMessage) {
						// 处理文件发送请求消息
						processFileTransferMessage((FileTranferMessage) msg);
					} else if (msg instanceof FileTranferReceiveMessage) {
						// 处理文件发送响应消息
						System.out.println("处理文件发送相应消息");
						processFileTranferReceiveMessage((FileTranferReceiveMessage) msg);
					} else if (msg instanceof talkWithoutOnlineMessage) {
						// 处理未上线就发送消息的异常消息
						JOptionPane.showMessageDialog(null, "你未在线上，因此发送消息失败");
					} else {
						// 这种情况对应着用户发来的消息格式 错误，应该发消息提示用户，这里从略
						System.err.println("用户发来的消息格式错误!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("服务器端退出");
				} else {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 处理文件发送请求消息的方法
		private void processFileTransferMessage(FileTranferMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String fileName = msg.getFileName();
			Object[] options = { "接收", "拒绝" };
			StringBuffer fileMsg = new StringBuffer("");
			long fileLen = msg.getLen();
			System.out.println("应该接收的文件大小是：" + fileLen);
			fileMsg.append(srcUser);
			fileMsg.append("给你发来了");
			fileMsg.append(fileName);
			fileMsg.append("你是否要接收？");
			int chosen = JOptionPane.showOptionDialog(null, fileMsg.toString(), "接收确认框", JOptionPane.OK_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			// Socket socketForFileTransfer = null;
			// ServerSocket serverSocketForFilrTransfer = null;
			try {
				FileTranferReceiveMessage fileTranferReceiveMessage = null;
				if (chosen == 0) {
					// serverSocketForFilrTransfer = new ServerSocket(0);

					serverSocketSets.add(new ServerSocketSet(dstUser, srcUser));
					fileTranferReceiveMessage = new FileTranferReceiveMessage(localUserName, srcUser, true,
							serverSocketSets.getLast().getPort(), fileName, fileLen);

					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								Socket gettedSocket = serverSocketSets.getLast().getServerSocket().accept();
								DataOutputStream fileOut = new DataOutputStream(
										new BufferedOutputStream(new FileOutputStream("E://" + fileName)));
								InputStream in = gettedSocket.getInputStream();
								byte cmd[] = new byte[8192];
								FileTranfer fileTranfer = new FileTranfer();
								fileTranfer.setVisible(true);
								long readedLen = 0;

								byte[] inValue = new byte[16];
								
								in.read(inValue);
								IvParameterSpec iv = new IvParameterSpec(inValue);
								MessageDigest pDigest = MessageDigest.getInstance("SHA-512");
								byte[] keyValue = pDigest.digest("ftft.abcd1234".getBytes());
								SecretKeySpec spec = new SecretKeySpec(keyValue, 0, 16, "AES");
								byte[] userPassword = pDigest.digest("ftft.abcd1234".getBytes());
								CipherInputStream cis = null;
								Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
								cipher.init(Cipher.DECRYPT_MODE, spec,iv);
								cis = new CipherInputStream(in,cipher);
								
								while (true) {
									int read = 0;
									if (in != null) {
										read = cis.read(cmd);
									}
									readedLen += read;
									if (readedLen > fileLen / 100) {
										fileTranfer.valueplus();
										readedLen = 0;
									}
									//System.out.println(readedLen);
									//System.out.println(read);
									if (read == -1) {
										//System.out.println("该跳出循环了吧？");
										fileTranfer.changeValue(100);
										break;
									}
									fileOut.write(cmd, 0, read);
								}
								JOptionPane.showMessageDialog(null, "文件接收完成");
								cis.close();
								fileOut.close();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							} catch (NoSuchPaddingException e) {
								e.printStackTrace();
							} catch (InvalidKeyException e) {
								e.printStackTrace();
							} catch (InvalidAlgorithmParameterException e) {
								e.printStackTrace();
							}
						}
					}).start();

				} else if (chosen == 1) {
					fileTranferReceiveMessage = new FileTranferReceiveMessage(localUserName, srcUser, false, 0,
							fileName, fileLen);
				}
				System.out.println(fileTranferReceiveMessage);
				synchronized (oos) {
					oos.writeObject(fileTranferReceiveMessage);
					oos.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 处理文件发送响应消息的方法
		private void processFileTranferReceiveMessage(FileTranferReceiveMessage msg) {
			System.out.println("我进入到处理文件发送响应消息的方法中了！");
			if (msg.getPurpose()) {
				// String savePath = "E://";
				String srcUser = msg.getSrcUser();
				String dstUser = msg.getDstUser();
				String fileName = msg.getFileName();
				int portForTransfer = msg.getPort();
				long fileSize = msg.getFileLen();
				System.out.println("文件长度为：" + fileSize);
				// File file =
				System.out.println("端口为：" + portForTransfer);
				EventQueue.invokeLater(() -> {
					Socket socketForTransfer;
					try {
						socketForTransfer = new Socket(InetAddress.getLocalHost(), portForTransfer);
						OutputStream out = socketForTransfer.getOutputStream();
						String fileAbPath = null;
						// System.out.println(fileLists.size());
						for (int i = 0; i < fileLists.size(); i++) {
							System.out.println(fileLists.get(i).fileAbsolutelyPath);
							if (fileLists.get(i).userNameToFile == srcUser) {
								fileAbPath = fileLists.get(i).fileAbsolutelyPath;
							}
						}
						System.out.println(fileAbPath);
						FileInputStream fis = new FileInputStream(new File(fileAbPath));
						
						
						MessageDigest mDigest = MessageDigest.getInstance("SHA-512");
						byte[] keyValue = mDigest.digest(new String("ftft.abcd1234").getBytes());
						SecretKeySpec spec = new SecretKeySpec(keyValue, 0, 16, "AES");
						byte[] ivValue = new byte[16];
						Random random = new Random(System.currentTimeMillis());
						random.nextBytes(ivValue);
						IvParameterSpec iv = new IvParameterSpec(ivValue);
						Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
						cipher.init(Cipher.ENCRYPT_MODE, spec, iv);
						CipherInputStream cis = new CipherInputStream(fis, cipher);
						
						
						
						DataOutputStream dos = new DataOutputStream(out);
						byte[] bs = new byte[8192];

						dos.write(ivValue);
						while (true) {
							int read = 0;
							if (cis != null) {
								read = cis.read(bs);
							}
							if (read == -1) {
								System.out.println("发送方已经读完文件了！");
								break;
							}
							dos.write(bs, 0, read);

						}
						dos.flush();
						fis.close();
						// 这里我少关了一个流，导致接收方一直在阻塞等待传输，我擦咧怪不得文件不完整
						dos.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (NoSuchPaddingException e) {
						e.printStackTrace();
					} catch (InvalidKeyException e) {
						e.printStackTrace();
					} catch (InvalidAlgorithmParameterException e) {
						e.printStackTrace();
					}

				});
			}

		}

		// 处理用户状态消息
		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			if (msg.isUserOnline()) {
				if (msg.isPubUserStateMessage()) { // 新用户上线消息
					// 用绿色文字将用户名和用户上线时间添加到“消息记录”文本框中
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "上线了!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false, totalMsgscrollPane);
					// 在“在线用户”列表中增加新上线的用户名
					onlinUserDlm.addElement(srcUser);
				}
				if (dstUser.equals(localUserName)) { // 用户在线消息
					onlinUserDlm.addElement(srcUser);
				}
			} else if (msg.isUserOffline()) { // 用户下线消息
				if (onlinUserDlm.contains(srcUser)) {
					// 用绿色文字将用户名和用户下线时间添加到“消息记录”文本框中
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "下线了!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false, totalMsgscrollPane);
					// 在“在线用户”列表中删除下线的用户名
					onlinUserDlm.removeElement(srcUser);
				}
			}
		}

		// 处理服务器转发来的公聊消息
		private void processChatMessage(ChatMessage msg) {

			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (onlinUserDlm.contains(srcUser)) {
				if (msg.isPubChatMessage()) {
					// 用黑色文字将收到消息的时间、发送消息的用户名和消息内容添加到“消息记录”文本框中
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "说: " + msgContent
							+ "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false, totalMsgscrollPane);
				} else {
					boolean isInTabbedPane = false;
					int inTabbedPaneNumber = -1;
					for (int i = 0; i < tabNameToCountumbers.size(); i++) {
						if (tabNameToCountumbers.get(i).tabName.equals(srcUser)) {
							isInTabbedPane = true;
							inTabbedPaneNumber = i;
							break;
						}
					}
					final String whisperedRecord = dateFormat.format(new Date()) + srcUser + "对你说：" + msgContent + "\r\n";
					if (!isInTabbedPane) {
						whisperCount++;
						addTabbedPanel(srcUser);
						addMsgRecord(whisperedRecord, Color.BLUE, 13, false, true,
								tabNameToCountumbers.get(whisperCount - 1).jScrollPaneForWhisper);
					} else {
						addMsgRecord(whisperedRecord, Color.BLUE, 12, false, false,
								tabNameToCountumbers.get(inTabbedPaneNumber).jScrollPaneForWhisper);
					}
				}
			}
		}
	}
}
