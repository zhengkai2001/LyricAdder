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
	private final static String[] infoList = new String[] {
			"制作", "QQ", "★", "www.", ".com", "匹配时间为", "edit:", "[ti:", "[ar:", "[al:", "歌手:",
			"专辑:", "词 曲:" };

	// 原始lrc歌词，包含时间信息等
	private ArrayList<String> lyricLines;

	public Lyric(ArrayList<String> lyricLines) {
		this.lyricLines = lyricLines;
	}

	public Lyric(String filePath) {
		super(filePath);
		this.lyricLines = readLyric();
	}

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

	private String deleteRubbishInfo(String lyricLine) {
		if (containsRubbishInfo(lyricLine)) {
			return "";
		} else {
			return lyricLine;
		}
	}

	private boolean containsRubbishInfo(String lyricLine) {
		for (int i = 0; i != infoList.length; i++) {
			if (lyricLine.contains(infoList[i])) {
				return true;
			}
		}
		return false;
	}

	// 删除lrc歌词的时间戳
	private String deleteTimeStamp(String line) {
		return deleteSectionBetween(line, '[', ']');
	}

	public String getLyricString() {
		StringBuilder lyricStringBuilder = new StringBuilder();
		for (String lyricLine : lyricLines) {
			lyricLine = deleteTimeStamp(lyricLine);
			lyricLine = deleteRubbishInfo(lyricLine);

			lyricStringBuilder.append(lyricLine);
			lyricStringBuilder.append("\n");
		}
		String lyricString = lyricStringBuilder.toString().trim();

		lyricString = deleteConsecutiveEmptyLines(lyricString);
		return lyricString;
	}

	public void save(String lyricDirectory) {

	}
}
