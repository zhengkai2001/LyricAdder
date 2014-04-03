package com.zhengkai.media;

import java.util.ArrayList;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONObject;

public class BaiduLyricHelper extends LyricHelperBase {
	private final static String urlStringBase = "http://music.baidu.com/search/lrc?key=";
	private final static String urlStringBase_lrc = "http://music.baidu.com";
	
	private static BaiduLyricHelper instance = new BaiduLyricHelper();
	
	public static BaiduLyricHelper getInstance() {
		return instance;
	}

	private BaiduLyricHelper() {
	}

	/**
	 * 从百度音乐获取歌词
	 * 
	 * @param song
	 *            歌曲
	 * @param searchArtist
	 *            是否要搜索歌手名
	 * @param strictLevel
	 *            匹配的严格程度：3-歌曲名和歌手名需要同时匹配；2-只需要歌曲名匹配；1-直接返回第一条结果
	 * @return 歌词
	 */

	protected ArrayList<String> getLyric(Song song, boolean searchArtist, int strictLevel) {
		try {
			String urlString = null;
			if (searchArtist) {
				urlString = new String(urlStringBase + song.title + " " + song.artist);
			} else {
				urlString = new String(urlStringBase + song.title);
			}

			String htmlString = getHTMLFromURL(urlString);
			if (htmlString == null) {
				return null;
			}
			// System.out.println(htmlString);

			Parser parser;
			AndFilter filter;

			// <div id="result_container">
			parser = new Parser(htmlString);
			filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("id",
					"result_container"));
			Node resultNode = parser.parse(filter).elementAt(0);

			// <li class="clearfix bb">
			parser = new Parser(resultNode.toHtml());
			filter = new AndFilter(new TagNameFilter("li"), new HasAttributeFilter("class",
					"clearfix bb"));
			NodeList songNodeList = parser.parse(filter);

			for (int i = 0; i != songNodeList.size(); i++) {
				Node songNode = songNodeList.elementAt(i);
				Parser songNodeParser;

				// System.out.println("i = " + i);
				// System.out.println(songNode.toHtml());

				// <span class="song-title">歌曲:<a href="/song/7451676"
				// title="歌曲名"> ......
				String title = null;
				songNodeParser = new Parser(songNode.toHtml());
				AndFilter titleFilter = new AndFilter(new TagNameFilter("span"),
						new HasAttributeFilter("class", "song-title"));
				Node titleNodeParent = songNodeParser.parse(titleFilter).elementAt(0);
				TagNode titleNode = (TagNode) titleNodeParent.getChildren().elementAt(1);
				title = titleNode.getAttribute("title");
				// System.out.println(title);

				String artist = null;
				// <span class="artist-title">歌手:<span class="author_list"
				// title="歌手名"> ......
				songNodeParser = new Parser(songNode.toHtml());
				AndFilter artistFilter = new AndFilter(new TagNameFilter("span"),
						new HasAttributeFilter("class", "artist-title"));
				Node artistNodeParent = songNodeParser.parse(artistFilter).elementAt(0);
				TagNode artistNode = (TagNode) artistNodeParent.getChildren().elementAt(1);
				artist = artistNode.getAttribute("title");
				// System.out.println(artist);

				boolean found = false;
				if (strictLevel == 3) {
					if (matched(song, title, artist)) {
						found = true;
					}
				} else if (strictLevel == 2) {
					if (matched(song, title)) {
						found = true;
					}
				} else if (strictLevel == 1) {
					found = true;
				}

				if (found) {
					// System.out.println(songNode.toHtml());
					// System.out.println("-----");

					// <span class="lyric-action">
					songNodeParser = new Parser(songNode.toHtml());
					AndFilter lyricFilter = new AndFilter(new TagNameFilter("span"),
							new HasAttributeFilter("class", "lyric-action"));
					Node lyricNodeParent = (Span) songNodeParser.parse(lyricFilter).elementAt(0);
					if (lyricNodeParent == null) {
						continue;
					}
					// System.out.println(lyricNodeParent.toHtml());
					NodeList lyricNodeList = lyricNodeParent.getChildren();

					for (int j = 0; j != lyricNodeList.size(); j++) {
						// System.out.println("j = " + j + ": " +
						// lyricNodeList.elementAt(j).toHtml());
						NodeList lyricNodeChildren = lyricNodeList.elementAt(j).getChildren();
						if (lyricNodeChildren == null) {
							continue;
						} else {
							// <a
							// class="down-lrc-btn { 'href':'/data2/lrc/12741704/12741704.lrc' }"
							// href="#">下载LRC歌词</a>
							for (int k = 0; k != lyricNodeChildren.size(); k++) {
								if (lyricNodeChildren.elementAt(k).getText().contains("下载LRC歌词")) {
									String downloadClass = ((TagNode) lyricNodeList.elementAt(j))
											.getAttribute("class");
									// System.out.println(downloadClass);

									int openingBrace = downloadClass.indexOf('{');
									int closingQuote = downloadClass.lastIndexOf('}');
									String href = downloadClass.substring(openingBrace,
											closingQuote + 1);
									// System.out.println(href);

									JSONObject jsonObject = new JSONObject(href);
									String lrcURLString = jsonObject.getString("href");
									// System.out.println(lrcURLString);

									System.out.println("baidu: found! Download successfully.");
									return getLRCFromURL(urlStringBase_lrc + lrcURLString);
								}
							}
						}
					}
				}
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return null;
	}
}
