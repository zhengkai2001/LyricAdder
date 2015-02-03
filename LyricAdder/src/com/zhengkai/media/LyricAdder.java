package com.zhengkai.media;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 歌词添加器类
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class LyricAdder extends Thread {
	private String musicDirectory;
	private String lyricDirectory;

	private HashMap<String, ArrayList<MusicObject>> arrayListMap = new HashMap<String, ArrayList<MusicObject>>();
	private ArrayList<MusicObject> songs;
	private ArrayList<MusicObject> lyrics;

	private HashMap<String, String[]> extensionMap = new HashMap<String, String[]>();
	private String[] musicFileExtensions = new String[] { ".mp3", ".m4a" };
	private String[] lyricFileExtensions = new String[] { ".lrc", ".txt" };

	@SuppressWarnings("rawtypes")
	private HashMap<String, Class> classMap = new HashMap<String, Class>();

	private boolean usingLocalLyric;

	private LyricHelper lyricHelper;
	private LyricSystem lyricSystem;
	private boolean debugMode;

	/**
	 * 添加歌词
	 */
	public void addLyrics() {
		System.out.print("正在遍历音乐目录，请稍候……");
		travel(musicDirectory, "music");
		System.out.print("完成！\n\n");

		if (usingLocalLyric) {
			travel(lyricDirectory, "lyric");
			addLyricsLocal();
		} else {
			addLyricsFromInternet();
			// removeTag();
		}
		System.out.print("本次添加已完成。");
	}

	private void removeTag() {
		for (int i = 0; i != songs.size(); i++) {
			Song song = (Song) songs.get(i);
			System.out.println("第" + (i + 1) + "首：[" + song.title + "]["
					+ song.artist + "]\n" + song.filePath);
			song.removeTag();
		}
	}

	/**
	 * 从Internet获取歌词
	 */
	private void addLyricsFromInternet() {
		for (int i = 0; i != songs.size(); i++) {
			Song song = (Song) songs.get(i);
			System.out.println("第" + (i + 1) + "首：[" + song.title + "]["
					+ song.artist + "]\n" + song.filePath);

			// song.renameFileUsingTitleInTag();
			// song.outputTag();

			ArrayList<String> lyricLines = lyricHelper
					.getLyricFromInternet(song);
			if (lyricLines != null) {
				Lyric lyric = new Lyric(lyricLines);
				lyric.save(lyricDirectory);
				song.setLyric(lyric);
			}

			System.out.println();
			System.out.flush();
		}
	}

	/**
	 * 在指定目录下，遍历指定类型的文件
	 * 
	 * @param mol
	 *            用于存放遍历结果的容器
	 * @param path
	 *            指定的目录
	 * @param type
	 *            指定的文件类型
	 */
	private void travel(String path, String type) {
		ArrayList<MusicObject> mol = arrayListMap.get(type);
		String[] extensions = extensionMap.get(type);
		travel(mol, path, type, extensions);
	}

	/**
	 * 在指定目录下，遍历指定类型的文件
	 * 
	 * @param mol
	 *            用于存放遍历结果的容器
	 * @param path
	 *            指定的目录
	 * @param type
	 *            指定的文件类型
	 * @param extensions
	 *            指定的文件扩展名
	 */
	private void travel(ArrayList<MusicObject> mol, String path, String type,
			String[] extensions) {
		File dir = new File(path);
		File[] files = dir.listFiles();

		if (!dir.exists() || files == null || files.length == 0) {
			return;
		}

		for (File file : files) {
			String absolutePath = file.getAbsolutePath();

			if (file.isDirectory()) {
				travel(mol, absolutePath, type, extensions);
			} else {
				for (String extension : extensions) {
					if (absolutePath.endsWith(extension)) {

						try {
							@SuppressWarnings({ "rawtypes", "unchecked" })
							Constructor constructor = classMap.get(type)
									.getConstructor(String.class);
							mol.add((MusicObject) constructor
									.newInstance(absolutePath));
						} catch (NoSuchMethodException | SecurityException
								| InstantiationException
								| IllegalAccessException
								| IllegalArgumentException
								| InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * 在本地搜索歌词文件，并添加到歌曲中
	 */
	private void addLyricsLocal() {
		for (int i = 0; i != songs.size(); i++) {
			Song song = (Song) songs.get(i);
			System.out.println("第" + (i + 1) + "首：" + song.title + " "
					+ song.filePath);
			// song.renameFileUsingTitleInTag();
			// song.outputTag();

			for (int j = 0; j != lyrics.size(); j++) {
				// System.out.println("j = " + j);
				Lyric lyric = (Lyric) lyrics.get(j);

				if (matched(song, lyric)) {
					System.out.println("local: found!");
					song.setLyric(lyric);
					break;
				}
			}
		}
	}

	/**
	 * 判定歌曲与歌词是否匹配
	 * 
	 * @param song
	 *            歌曲
	 * @param lyric
	 *            歌词
	 * @return 是否匹配
	 */
	protected boolean matched(Song song, Lyric lyric) {
		return ((song.title.contains(lyric.title) && song.artist
				.contains(lyric.artist))
				|| (song.title.contains(lyric.title) && lyric.artist
						.contains(song.artist))
				|| (lyric.title.contains(song.title) && lyric.artist
						.contains(song.artist)) || (lyric.title
				.contains(song.title) && song.artist.contains(lyric.artist)));
	}

	public LyricAdder() {
		lyricHelper = LyricHelper.getInstance();

		extensionMap.put("music", musicFileExtensions);
		extensionMap.put("lyric", lyricFileExtensions);

		classMap.put("music", Song.class);
		classMap.put("lyric", Lyric.class);

		lyrics = new ArrayList<MusicObject>();
		songs = new ArrayList<MusicObject>();

		arrayListMap.put("lyric", lyrics);
		arrayListMap.put("music", songs);
	}

	@Override
	public void run() {
		addLyrics();

		// 添加完毕之后，将GUI重设为默认
		lyricSystem.setDefault();
	}

	public void setLyricSites(boolean baidu, boolean gecimi, boolean lyricwiki) {
		this.lyricHelper.setLyricSites(baidu, gecimi, lyricwiki);
	}

	public void setMusicDirectory(String musicDirectory) {
		this.musicDirectory = musicDirectory;
	}

	public void setLyricDirectory(String lyricDirectory) {
		this.lyricDirectory = lyricDirectory;
	}

	public void setUsingLocalLyric(boolean usingLocalLyric) {
		this.usingLocalLyric = usingLocalLyric;
	}

	public void setLyricSystem(LyricSystem lyricSystem) {
		this.lyricSystem = lyricSystem;
	}
}