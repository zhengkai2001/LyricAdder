package com.zhengkai.media;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;

import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;

public class LyricSystem {
	private final static String windowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	private final static Font yahei = new Font("微软雅黑", Font.PLAIN, 12);
	private final static String version = "0.1";

	private static String musicDirectory = "D:\\Music1";
	// private static String lyricDir = "C:\\Lyrics\\";

	private JFrame frame;
	private JTextField labelPath;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JCheckBox checkBox_log, checkBox_baidu, checkBox_gecimi, chckbx_lyricwiki;
	private JButton button_pause;

	private boolean pause;

	LyricAdder lyricAdder;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LyricSystem window = new LyricSystem();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LyricSystem() {
		initialize();
		setLookAndFeel();

		pause = false;
	}

	private void setLookAndFeel() {
		// 设置默认字体，必须在设置观感之前，否则无效
		// UIManager.put("Button.font", yahei);
		// UIManager.put("Label.font", yahei);
		// UIManager.put("Menu.font", yahei);
		// UIManager.put("TextField.font", yahei);

		// 将所有UI组件的字体都设置为雅黑
		FontUIResource yaheiFontUIResource = new FontUIResource(yahei);
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, yaheiFontUIResource);
			}
		}

		// LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		// for(LookAndFeelInfo info : infos) {
		// System.out.println(info.getName());
		// System.out.println(info.getClassName());
		// }

		// 设置观感
		try {
			UIManager.setLookAndFeel(windowsLookAndFeel);
			SwingUtilities.updateComponentTreeUI(frame);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
				LyricSystem.class.getResource("/com/zhengkai/media/LyricAdder.png")));
		frame.setBounds(100, 100, 500, 400);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu menu_1 = new JMenu("帮助");
		menuBar.add(menu_1);

		JMenuItem menuItem = new JMenuItem("关于");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				castAbout();
			}
		});
		menu_1.add(menuItem);
		frame.getContentPane().setLayout(null);

		checkBox_baidu = new JCheckBox("百度音乐");
		checkBox_baidu.setSelected(true);
		checkBox_baidu.setBounds(10, 87, 80, 23);
		frame.getContentPane().add(checkBox_baidu);

		checkBox_gecimi = new JCheckBox("歌词迷");
		checkBox_gecimi.setBounds(92, 87, 80, 23);
		frame.getContentPane().add(checkBox_gecimi);

		chckbx_lyricwiki = new JCheckBox("LyricWiki");
		chckbx_lyricwiki.setBounds(10, 112, 103, 23);
		frame.getContentPane().add(chckbx_lyricwiki);

		JLabel label_1 = new JLabel("要使用的歌词站点：");
		label_1.setBounds(10, 66, 120, 15);
		frame.getContentPane().add(label_1);

		JLabel label = new JLabel("设置歌曲目录：");
		label.setBounds(10, 10, 100, 15);
		frame.getContentPane().add(label);

		labelPath = new JTextField();
		labelPath.setText(musicDirectory);
		labelPath.setBounds(10, 35, 389, 21);
		frame.getContentPane().add(labelPath);
		labelPath.setColumns(10);
		labelPath.setPreferredSize(new Dimension(100, 20));

		JButton button = new JButton("浏览...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setCurrentDirectory(new File(musicDirectory));
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					if (selectedFile.exists()) {
						musicDirectory = selectedFile.getAbsolutePath();
						labelPath.setText(musicDirectory);
					} else {
					}
				}
			}
		});
		button.setBounds(409, 34, 75, 23);
		frame.getContentPane().add(button);

		checkBox_log = new JCheckBox("保存log");
		checkBox_log.setBounds(213, 76, 80, 23);
		checkBox_log.setSelected(false);
		frame.getContentPane().add(checkBox_log);

		JButton button_start = new JButton("开始添加歌词！");
		button_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setOutput();

				musicDirectory = labelPath.getText();
				lyricAdder = new LyricAdder(musicDirectory);
				lyricAdder.setLyricSites(checkBox_baidu.isSelected(), checkBox_gecimi.isSelected(),
						chckbx_lyricwiki.isSelected());
				lyricAdder.start();
			}
		});
		button_start.setBounds(299, 66, 185, 36);
		frame.getContentPane().add(button_start);

		button_pause = new JButton("暂停");
		button_pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (lyricAdder != null) {
					System.out.println("ddddddddddddddddd");
					pauseOrResume();
				}
			}
		});
		button_pause.setBounds(299, 112, 75, 23);
		frame.getContentPane().add(button_pause);

		JButton button_stop = new JButton("停止");
		button_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JScrollBar jsb = scrollPane.getVerticalScrollBar();
				System.out.println(jsb.getMaximum());
				Document document = textPane.getDocument();
				try {
					document.insertString(document.getLength(), "hehe.\n", null);
					jsb.setValue(jsb.getMaximum());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		});
		button_stop.setBounds(409, 112, 75, 23);
		frame.getContentPane().add(button_stop);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 141, 474, 200);
		frame.getContentPane().add(scrollPane);

		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		// textPane.setText("123\n\n\n\n\n\n\n\n\n\n\nfdsg\n\n\n\n\nfg\n\n\n\n\n");
		textPane.setEditable(false);
		textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}

	@SuppressWarnings("deprecation")
	protected void pauseOrResume() {
		if (pause) {
			button_pause.setText("暂停");
			lyricAdder.resume();
		} else {
			button_pause.setText("恢复");
			lyricAdder.suspend();
		}
		pause = !pause;
	}

	private void castAbout() {
		JOptionPane.showMessageDialog(frame, "Author: zk\nVersion: " + version, "About",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void setOutput() {
		System.setOut(new GUIPrintStream(System.out, textPane));

		try {
			if (checkBox_log.isSelected()) {
				PrintStream out = new PrintStream("C:\\LyricAdderLog.txt");
				System.setOut(out);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
