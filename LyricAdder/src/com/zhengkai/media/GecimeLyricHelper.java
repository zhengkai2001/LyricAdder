package com.zhengkai.media;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 从歌词迷网站下载歌词
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class GecimeLyricHelper extends LyricHelperBase {
	private final static String urlStringBase = "http://geci.me/api/lyric/";

	private static GecimeLyricHelper instance = new GecimeLyricHelper();

	public static GecimeLyricHelper getInstance() {
		return instance;
	}

	private GecimeLyricHelper() {
		siteName = new String("歌词迷");
	}

	/**
	 * 从歌词迷网站获取歌词
	 * 
	 * @param song
	 *            歌曲
	 * @param searchArtist
	 *            是否要搜索歌手名
	 * @return 歌词
	 */
	public ArrayList<String> getLyric(Song song, boolean searchArtist) {
		String urlString = null;
		if (searchArtist) {
			String title = processString(song.title);
			String artist = processString(song.artist);
			urlString = new String(urlStringBase + title + "/" + artist);
		} else {
			String title = processString(song.title);
			urlString = new String(urlStringBase + title);
		}

		String htmlString = getHTMLFromURL(urlString);
		if (htmlString == null) {
			return null;
		}

		JSONObject JSONResult = new JSONObject(htmlString);

		if (JSONResult.getInt("count") != 0) {
			// 得到歌词结果的JSONArray
			JSONArray resultArray = JSONResult.getJSONArray("result");
			// 只使用第一个歌词
			JSONObject result1 = resultArray.getJSONObject(0);
			// 得到歌词地址
			String lrcURLString = result1.getString("lrc");

			ArrayList<String> lyricLines = getLRCFromURL(lrcURLString);
			if (lyricLines != null) {
				foundMessage();
			}

			return lyricLines;
		}
		return null;
	}
}
