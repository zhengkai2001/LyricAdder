package com.zhengkai.media;

import java.util.ArrayList;

/**
 * 调用具体的歌词网站助手的助手类
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class LyricHelper {
	GecimeLyricHelper gecimeLyricHelper;
	BaiduLyricHelper baiduLyricHelper;
	LyricwikiLyricHelper lyricwikiLyricHelper;

	boolean baidu, gecimi, lyricwiki;

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

			// 首先使用歌曲名+歌手名来搜索

			// gecime的lrc服务经常不可用，所以百度优先级更高
			if (result == null) {
				if (baidu)
					result = baiduLyricHelper.getLyric(song, true, true);
			}
			if (result == null) {
				if (gecimi)
					result = gecimeLyricHelper.getLyric(song, true);
			}
			// LyricWiki连接速度较慢，不建议使用
			if (result == null) {
				if (lyricwiki)
					result = lyricwikiLyricHelper.getLyric(song);
			}

			// 如果没找到，则尝试单独使用歌曲名来搜索
			if (result == null) {
				if (baidu)
					result = baiduLyricHelper.getLyric(song, false, true);
			}
			if (result == null) {
				if (gecimi)
					result = gecimeLyricHelper.getLyric(song, false);
			}

		} while (result == null && song.hasModifyTitleMethod());

		// 如果仍然没有找到，则直接取回百度音乐的第一条结果
		if (result == null) {
			if (baidu)
				result = baiduLyricHelper.getLyric(song, false, false);
		}

		return result;
	}

	public void setLyricSites(boolean baidu, boolean gecimi, boolean lyricwiki) {
		this.baidu = baidu;
		this.gecimi = gecimi;
		this.lyricwiki = lyricwiki;
	}
}
