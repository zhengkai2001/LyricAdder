package com.zhengkai.media;

import java.util.ArrayList;

import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * 从lyricwiki网站下载歌词
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class LyricwikiLyricHelper extends LyricHelperBase {
	private final static String urlStringBase = "http://lyrics.wikia.com/api.php?func=getSong";

	private static LyricwikiLyricHelper instance = new LyricwikiLyricHelper();

	public static LyricwikiLyricHelper getInstance() {
		return instance;
	}

	private LyricwikiLyricHelper() {
	}

	/**
	 * 未完成，从歌词页面提取歌词还没写 从LyricWiki网站上获取歌词 （http://api.wikia.com/wiki/LyricWiki_API/REST）
	 * 
	 * @param song
	 *        歌曲
	 * @return 歌词
	 */
	public ArrayList<String> getLyric(Song song) {
		String urlString = urlStringBase + "&artist=" + song.artist + "&song=" + song.title;
		String htmlString = getHTMLFromURL(urlString);
		if (htmlString == null) {
			return null;
		}

		try {
			Parser parser;

			parser = new Parser(htmlString);
			TagNameFilter preFilter = new TagNameFilter("pre");
			String pre = parser.extractAllNodesThatMatch(preFilter).elementAt(0)
					.getNextSibling().getText().trim();
			// System.out.println(pre);

			if (pre.equals("Not found")) {
				return null;
			} else {
				parser = new Parser(htmlString);
				AndFilter urlFilter = new AndFilter(new TagNameFilter("a"),
						new HasAttributeFilter("title", "url"));

				TagNode lyricNode = (TagNode) parser.parse(urlFilter).elementAt(0);
				String href = lyricNode.getAttribute("href");

				htmlString = getHTMLFromURL(href);
				if (htmlString == null) {
					return null;
				}
				System.out.println(htmlString);

				// <div class='lyricbox'>
				parser = new Parser("D:\\zhengkai\\Desktop\\1.txt");
				AndFilter lyricboxFilter = new AndFilter(new TagNameFilter("div"),
						new HasAttributeFilter("class", "lyricbox"));
				NodeList lyricboxNodeList = parser.parse(lyricboxFilter).elementAt(0)
						.getChildren();
				// System.out.println(lyricboxNodeList.toHtml());

				for (int i = 0; i != lyricboxNodeList.size(); i++) {
					System.out.println("i = " + i + ": "
							+ lyricboxNodeList.elementAt(i).toHtml());
				}

				// <div class="rtMatcher">

				ArrayList<String> result = null;
				// if (result != null) {
				// }

				return result;

			}

		} catch (ParserException e) {
			e.printStackTrace();
		}

		return null;
	}
}
