package com.zhengkai.media;

import java.util.ArrayList;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.json.JSONObject;

/**
 * 从百度音乐下载歌词
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class BaiduLyricHelper extends LyricHelperBase {
	private final static String urlStringBase = "http://music.baidu.com/search/lrc?key=";
	private final static String urlStringBase_lrc = "http://music.baidu.com";

	private static BaiduLyricHelper instance = new BaiduLyricHelper();

	public static BaiduLyricHelper getInstance() {
		return instance;
	}

	private BaiduLyricHelper() {
		siteName = new String("百度音乐");
	}

	/**
	 * 从百度音乐获取歌词
	 * 
	 * @param song
	 *            歌曲
	 * @param searchArtist
	 *            是否要搜索歌手名
	 * @param strictLevel
	 *            是否启用严格匹配：true - 歌名、歌手名都必须一致；false - 只需要歌名一致
	 * @return 歌词
	 */

	protected ArrayList<String> getLyric(Song song, boolean searchArtist,
			boolean strict) {
		String urlString = null;
		if (searchArtist) {
			String title = processString(song.title);
			String artist = processString(song.artist);
			urlString = new String(urlStringBase + title + "%20" + artist);
		} else {
			String title = processString(song.title);
			urlString = new String(urlStringBase + title);
		}

		try {
			String htmlContent = null;
			TagNode node = null;

			// 使用HtmlCleaner来解析HTML
			HtmlCleaner cleaner = new HtmlCleaner();

			htmlContent = getHTMLFromURL(urlString);
			if (htmlContent == null) {
				return null;
			}
			node = cleaner.clean(htmlContent);

			// <li class="clearfix bb">
			Object[] songDivs = node
					.evaluateXPath("//li[@class='clearfix bb']");
			if (songDivs == null || songDivs.length == 0) {
				return null;
			}
			for (Object songDiv : songDivs) {
				// 找出歌名
				// <span class="song-title">
				// <a title="I'm Yours">
				Object[] anchors = ((TagNode) songDiv)
						.evaluateXPath("//span[@class='song-title']/a");
				if (anchors == null || anchors.length == 0) {
					return null;
				}
				String title = ((TagNode) anchors[0])
						.getAttributeByName("title");
				// System.out.println(title);

				// 找出歌手名
				// <span class="artist-title">
				// <span title="Jason Mraz">
				Object[] spans = ((TagNode) songDiv)
						.evaluateXPath("//span[@class='artist-title']/span");
				if (spans == null || spans.length == 0) {
					return null;
				}
				String artist = ((TagNode) spans[0])
						.getAttributeByName("title");
				// System.out.println(artist);

				// 判断歌曲是否与搜索结果匹配
				boolean found = false;
				if (strict) {
					if (matched(song, title, artist)) {
						found = true;
					}
				} else {
					if (matched(song, title)) {
						found = true;
					}
				}

				found = true;
				// 若匹配，则下载LRC歌词
				if (found) {
					// 找到下载LRC歌词的超链接
					// <span class="lyric-action">
					// <span><a></a></span>
					// <a
					// class="down-lrc-btn { 'href':'/data2/lrc/31252011/31252011.lrc' }"
					// href="#"></a>
					anchors = ((TagNode) songDiv)
							.evaluateXPath("//span[@class='lyric-action']/a");
					if (anchors == null || anchors.length == 0) {
						return null;
					}
					String downloadClass = ((TagNode) anchors[0])
							.getAttributeByName("class");
					if (downloadClass.contains("down-lrc-btn")) {
						int openingBrace = downloadClass.indexOf('{');
						int closingBrace = downloadClass.lastIndexOf('}');
						String href = downloadClass.substring(openingBrace,
								closingBrace + 1);
						// System.out.println(href);

						JSONObject jsonObject = new JSONObject(href);
						String lrcURLString = jsonObject.getString("href");
						// System.out.println(lrcURLString);

						ArrayList<String> lyricLines = getLRCFromURL(urlStringBase_lrc
								+ lrcURLString);
						if (lyricLines != null) {
							foundMessage();
						}

						return lyricLines;
					}
				}
			}
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		return null;
	}
}
