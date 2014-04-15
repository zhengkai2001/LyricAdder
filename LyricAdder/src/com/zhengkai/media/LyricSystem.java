package com.zhengkai.media;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.plaf.FontUIResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;

import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;

/**
 * 歌词添加器的GUI界面
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class LyricSystem {
	private final static String windowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	private final static Font yahei = new Font("微软雅黑", Font.PLAIN, 12);
	private final static String version = "0.1";

	private static String musicDirectory = "D:\\Music1";
	// private static String lyricDir = "C:\\Lyrics\\";

	private JFrame frame;
	private JTextField label_musicPath;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JCheckBox checkBox_log, checkBox_baidu, checkBox_gecimi, checkBox_lyricwiki;
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

		JMenu menu_help = new JMenu("帮助");
		menuBar.add(menu_help);

		JMenuItem menuItem_about = new JMenuItem("关于...");
		menuItem_about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				castAbout();
			}
		});

		JMenuItem menuItem_help = new JMenuItem("使用帮助");
		menuItem_help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				castHelp();
			}
		});
		menu_help.add(menuItem_help);
		menu_help.add(menuItem_about);
		frame.getContentPane().setLayout(null);

		checkBox_baidu = new JCheckBox("百度音乐");
		checkBox_baidu.setSelected(true);
		checkBox_baidu.setBounds(10, 87, 80, 23);
		frame.getContentPane().add(checkBox_baidu);

		checkBox_gecimi = new JCheckBox("歌词迷");
		checkBox_gecimi.setBounds(92, 87, 80, 23);
		frame.getContentPane().add(checkBox_gecimi);

		checkBox_lyricwiki = new JCheckBox("LyricWiki");
		checkBox_lyricwiki.setBounds(10, 112, 103, 23);
		frame.getContentPane().add(checkBox_lyricwiki);

		JLabel label_setLyricSite = new JLabel("要使用的歌词站点：");
		label_setLyricSite.setBounds(10, 66, 120, 15);
		frame.getContentPane().add(label_setLyricSite);

		JLabel label_setMusicPath = new JLabel("设置歌曲目录：");
		label_setMusicPath.setBounds(10, 10, 100, 15);
		frame.getContentPane().add(label_setMusicPath);

		label_musicPath = new JTextField();
		label_musicPath.setText(musicDirectory);
		label_musicPath.setBounds(10, 35, 389, 21);
		frame.getContentPane().add(label_musicPath);
		label_musicPath.setColumns(10);
		label_musicPath.setPreferredSize(new Dimension(100, 20));

		JButton button = new JButton("浏览...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setMusicPath();
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
				startAdding();
			}
		});
		button_start.setBounds(299, 66, 185, 36);
		frame.getContentPane().add(button_start);

		button_pause = new JButton("暂停");
		button_pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (lyricAdder != null) {
					pauseOrResume();
				}
			}
		});
		button_pause.setBounds(299, 112, 75, 23);
		frame.getContentPane().add(button_pause);

		JButton button_stop = new JButton("停止");
		button_stop.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				if (lyricAdder != null) {
					lyricAdder.stop();
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
		textPane.setEditable(false);
		textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}

	private void setMusicPath() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(musicDirectory));
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile.exists()) {
				musicDirectory = selectedFile.getAbsolutePath();
				label_musicPath.setText(musicDirectory);
			} else {
			}
		}
	}

	private void startAdding() {
		setOutput();

		musicDirectory = label_musicPath.getText();
		lyricAdder = new LyricAdder(musicDirectory);
		lyricAdder.setLyricSites(checkBox_baidu.isSelected(), checkBox_gecimi.isSelected(),
				checkBox_lyricwiki.isSelected());
		lyricAdder.start();
	}

	@SuppressWarnings("deprecation")
	private void pauseOrResume() {
		if (pause) {
			button_pause.setText("暂停");
			lyricAdder.resume();
		} else {
			button_pause.setText("恢复");
			lyricAdder.suspend();
		}
		pause = !pause;
	}

	/**
	 * 显示使用帮助
	 */
	private void castHelp() {
		// TODO 自动生成的方法存根

	}

	/**
	 * 显示关于信息
	 */
	private void castAbout() {
		URLLabel authorLabel = new URLLabel("作者: zk", "weibo.com/1267591671");
		JLabel versionLabel = new JLabel("version: " + version);
		URLLabel githubLabel = new URLLabel("github: https://github.com/zhengkai2001/LyricAdder",
				"https://github.com/zhengkai2001/LyricAdder");

		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new GridLayout(3, 1));
		aboutPanel.add(authorLabel);
		aboutPanel.add(versionLabel);
		aboutPanel.add(githubLabel);

		JOptionPane.showMessageDialog(frame, aboutPanel, "关于", JOptionPane.INFORMATION_MESSAGE);
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
