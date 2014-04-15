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

	private boolean useLocalLyric;

	LyricHelper lyricHelper;

	/**
	 * 添加歌词
	 */
	public void addLyrics() {
		travel(musicDirectory, "music");

		if (useLocalLyric) {
			travel(lyricDirectory, "lyric");
			addLyricsLocal();
		} else {
			addLyricsFromInternet();
		}
	}

	/**
	 * 从Internet获取歌词
	 */
	private void addLyricsFromInternet() {
		for (int i = 0; i != songs.size(); i++) {
			Song song = (Song) songs.get(i);
			System.out.println("i = " + i + "  " + song.title + "  " + song.artist + "  "
					+ song.filePath);
			// song.renameFileUsingTitleInTag();
			// song.outputTag();

			ArrayList<String> lyricLines = getLyricFromInternet(song);
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
	 * 为某一首歌曲从网上获取歌词
	 * 
	 * @param song
	 *        指定的歌曲
	 * @return 歌词
	 */
	private ArrayList<String> getLyricFromInternet(Song song) {
		return lyricHelper.getLyricFromInternet(song);
	}

	/**
	 * 在指定目录下，遍历指定类型的文件
	 * 
	 * @param mol
	 *        用于存放遍历结果的容器
	 * @param path
	 *        指定的目录
	 * @param type
	 *        指定的文件类型
	 */
	private void travel(String path, String type) {
		System.out.println(type);
		ArrayList<MusicObject> mol = arrayListMap.get(type);
		System.out.println(mol);
		String[] extensions = extensionMap.get(type);
		travel(mol, path, type, extensions);
	}

	/**
	 * 在指定目录下，遍历指定类型的文件
	 * 
	 * @param mol
	 *        用于存放遍历结果的容器
	 * @param path
	 *        指定的目录
	 * @param type
	 *        指定的文件类型
	 * @param extensions
	 *        指定的文件扩展名
	 */
	private void travel(ArrayList<MusicObject> mol, String path, String type, String[] extensions) {
		System.out.println(path);
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
							Constructor constructor = classMap.get(type).getConstructor(String.class);
							mol.add((MusicObject) constructor.newInstance(absolutePath));
						} catch (NoSuchMethodException | SecurityException | InstantiationException
								| IllegalAccessException | IllegalArgumentException
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
			System.out.println("i = " + i + " " + song.title + " " + song.filePath);
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
	 *        歌曲
	 * @param lyric
	 *        歌词
	 * @return 是否匹配
	 */
	protected boolean matched(Song song, Lyric lyric) {
		return (song.title.contains(lyric.title) && song.artist.contains(lyric.artist)
				|| song.title.contains(lyric.title) && lyric.artist.contains(song.artist)
				|| lyric.title.contains(song.title) && lyric.artist.contains(song.artist) || lyric.title
				.contains(song.title) && song.artist.contains(lyric.artist));
	}

	/**
	 * 构造函数
	 * 
	 * @param musicdirectory
	 *        指定的音乐文件目录
	 * @param lyricdirectory
	 *        指定的歌词文件目录
	 */
	public LyricAdder(String musicdirectory, String lyricdirectory) {
		this(musicdirectory);
		this.lyricDirectory = lyricdirectory;
		lyrics = new ArrayList<MusicObject>();
		arrayListMap.put("lyric", lyrics);
		useLocalLyric = true;
	}

	public LyricAdder(String musicdirectory) {
		this();
		this.musicDirectory = musicdirectory;
		songs = new ArrayList<MusicObject>();
		arrayListMap.put("music", songs);
		useLocalLyric = false;
	}

	public LyricAdder() {
		lyricHelper = LyricHelper.getInstance();

		extensionMap.put("music", musicFileExtensions);
		extensionMap.put("lyric", lyricFileExtensions);

		classMap.put("music", Song.class);
		classMap.put("lyric", Lyric.class);
	}

	@Override
	public void run() {
		addLyrics();
	}

	public void setLyricSites(boolean baidu, boolean gecimi, boolean lyricwiki) {
		lyricHelper.setLyricSites(baidu, gecimi, lyricwiki);
	}
}