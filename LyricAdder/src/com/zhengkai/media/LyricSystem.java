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
import java.util.ArrayList;
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

	private final static String[] helpStrings = {
			"使用说明：", "本软件会将指定目录下的所有音乐文件自动添加歌词，添加后的歌词可以在 iOS 的自带音乐 app 中显示。",
			" - 目前仅支持 .mp3 和 .m4a 文件，请确保歌曲都包含正确的标签（歌名、艺术家）",
			" - 歌词来源于各个网站，本软件无法保证为所有歌曲都添加上正确的歌词", " - 歌词站点推荐仅使用百度音乐，歌词迷的下载服务经常不可用",
			" - 由于歌词站点的网页随时可能发生变化，因此本软件"};

	private static String musicDirectory = "D:\\Music1";
	// private static String lyricDir = "C:\\Lyrics\\";

	private JFrame frame;
	private JTextField textField_musicPath;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private GUIPrintStream guiPrintStream;
	private JCheckBox checkBox_log, checkBox_baidu, checkBox_gecimi, checkBox_lyricwiki;
	private JButton button_browse, button_start, button_pause, button_stop;

	private boolean started;
	private boolean paused;

	private LyricAdder lyricAdder;

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
		setComponentsDefault();
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

		textField_musicPath = new JTextField();
		textField_musicPath.setText(musicDirectory);
		textField_musicPath.setBounds(10, 35, 389, 21);
		frame.getContentPane().add(textField_musicPath);
		textField_musicPath.setColumns(10);
		textField_musicPath.setPreferredSize(new Dimension(100, 20));

		button_browse = new JButton("浏览...");
		button_browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setMusicPath();
			}
		});
		button_browse.setBounds(409, 34, 75, 23);
		frame.getContentPane().add(button_browse);

		checkBox_log = new JCheckBox("保存log");
		checkBox_log.setBounds(213, 76, 80, 23);
		checkBox_log.setSelected(false);
		frame.getContentPane().add(checkBox_log);

		button_start = new JButton("开始添加歌词！");
		button_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				start();
			}
		});
		button_start.setBounds(299, 66, 185, 35);
		frame.getContentPane().add(button_start);

		button_pause = new JButton("暂停");
		button_pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pauseOrResume();
			}
		});
		button_pause.setBounds(299, 112, 75, 23);
		frame.getContentPane().add(button_pause);

		button_stop = new JButton("停止");
		button_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
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

		guiPrintStream = new GUIPrintStream(System.out, textPane);
		System.setOut(guiPrintStream);
	}

	private void setComponentsDefault() {
		textField_musicPath.setEnabled(true);
		button_browse.setEnabled(true);
		button_start.setEnabled(true);
		button_start.setText("开始");
		button_pause.setText("暂停");
		button_pause.setEnabled(false);
		button_stop.setEnabled(false);
	}

	private void setComponentsRunning() {
		textField_musicPath.setEnabled(false);
		button_browse.setEnabled(false);
		button_start.setEnabled(false);
		button_start.setText("添加中……");
		button_pause.setEnabled(true);
		button_stop.setEnabled(true);
		guiPrintStream.clear();
	}

	private void setMusicPath() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(musicDirectory));
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile.exists()) {
				musicDirectory = selectedFile.getAbsolutePath();
				textField_musicPath.setText(musicDirectory);
			}
		}
	}

	private void start() {
		if (!started) {
			started = true;
			paused = false;
			setComponentsRunning();

			startAdding();
		}
	}

	private void startAdding() {
		setLogOutput();

		lyricAdder = new LyricAdder();

		musicDirectory = textField_musicPath.getText();
		lyricAdder.setMusicDirectory(musicDirectory);
		lyricAdder.setLyricSites(checkBox_baidu.isSelected(), checkBox_gecimi.isSelected(),
				checkBox_lyricwiki.isSelected());
		lyricAdder.setUsingLocalLyric(false);
		lyricAdder.start();
	}

	@SuppressWarnings("deprecation")
	private void stop() {
		if (started) {
			if (lyricAdder != null && started) {
				lyricAdder.stop();
			}

			started = false;
			paused = false;
			setComponentsDefault();
		}
	}

	@SuppressWarnings("deprecation")
	private void pauseOrResume() {
		if (lyricAdder != null && started) {
			if (paused) {
				button_pause.setText("暂停");
				lyricAdder.resume();
			} else {
				button_pause.setText("恢复");
				lyricAdder.suspend();
			}
			paused = !paused;
		}
	}

	/**
	 * 显示使用帮助
	 */
	private void castHelp() {
		ArrayList<JLabel> labelList = new ArrayList<JLabel>();

		JLabel helpLabel = new JLabel(generateLabString(helpStrings));
		labelList.add(helpLabel);

		// JLabel attentionLabel = new JLabel(generateLabString(attentionStrings));
		// labelList.add(attentionLabel);

		JPanel helpPanel = new JPanel();
		helpPanel.setLayout(new GridLayout(labelList.size(), 1));
		for (JLabel label : labelList) {
			helpPanel.add(label);
		}

		JOptionPane.showMessageDialog(frame, helpPanel, "使用帮助", JOptionPane.QUESTION_MESSAGE);
	}

	private String generateLabString(String[] strings) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");

		for (String string : strings) {
			sb.append(string);
			sb.append("<br>");
		}

		sb.append("</html>");
		return sb.toString();
	}

	/**
	 * 显示关于信息
	 */
	private void castAbout() {
		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new GridLayout(3, 1));

		URLLabel authorLabel = new URLLabel("作者: zk", "weibo.com/1267591671");
		JLabel versionLabel = new JLabel("version: " + version);
		URLLabel githubLabel = new URLLabel(
				"github: https://github.com/zhengkai2001/LyricAdder",
				"https://github.com/zhengkai2001/LyricAdder");

		aboutPanel.add(authorLabel);
		aboutPanel.add(versionLabel);
		aboutPanel.add(githubLabel);

		JOptionPane
				.showMessageDialog(frame, aboutPanel, "关于", JOptionPane.INFORMATION_MESSAGE);
	}

	private void setLogOutput() {
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
