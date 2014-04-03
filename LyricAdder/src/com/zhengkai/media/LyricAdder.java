package com.zhengkai.media;

import java.io.File;
import java.util.ArrayList;

public class LyricAdder {
	private String musicDirectory;
	private String lyricDirectory;

	private ArrayList<MusicObject> songs;
	private ArrayList<MusicObject> lyrics;

	LyricHelper lyricHelper;

	/**
	 * 添加歌词
	 */
	public void addLyrics() {
		travel(songs, musicDirectory, "mp3");
		travel(lyrics, lyricDirectory, "lrc");

		// addLyricLocal();
		addLyricsFromInternet();
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
		}
	}

	/**
	 * 为某一首歌曲从网上获取歌词
	 * 
	 * @param song
	 *            指定的歌曲
	 * @return 歌词
	 */
	private ArrayList<String> getLyricFromInternet(Song song) {
		return lyricHelper.getLyricFromInternet(song);
	}

	/**
	 * 在指定目录下，遍历指定扩展名
	 * 
	 * @param mol
	 *            用于存放遍历结果的容器
	 * @param path
	 *            指定的目录
	 * @param extensionName
	 *            指定的扩展名
	 */
	private void travel(ArrayList<MusicObject> mol, String path, String extensionName) {
		File dir = new File(path);
		File[] files = dir.listFiles();

		if (files == null) {
			return;
		}

		for (File file : files) {
			String absolutePath = file.getAbsolutePath();

			if (file.isDirectory()) {
				travel(mol, absolutePath, extensionName);
			} else {
				if (isSong(absolutePath)) {
					Song song = new Song(absolutePath);
					mol.add(song);
				} else if (isLyric(absolutePath)) {
					Lyric lyric = new Lyric(absolutePath);
					mol.add(lyric);
				}
			}
		}
	}

//	/**
//	 * 在本地搜索歌词文件，并添加到歌曲中
//	 */
//	private void addLyricLocal() {
//		for (int i = 0; i != songs.size(); i++) {
//			Song song = (Song) songs.get(i);
//			System.out.println("i = " + i + " " + song.title + " " + song.filePath);
//			// song.renameFileUsingTitleInTag();
//			// song.outputTag();
//
//			for (int j = 0; j != lyrics.size(); j++) {
//				// System.out.println("j = " + j);
//				Lyric lyric = (Lyric) lyrics.get(j);
//
//				if (matched(song, lyric)) {
//					System.out.println("local: found!");
//					song.setLyric(lyric);
//					break;
//				}
//			}
//		}
//	}
//
//	/**
//	 * 将数字形式的ASCII码字符串（如"&#73;&#46;&#32;&#84;&#79;）转化为字符串
//	 * 
//	 * @param input
//	 *            数字形式的ASCII码字符串
//	 * @return 字符串
//	 */
//	private String transform(String input) {
//		// String[] input = new String[] {
//		// "&#73;&#46;&#32;&#84;&#79;&#71;&#69;&#84;&#72;&#69;&#82;&#78;&#69;&#83;&#83;",
//		// "&#40;&#73;&#110;&#115;&#116;&#114;&#117;&#109;&#101;&#110;&#116;&#97;&#108;&#41;",
//		// "&#73;&#73;&#46;&#32;&#67;&#82;&#79;&#83;&#83;&#70;&#73;&#82;&#69;",
//		// "&#74;&#117;&#108;&#105;&#101;&#39;&#115;&#32;&#115;&#105;&#99;&#107;&#32;&#97;&#110;&#100;&#32;&#116;&#105;&#114;&#101;&#100;&#32;&#111;&#102;&#32;&#104;&#101;&#114;&#32;&#106;&#111;&#98;&#32;&#110;&#39;&#97;&#108;&#108;&#32;&#116;&#104;&#101;&#32;&#114;&#101;&#97;&#115;&#111;&#110;&#115;&#32;&#108;&#97;&#116;&#101;&#108;&#121;"};
//
//		StringBuilder result = new StringBuilder();
//		String[] token = input.split(";");
//		for (String string : token) {
//			// System.out.println(string);
//			string = string.replaceAll("&#", "");
//			char c = (char) Integer.parseInt(string);
//			System.out.print(c);
//
//			result.append(c);
//		}
//		return result.toString();
//	}

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
		return (song.title.contains(lyric.title) && song.artist.contains(lyric.artist)
				|| song.title.contains(lyric.title) && lyric.artist.contains(song.artist)
				|| lyric.title.contains(song.title) && lyric.artist.contains(song.artist) || lyric.title
				.contains(song.title) && song.artist.contains(lyric.artist));
	}

	/**
	 * 判断某一文件是否为音乐文件
	 * 
	 * @param filePath
	 *            文件的路径
	 * @return 是否为音乐文件
	 */
	private boolean isSong(String filePath) {
		return filePath.endsWith(".mp3") || filePath.endsWith(".wma");
	}

	/**
	 * 判断某一文件是否为歌词文件
	 * 
	 * @param filePath
	 *            文件的路径
	 * @return 是否为音乐文件
	 */
	private boolean isLyric(String filePath) {
		return filePath.endsWith(".lrc") || filePath.endsWith(".txt");
	}

	/**
	 * 构造函数
	 * 
	 * @param musicdirectory
	 *            指定的音乐文件目录
	 * @param lyricdirectory
	 *            指定的歌词文件目录
	 */
	public LyricAdder(String musicdirectory, String lyricdirectory) {
		this.musicDirectory = musicdirectory;
		this.lyricDirectory = lyricdirectory;

		songs = new ArrayList<MusicObject>();
		lyrics = new ArrayList<MusicObject>();

		lyricHelper = LyricHelper.getInstance();
	}
}