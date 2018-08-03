package com.cauc.chat;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class tabNameToCountumber {
	public int tabCountnumber;
	public String tabName;
	public JScrollPane jScrollPaneForWhisper;
	public JTextPane jTextPaneForWhisper;
	public tabNameToCountumber(int tabCountnumber, String tabName, JScrollPane jScrollPaneForWhisper,
			JTextPane jTextPaneForWhisper) {
		this.tabCountnumber = tabCountnumber;
		this.tabName = tabName;
		this.jScrollPaneForWhisper = jScrollPaneForWhisper;
		
		this.jTextPaneForWhisper = jTextPaneForWhisper;
	}
}
