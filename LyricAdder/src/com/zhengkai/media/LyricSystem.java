package com.zhengkai.media;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class LyricSystem {
	private final static String md = "D:\\Music1\\";
	private final static String ld = "D:\\Lyrics\\";

	private JFrame mainFrame;
	private JCheckBox box;
	private JButton button;
	private JLabel statusLabel;

	private boolean beginAdding;

	public LyricSystem() {
		beginAdding = false;
	}

	public static void main(String[] args) {
		LyricSystem ls = new LyricSystem();

		ls.initComponents();
		ls.initGUI();
	}

	private void initComponents() {
		mainFrame = new JFrame();

		box = new JCheckBox("是否保存歌词到本地");
		mainFrame.add(box, BorderLayout.NORTH);

		statusLabel = new JLabel("哈哈");
		mainFrame.add(statusLabel, BorderLayout.SOUTH);

		button = new JButton("开始添加歌词！");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				button.setText("添加中，请稍等……");
				// beginAdding = true;
				addLyrics();
			}
		});

		mainFrame.add(button, BorderLayout.CENTER);
	}

	private void initGUI() {
		mainFrame.setTitle("歌词添加器");
		mainFrame.setIconImage(mainFrame.getToolkit().getImage("LyricAdder.png"));
		
		mainFrame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null);
		
		mainFrame.setVisible(true);

		// 关闭窗口时，结束程序（否则会一直仍在运行）
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	private void addLyrics() {
		Editor editor = new Editor(md, ld);
		editor.addLyrics();
	}
	
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 300;
}
