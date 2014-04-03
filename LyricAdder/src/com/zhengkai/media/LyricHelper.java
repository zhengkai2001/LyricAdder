package com.zhengkai.media;

import java.util.ArrayList;

public class LyricHelper {
	GecimeLyricHelper gecimeLyricHelper;
	BaiduLyricHelper baiduLyricHelper;
	LyricwikiLyricHelper lyricwikiLyricHelper;
	
	private static LyricHelper instance = null;

	public static LyricHelper getInstance() {
		if (instance == null) {
			instance = new LyricHelper();
		}
		return instance;
	}
	
	private LyricHelper() {
		gecimeLyricHelper = GecimeLyricHelper.getInstance();
		baiduLyricHelper = BaiduLyricHelper.getInstance();
		lyricwikiLyricHelper = LyricwikiLyricHelper.getInstance();
	}

	public ArrayList<String> getLyricFromInternet(Song song) {
		ArrayList<String> result = null;

		boolean first = true;
		// 在找到歌词前，不断尝试修改歌曲名
		do {
			String before = song.title;
			song.tryModifyTitle();
			// System.out.println(song.title);

			if (first) {
				first = false;
			} else {
				if (song.title.equals(before)) {
					continue;
				}
			}

			// 使用歌曲名+歌手名来搜索

			// LyricWiki连接速度太慢，暂时弃用
			// result = lyricwikiLyricHelper.getLyric(song);
			
			// gecime的lrc服务经常不可用，所以先去百度精确查找
			if (result == null) {
				result = baiduLyricHelper.getLyric(song, true, 3);
			}
			if (result == null) {
				result = gecimeLyricHelper.getLyric(song, true);
			}

			// 如果没找到，则尝试单独使用歌曲名来搜索
			if (result == null) {
				result = baiduLyricHelper.getLyric(song, false, 3);
			}
			if (result == null) {
				result = gecimeLyricHelper.getLyric(song, false);
			}

			// 如果仍然没有找到，则放宽条件，只在百度音乐搜索并匹配歌名，
			if (result == null) {
				result = baiduLyricHelper.getLyric(song, false, 2);
			}

			// 如果仍然没有找到，则直接取回百度音乐的第一条结果
			if (result == null) {
				result = baiduLyricHelper.getLyric(song, false, 1);
			}
		} while (result == null && song.hasModifyTitleMethod());

		return result;
	}

}
