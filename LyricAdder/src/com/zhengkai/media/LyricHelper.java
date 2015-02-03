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

	/**
	 * 获取各个歌词助手类的实例
	 */
	private LyricHelper() {
		gecimeLyricHelper = GecimeLyricHelper.getInstance();
		baiduLyricHelper = BaiduLyricHelper.getInstance();
		lyricwikiLyricHelper = LyricwikiLyricHelper.getInstance();
	}

	/**
	 * 从网上获取歌词
	 * 
	 * @param song
	 *            要获取歌词的歌曲
	 * @return 歌词容器
	 */
	public ArrayList<String> getLyricFromInternet(Song song) {
		ArrayList<String> result = null;

		if (song.title == null) {
			return null;
		}

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

			// 各个站点的优先级：lyricwiki > 歌词迷 > 百度音乐
			// 首先使用歌名+歌手名进行搜索
			if (result == null) {
				if (lyricwiki)
					result = lyricwikiLyricHelper.getLyric(song);
			}
			if (result == null) {
				if (gecimi)
					result = gecimeLyricHelper.getLyric(song, true);
			}
			if (result == null) {
				if (baidu)
					result = baiduLyricHelper.getLyric(song, true, true);
			}

			// 如果没找到，则尝试单独使用歌曲名来搜索
			if (result == null) {
				if (gecimi)
					result = gecimeLyricHelper.getLyric(song, false);
			}
			if (result == null) {
				if (baidu)
					result = baiduLyricHelper.getLyric(song, false, true);
			}

		} while (result == null && song.hasModifyTitleMethod());

		return result;
	}

	/**
	 * 设定要使用的歌词站点
	 * 
	 * @param baidu
	 *            是否使用百度音乐
	 * @param gecimi
	 *            是否使用歌词迷
	 * @param lyricwiki
	 *            是否使用lyricwiki
	 */
	public void setLyricSites(boolean baidu, boolean gecimi, boolean lyricwiki) {
		this.baidu = baidu;
		this.gecimi = gecimi;
		this.lyricwiki = lyricwiki;
	}
}
