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

	// �������û��б�ListModel��,����ά���������û��б�����ʾ������
	private final DefaultListModel<String> onlinUserDlm = new DefaultListModel<String>();
	// ���ڿ���ʱ����Ϣ��ʾ��ʽ
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

		// ����Կ��
		//String keyStoreFile = "test.keys";
		String passPhrase = "654321";
		char[] password = passPhrase.toCharArray();
		String trustStoreFile = "test.keys";

		// ָ����Կ������
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(trustStoreFile), password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		// ��ȫ�׽���Э���ʵ��
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, tmf.getTrustManagers(), null);

		// ��������������ȫ���ӵ�socket����
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

		btnLogon = new JButton("\u767B\u5F55"); // ����¼����ť

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
		msgtabbedPane.addTab("�ۺ�������Ϣ", null, totalMsgscrollPane, null);

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

		btnSendMsg = new JButton("\u53D1\u9001\u6D88\u606F"); // ��������Ϣ����ť

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
			// ��socket����������������ֱ��װ�ɶ����������Ͷ��������
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e1) {
			JOptionPane.showMessageDialog(null, "�Ҳ�������������");
			System.exit(0);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "������I/O���󣬷�����δ������");
			System.exit(0);
		}
		UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", true);
		try {
			//System.out.println("�ҿ϶����˰���");
			oos.writeObject(userStateMessage);
			oos.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		

		// �������ļ���ť��Ϊ������״̬
		btnSendFile.setEnabled(false);
		// ��������Ϣ��ť��Ϊ������״̬
		btnSendMsg.setEnabled(false);

		btnLogon.addActionListener((e) -> {
			if (btnLogon.getText().equals("��¼")) {
				localUserName = textFieldUserName.getText().trim();
				if (localUserName.length() > 0) {
					// ��������˽���Socket���ӣ�����׳��쳣���򵯳��Ի���֪ͨ�û������˳�
					try {
						//byte[] serverIPAddr = ServerIPaddrTextField.getText().getBytes();
						//socket = new Socket(serverIPAddr, port);
						//System.out.println(InetAddress.getByName(ServerIPaddrTextField.getText()).getHostAddress());
						//socket = (SSLSocket) factory.createSocket(InetAddress.getByName(ServerIPaddrTextField.getText()).getHostAddress(), port);
						//socket = (SSLSocket) factory.createSocket(ServerIPaddrTextField.getText(), port);
						socket = (SSLSocket) factory.createSocket(InetAddress.getLocalHost(), port);
						// ��socket����������������ֱ��װ�ɶ����������Ͷ��������
						oos = new ObjectOutputStream(socket.getOutputStream());
						ois = new ObjectInputStream(socket.getInputStream());
					} catch (UnknownHostException e1) {
						JOptionPane.showMessageDialog(null, "�Ҳ�������������");
						System.exit(0);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "������I/O���󣬷�����δ������");
						System.exit(0);
					}
					// ������������û�������Ϣ�����Լ����û�����IP��ַ���͸�������
//					UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", true);
//					try {
//						//System.out.println("�ҿ϶����˰���");
//						oos.writeObject(userStateMessage);
//						oos.flush();
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
					// �ڡ���Ϣ��¼���ı������ú�ɫ��ӡ�XXʱ���¼�ɹ�������Ϣ
					String msgRecord = dateFormat.format(new Date()) + " ��¼�ɹ�\r\n";
					addMsgRecord(msgRecord, Color.red, 12, false, false, totalMsgscrollPane);
					// ��������������̨�����̡߳�,�����������������������Ϣ
					new Thread(new ListeningHandler()).start();
					// ������¼����ť��Ϊ���˳�����ť
					btnLogon.setText("�˳�");
					// �������ļ���ť��Ϊ����״̬
					btnSendFile.setEnabled(true);
					// ��������Ϣ��ť��Ϊ����״̬
					btnSendMsg.setEnabled(true);
				}
			} else if (btnLogon.getText().equals("�˳�")) {
				if (JOptionPane.showConfirmDialog(null, "�Ƿ��˳�?", "�˳�ȷ��",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					// ������������û�������Ϣ
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
				// ����Ϣ�ı����е�������Ϊ������Ϣ���͸�������
				ChatMessage chatMessage = new ChatMessage(localUserName, "", msgContent);
				try {
					synchronized (oos) {
						oos.writeObject(chatMessage);
						oos.flush();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// �ڡ���Ϣ��¼���ı���������ɫ��ʾ���͵���Ϣ������ʱ��
				String msgRecord = dateFormat.format(new Date()) + "����˵:" + msgContent + "\r\n";
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
					String whisperRecord = dateFormat.format(new Date()) + "���" + whisperUser + "˵��" + msgContent + "\r\n";

					addMsgRecord(whisperRecord, Color.GREEN, 13, false, false,
							(JScrollPane) msgtabbedPane.getComponentAt(msgtabbedPane.getSelectedIndex()));
				} catch (IllegalStateException e2) {
					JOptionPane.showMessageDialog(null, "��������ȷ��˽����䡣");
				} catch (IOException e2) {
					e2.printStackTrace();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}
		});
	}

	// ���˽��tabpanel�ķ���
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

	// ��ָ����Ϣ��¼�ı��������һ����Ϣ��¼
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

	// ��̨�����߳�
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
						System.out.println("���������ģ�");
					}
					if (msg instanceof UserStateMessage) {
						// �����û�״̬��Ϣ
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						// ����������Ϣ
						processChatMessage((ChatMessage) msg);
					} else if (msg instanceof FileTranferMessage) {
						// �����ļ�����������Ϣ
						processFileTransferMessage((FileTranferMessage) msg);
					} else if (msg instanceof FileTranferReceiveMessage) {
						// �����ļ�������Ӧ��Ϣ
						System.out.println("�����ļ�������Ӧ��Ϣ");
						processFileTranferReceiveMessage((FileTranferReceiveMessage) msg);
					} else if (msg instanceof talkWithoutOnlineMessage) {
						// ����δ���߾ͷ�����Ϣ���쳣��Ϣ
						JOptionPane.showMessageDialog(null, "��δ�����ϣ���˷�����Ϣʧ��");
					} else {
						// ���������Ӧ���û���������Ϣ��ʽ ����Ӧ�÷���Ϣ��ʾ�û����������
						System.err.println("�û���������Ϣ��ʽ����!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("���������˳�");
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

		// �����ļ�����������Ϣ�ķ���
		private void processFileTransferMessage(FileTranferMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String fileName = msg.getFileName();
			Object[] options = { "����", "�ܾ�" };
			StringBuffer fileMsg = new StringBuffer("");
			long fileLen = msg.getLen();
			System.out.println("Ӧ�ý��յ��ļ���С�ǣ�" + fileLen);
			fileMsg.append(srcUser);
			fileMsg.append("���㷢����");
			fileMsg.append(fileName);
			fileMsg.append("���Ƿ�Ҫ���գ�");
			int chosen = JOptionPane.showOptionDialog(null, fileMsg.toString(), "����ȷ�Ͽ�", JOptionPane.OK_OPTION,
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
										//System.out.println("������ѭ���˰ɣ�");
										fileTranfer.changeValue(100);
										break;
									}
									fileOut.write(cmd, 0, read);
								}
								JOptionPane.showMessageDialog(null, "�ļ��������");
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

		// �����ļ�������Ӧ��Ϣ�ķ���
		private void processFileTranferReceiveMessage(FileTranferReceiveMessage msg) {
			System.out.println("�ҽ��뵽�����ļ�������Ӧ��Ϣ�ķ������ˣ�");
			if (msg.getPurpose()) {
				// String savePath = "E://";
				String srcUser = msg.getSrcUser();
				String dstUser = msg.getDstUser();
				String fileName = msg.getFileName();
				int portForTransfer = msg.getPort();
				long fileSize = msg.getFileLen();
				System.out.println("�ļ�����Ϊ��" + fileSize);
				// File file =
				System.out.println("�˿�Ϊ��" + portForTransfer);
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
								System.out.println("���ͷ��Ѿ������ļ��ˣ�");
								break;
							}
							dos.write(bs, 0, read);

						}
						dos.flush();
						fis.close();
						// �������ٹ���һ���������½��շ�һֱ�������ȴ����䣬�Ҳ��ֲֹ����ļ�������
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

		// �����û�״̬��Ϣ
		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			if (msg.isUserOnline()) {
				if (msg.isPubUserStateMessage()) { // ���û�������Ϣ
					// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "������!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false, totalMsgscrollPane);
					// �ڡ������û����б������������ߵ��û���
					onlinUserDlm.addElement(srcUser);
				}
				if (dstUser.equals(localUserName)) { // �û�������Ϣ
					onlinUserDlm.addElement(srcUser);
				}
			} else if (msg.isUserOffline()) { // �û�������Ϣ
				if (onlinUserDlm.contains(srcUser)) {
					// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "������!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false, totalMsgscrollPane);
					// �ڡ������û����б���ɾ�����ߵ��û���
					onlinUserDlm.removeElement(srcUser);
				}
			}
		}

		// ���������ת�����Ĺ�����Ϣ
		private void processChatMessage(ChatMessage msg) {

			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (onlinUserDlm.contains(srcUser)) {
				if (msg.isPubChatMessage()) {
					// �ú�ɫ���ֽ��յ���Ϣ��ʱ�䡢������Ϣ���û�������Ϣ������ӵ�����Ϣ��¼���ı�����
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "˵: " + msgContent
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
					final String whisperedRecord = dateFormat.format(new Date()) + srcUser + "����˵��" + msgContent + "\r\n";
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
