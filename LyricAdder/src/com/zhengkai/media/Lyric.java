package com.zhengkai.media;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 歌词类，代表歌词文件，例如.lrc和.txt文件
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class Lyric extends MusicObject {
	private final static String encoding = "UTF-8";
	private final static String[] filter = new String[] { "制作", "qq:", "qq：",
			"★", "www.", ".com", "匹配时间为", "edit:", "[ti:", "[ar:", "[al:",
			"歌手:", "专辑:", "词 曲:", "http:", "xiamilyrics", "歌词整理：", "lrc" };

	// 原始lrc歌词，包含时间信息等
	private ArrayList<String> lyricLines;

	public Lyric(ArrayList<String> lyricLines) {
		this.lyricLines = lyricLines;
	}

	public Lyric(String filePath) {
		super(filePath);
		this.lyricLines = readLyric();
	}

	/**
	 * 从文件中读取歌词
	 * 
	 * @return 歌词容器
	 */
	public ArrayList<String> readLyric() {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			FileInputStream fis = new FileInputStream(filePath);
			InputStreamReader isr = new InputStreamReader(fis, encoding);
			BufferedReader br = new BufferedReader(isr);

			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			br.close();
			isr.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lines;
	}

	/**
	 * 删除歌词中连续的空行，即将两个及以上的空行替换为一个
	 * 
	 * @param lyricLine
	 *            歌词字符串
	 * @return 删除了连续空行的歌词字符串
	 */
	private String deleteConsecutiveEmptyLines(String lyricLine) {
		while (true) {
			if (lyricLine.contains("\n\n\n")) {
				lyricLine = lyricLine.replaceAll("\n\n\n", "\n\n");
			} else {
				break;
			}
		}
		return lyricLine;
	}

	/**
	 * 删除歌词中的冗余信息
	 * 
	 * @param lyricLine
	 *            歌词字符串
	 * @return 删除了冗余信息的歌词字符串
	 */
	private String deleteRedundantInfo(String lyricLine) {
		if (containsRubbishInfo(lyricLine)) {
			return "";
		} else {
			return lyricLine;
		}
	}

	/**
	 * 判断某一行歌词是否含有冗余信息
	 * 
	 * @param lyricLine
	 *            歌词行字符串
	 * @return 是否含有冗余信息
	 */
	private boolean containsRubbishInfo(String lyricLine) {
		for (int i = 0; i != filter.length; i++) {
			if (lyricLine.toLowerCase().contains(filter[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 删除lrc歌词的时间戳
	 * 
	 * @param line
	 *            lrc歌词行字符串
	 * @return 删除了时间戳的字符串
	 */
	private String deleteTimeStamp(String line) {
		return deleteSectionBetween(line, '[', ']');
	}

	/**
	 * 将歌词容器中的歌词整合为单一的歌词字符串
	 * 
	 * @return 歌词字符串
	 */
	public String getLyricString() {
		StringBuilder lyricStringBuilder = new StringBuilder();
		for (String lyricLine : lyricLines) {
			lyricLine = deleteTimeStamp(lyricLine);
			lyricLine = deleteRedundantInfo(lyricLine);

			lyricStringBuilder.append(lyricLine);
			lyricStringBuilder.append("\n");
		}
		String lyricString = lyricStringBuilder.toString().trim();

		lyricString = deleteConsecutiveEmptyLines(lyricString);
		return lyricString;
	}

	/**
	 * 将歌词保存到本地文件
	 * 
	 * @param lyricDirectory
	 *            要保存的路径
	 */
	public void save(String lyricDirectory) {

	}
}
