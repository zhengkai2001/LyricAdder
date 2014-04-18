package com.zhengkai.media;

import java.util.ArrayList;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CompactHtmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

/**
 * 从lyricwiki网站下载歌词
 * http://api.wikia.com/wiki/LyricWiki_API
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class LyricwikiLyricHelper extends LyricHelperBase {
	private final static String urlStringBase = "http://lyrics.wikia.com/api.php?func=getSong";
	private final static String xmlFormat = "&fmt=xml";

	private static LyricwikiLyricHelper instance = new LyricwikiLyricHelper();

	public static LyricwikiLyricHelper getInstance() {
		return instance;
	}

	private LyricwikiLyricHelper() {
	}

	/**
	 * 从LyricWiki获取歌词
	 * 
	 * @param song
	 *        歌曲
	 * @return 歌词
	 */
	public ArrayList<String> getLyric(Song song) {
		// LyricWiki只能是精确搜索，如果缺少歌名或者歌手名，就无法搜索
		String urlString = urlStringBase + "&artist=" + song.artist + "&song=" + song.title
				+ xmlFormat;

		try {
			String htmlContent = null;
			TagNode node = null;
			Object[] anchors = null;
			Object anchor = null;

			HtmlCleaner cleaner = new HtmlCleaner();

			htmlContent = getHTMLFromURL(urlString);
			if (htmlContent == null) {
				return null;
			}
			node = cleaner.clean(htmlContent);

			// <LyricsResult>
			// <url>http://lyrics.wikia.com/Tool:Schism</url>
			// </LyricsResult>
			anchors = node.evaluateXPath("//url");
			if (anchors == null || anchors.length == 0) {
				return null;
			}
			anchor = anchors[0];
			String href = ((TagNode) anchor).getText().toString();

			// 如果歌词网址为“http://lyrics.wikia.com”，说明没有搜索到歌词
			if (href.equals("http://lyrics.wikia.com")) {
				return null;
			}

			// 获取歌词显示网页
			htmlContent = getHTMLFromURL(href);
			if (htmlContent == null) {
				return null;
			}
			node = cleaner.clean(htmlContent);

			// <div class="lyricbox">
			// <div><a></a><span></span></div>
			// Memories, how they fade so fast<br>Look back, that is no escape<br>
			// </div>

			// 提取歌词
			anchors = node.evaluateXPath("//div[@class='lyricbox']");
			if (anchors == null || anchors.length == 0) {
				return null;
			}
			anchor = anchors[0];

			CompactHtmlSerializer chs = new CompactHtmlSerializer(new CleanerProperties());
			String rawLyricDivString = chs.getAsString((TagNode) anchor);
			return getLyricFromRaw(rawLyricDivString);
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ArrayList<String> getLyricFromRaw(String lyricString) {
		// 提取出歌词文本
		String beginString = "<span class=\"adNotice\">Ad</span></div>";
		String endString = "<!--";
		int begin = lyricString.indexOf(beginString);
		int end = lyricString.lastIndexOf(endString);

		if (begin != -1 && end != lyricString.length() && begin + beginString.length() < end) {
			lyricString = lyricString.substring(begin + beginString.length(), end);
		} else {
			return null;
		}

		// 将 &apos; 转换为 '
		lyricString = lyricString.replaceAll("&apos;", "'");

		// 去除所有自然换行符
		lyricString = lyricString.replaceAll("\n", "");

		// 以 <br /> 为换行符，建立歌词容器
		// 如果连续出现两个 <br />，说明此处需要空一行
		lyricString = lyricString.replaceAll("<br /><br />", "\n<br />");

		String[] lines = lyricString.split("<br />");
		ArrayList<String> result = new ArrayList<String>();
		for (String line : lines) {
			result.add(line);
		}
		return result;
	}
}
