package com.zhengkai.media;

import java.awt.EventQueue;

import javax.swing.JCheckBoxMenuItem;
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
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

	// 指定是否要将system.out重定向到GUI面板上去
	private final boolean output2GUI = true;

	private static String musicDirectory = "D:\\Music\\";
	// private static String lyricDir = "C:\\Lyrics\\";

	private JFrame frame;
	private JTextField textField_musicPath;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private GUIPrintStream guiPrintStream;
	private JCheckBox checkBox_baidu, checkBox_gecimi, checkBox_lyricwiki;
	private JButton button_browse, button_start, button_pause, button_stop;
	private JLabel label_setLyricSiteHelp;

	private boolean started;
	private boolean paused;
	private boolean debugMode;

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

	/**
	 * 设置外观
	 */
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
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
				LyricSystem.class
						.getResource("/com/zhengkai/media/LyricAdder.png")));
		frame.setTitle("歌词添加器");
		frame.setBounds(100, 100, 500, 400);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		JMenu menu_setting = new JMenu("设置");
		menuBar.add(menu_setting);

		JCheckBoxMenuItem menuItem_debugMode = new JCheckBoxMenuItem("开启调试模式");
		menuItem_debugMode.setSelected(false);
		debugMode = false;
		menuItem_debugMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				debugMode = !debugMode;
			}
		});
		menu_setting.add(menuItem_debugMode);

		JMenu menu_help = new JMenu("帮助");
		menuBar.add(menu_help);

		JMenuItem menuItem_help = new JMenuItem("使用说明");
		menuItem_help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				castHelp();
			}
		});
		menu_help.add(menuItem_help);

		JMenuItem menuItem_lyricSiteHelp = new JMenuItem("该选择哪个歌词站点？");
		menuItem_lyricSiteHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				castLyricSiteHelp();
			}
		});
		menu_help.add(menuItem_lyricSiteHelp);

		JMenuItem menuItem_about = new JMenuItem("关于...");
		menuItem_about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				castAbout();
			}
		});
		menu_help.add(menuItem_about);

		frame.setJMenuBar(menuBar);

		checkBox_baidu = new JCheckBox("百度音乐");
		checkBox_baidu.setSelected(true);
		checkBox_baidu.setBounds(10, 87, 80, 23);
		frame.getContentPane().add(checkBox_baidu);

		checkBox_gecimi = new JCheckBox("歌词迷");
		checkBox_gecimi.setSelected(true);
		checkBox_gecimi.setBounds(92, 87, 80, 23);
		frame.getContentPane().add(checkBox_gecimi);

		checkBox_lyricwiki = new JCheckBox("LyricWiki");
		checkBox_lyricwiki.setSelected(true);
		checkBox_lyricwiki.setBounds(10, 112, 103, 23);
		frame.getContentPane().add(checkBox_lyricwiki);

		label_setLyricSiteHelp = new JLabel("要使用的歌词站点：");
		label_setLyricSiteHelp.setBounds(10, 66, 120, 15);
		frame.getContentPane().add(label_setLyricSiteHelp);

		label_setLyricSiteHelp = new JLabel();
		label_setLyricSiteHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				castLyricSiteHelp();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				label_setLyricSiteHelp.setCursor(Cursor
						.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				label_setLyricSiteHelp.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		label_setLyricSiteHelp.setText("<html><b>（我该选择哪个？）</b></html>");
		label_setLyricSiteHelp.setBounds(118, 66, 120, 15);
		frame.getContentPane().add(label_setLyricSiteHelp);

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

		button_start = new JButton("开始添加歌词！");
		button_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				start();
			}
		});
		button_start.setBounds(334, 66, 150, 35);
		frame.getContentPane().add(button_start);

		button_pause = new JButton("暂停");
		button_pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pauseOrResume();
			}
		});
		button_pause.setBounds(334, 112, 68, 23);
		frame.getContentPane().add(button_pause);

		button_stop = new JButton("停止");
		button_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String confirmInfo = "正在添加歌词。真的要停止吗？";
				int confirm = JOptionPane.showConfirmDialog(frame, confirmInfo,
						"确认", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					stop();
				}
			}
		});
		button_stop.setBounds(416, 112, 68, 23);
		frame.getContentPane().add(button_stop);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 141, 474, 200);
		frame.getContentPane().add(scrollPane);

		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		textPane.setEditable(false);
		textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

		guiPrintStream = new GUIPrintStream(System.out, textPane);
		if (output2GUI) {
			System.setOut(guiPrintStream);
		}
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
			setLogOutput();
			setComponentsRunning();

			startAdding();
		}
	}

	private void startAdding() {
		lyricAdder = new LyricAdder();

		musicDirectory = textField_musicPath.getText();
		lyricAdder.setMusicDirectory(musicDirectory);
		lyricAdder.setLyricSites(checkBox_baidu.isSelected(),
				checkBox_gecimi.isSelected(), checkBox_lyricwiki.isSelected());
		lyricAdder.setUsingLocalLyric(false);
		lyricAdder.setLyricSystem(this);
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
				button_pause.setText("继续");
				lyricAdder.suspend();
			}
			paused = !paused;
		}
	}

	/**
	 * 从给定的JLabel列表生成JPanel
	 * 
	 * @param labelList
	 *            指定的标签列表
	 * @return JPanel
	 */
	private JPanel createPanel(ArrayList<JLabel> labelList) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(labelList.size(), 1));
		for (JLabel label : labelList) {
			panel.add(label);
		}
		return panel;
	}

	/**
	 * 根据字符串数组生成标签
	 * 
	 * @param strings
	 *            要生成标签的字符串数组
	 * @return 标签容器
	 */
	private ArrayList<JLabel> createLabels(String[] strings) {
		ArrayList<JLabel> labelList = new ArrayList<JLabel>();

		for (String string : strings) {
			labelList.add(new JLabel("<html>" + string + "</html>"));
		}

		labelList.add(new JLabel());

		return labelList;
	}

	/**
	 * 显示使用帮助
	 */
	private void castHelp() {
		ArrayList<JLabel> labelList = createLabels(helpStrings);
		JPanel helpPanel = createPanel(labelList);

		JOptionPane.showMessageDialog(frame, helpPanel, "帮助",
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * 显示选择歌词站点的帮助
	 */
	private void castLyricSiteHelp() {
		ArrayList<JLabel> labelList = createLabels(lyricSiteHelpStrings);
		JPanel helpPanel = createPanel(labelList);

		JOptionPane.showMessageDialog(frame, helpPanel, "帮助",
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * 显示关于信息
	 */
	private void castAbout() {
		ArrayList<JLabel> labelList = new ArrayList<JLabel>();

		JLabel versionLabel = new JLabel("版本：" + version);
		URLLabel authorLabel = new URLLabel("作者：zk", "weibo.com/1267591671");
		URLLabel githubLabel = new URLLabel(
				"https://github.com/zhengkai2001/LyricAdder",
				"https://github.com/zhengkai2001/LyricAdder");

		labelList.add(versionLabel);
		labelList.add(authorLabel);
		labelList.add(githubLabel);

		JPanel aboutPanel = createPanel(labelList);

		JOptionPane.showMessageDialog(frame, aboutPanel, "关于",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void setLogOutput() {
		// PrintStream out = new PrintStream("C:\\LyricAdderLog.txt");
		// System.setOut(out);
	}

	private final static String[] lyricSiteHelpStrings = { "关于歌词站点的选择：",
			"三个歌词网站各自有优缺点，请根据你的歌曲库的实际情况进行选择。",
			" - lyricwiki：国外网站，根据歌名和歌手名的精确查找，英文歌非常全。缺点是没有中文歌，连接速度较慢。",
			" - 歌词迷：国内网站，根据歌名和歌手名的精确查找，中英文歌都有一些。缺点是提供歌词下载的服务器经常出错。",
			" - 百度音乐：众所周知的网站，访问速度超快。缺点是只有模糊查找，很容易找到错误的歌词。", "",
			" - 如果有多个网站被勾选，优先级是 lyricwiki > 歌词迷 > 百度音乐。",
			" - 如果你发现某个网站无法连接或者速度很慢，请停止并取消对其的勾选，然后再开始。" };

	private final static String[] helpStrings = { "使用说明：",
			"本软件会将指定目录下的所有音乐文件自动添加歌词，添加后的歌词可以在 iOS 的自带音乐 app 中显示。",
			" - 搜索歌词时使用的是歌曲标签中的信息，请确保歌曲标签包含正确的歌名、艺术家名信息。",
			" - 受本人技术水平限制，目前仅支持 .mp3 和 .m4a 文件，并且仅支持 UTF-8 编码的标签。", "",
			" - 歌词来源于各个网站，<b>本软件无法保证为所有歌曲都添加上正确的歌词！</b>",
			" - 由于歌词站点的网页随时可能发生变化，<b>因此本软件随时可能失效。</b>若失效，请尝试获取最新版本。", };

	public void setDefault() {
		started = false;
		paused = false;
		setComponentsDefault();
	}
}
