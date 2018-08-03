package com.cauc.chat;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.JTextPane;

public class test extends JFrame {
	private LinkedList<tabNameToCountumber> tabNameToCountumbers;
	private int tabPaneCount;
	public test() {
		tabPaneCount = 0;
		tabNameToCountumbers = new LinkedList<>();
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JButton plus = new JButton("\u52A0");
		plus.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				tabNameToCountumbers.add(new tabNameToCountumber(tabPaneCount, "1111", new JScrollPane(),
						new JTextPane()));
				tabbedPane.addTab(tabNameToCountumbers.get(tabPaneCount).tabName,
						tabNameToCountumbers.get(tabPaneCount).jScrollPaneForWhisper);
				tabNameToCountumbers.get(tabPaneCount).jTextPaneForWhisper.setBackground(new Color(255, 255, 255));
				tabNameToCountumbers.get(tabPaneCount).jScrollPaneForWhisper.setViewportView(tabNameToCountumbers.get(tabPaneCount).jTextPaneForWhisper);
				tabPaneCount++;
			}
		});
		getContentPane().add(plus, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("\u51CF");
		btnNewButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				//System.out.println(tabbedPane.getComponentCount());
				JScrollPane jScrollPane = (JScrollPane) tabbedPane.getComponentAt(tabPaneCount-1);
				final SimpleAttributeSet attrset = new SimpleAttributeSet();
				StyleConstants.setForeground(attrset, Color.GREEN);
				StyleConstants.setFontSize(attrset, 13);
				StyleConstants.setUnderline(attrset, false);
				StyleConstants.setItalic(attrset, false);
				try {
					((JTextPane)(jScrollPane.getViewport().getView())).getDocument().insertString(0,"hello",attrset);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(jScrollPane.getViewport().getView());
				//tabbedPane.remove(tabPaneCount-1);
				tabPaneCount--;
				
			}
		});
		btnNewButton.setVerticalAlignment(SwingConstants.TOP);
		getContentPane().add(btnNewButton, BorderLayout.NORTH);
		
		JButton btnNewButton_1 = new JButton("\u52A0textpane");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				
			}
		});
		getContentPane().add(btnNewButton_1, BorderLayout.WEST);
		

	}
	
	//生成加密所用的盐
	public static byte[] createSalt()
	{
		byte[] salt = new byte[16];
		try {
			SecureRandom random = SecureRandom.getInstance("SHA-1");
			random.nextBytes(salt);
			return salt;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	//生成加盐加密的方法
	public static byte[] Pwddigest(String password, byte[] salt) {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
			if (salt != null && salt.length > 0) {
				mDigest.update(salt);
			}
			byte[] digest = mDigest.digest();
			return digest;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
//		EventQueue.invokeLater(()-> {
//			try {
//				test frame = new test();
//				frame.setVisible(true);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		});
//		Object[] options = {"接收","拒绝"};
//		EventQueue.invokeLater(() ->{
//			JOptionPane.showMessageDialog(null, "?????");
//		});
//		new Thread(new Runnable() {
//			public void run() {
//				FileTranfer fileTranfer = new FileTranfer();
//				fileTranfer.setVisible(true);
//				for(int i=0;i<99;i++)
//				{
////					fileTranfer.changeValue(i);
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					fileTranfer.valueplus();
//				}
//			}
//		}).start();
		
		
		
	}
}
