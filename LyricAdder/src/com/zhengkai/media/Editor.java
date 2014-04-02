package com.zhengkai.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class Editor {
	private final static String encoding = "UTF-8";
	private final static String urlStringBase_gecime = "http://geci.me/api/lyric/";
	private final static String urlStringBase_baidu = "http://music.baidu.com/search/lrc?key=";
	private final static String urlStringBase_baidu_lrc = "http://music.baidu.com";

	private String musicDirectory;
	private String lyricDirectory;

	private ArrayList<MusicObject> songs;
	private ArrayList<MusicObject> lyrics;

	public void addLyrics() {
		travel(songs, musicDirectory, "mp3");
		travel(lyrics, lyricDirectory, "lrc");

		for (int i = 0; i != songs.size(); i++) {
			Song song = (Song) songs.get(i);
			System.out.println("i = " + i + " " + song.title + " " + song.filePath);
			// song.renameFileUsingTitleInTag();
			// song.outputTag();

			boolean foundLyricInLocal = false;
			for (int j = 0; j != lyrics.size(); j++) {
				// System.out.println("j = " + j);
				Lyric lyric = (Lyric) lyrics.get(j);

				if (areSameSong(song, lyric)) {
					System.out.println("local: found!");
					foundLyricInLocal = true;
					song.setLyric(lyric);
					break;
				}
			}

			if (!foundLyricInLocal) {
				System.out.println("local: not found");
				ArrayList<String> lyricLines = getLyricFromInternet(song);
				if (lyricLines != null) {
					Lyric lyric = new Lyric(lyricLines);
					lyric.save(lyricDirectory);
					song.setLyric(lyric);
				}
			}

			System.out.println();
		}
	}

	private boolean areSameSong(Song song, Lyric lyric) {
		return (song.artist.equalsIgnoreCase(lyric.artist) && song.title
				.equalsIgnoreCase(lyric.title));
	}

	private ArrayList<String> getLyricFromInternet(Song song) {
		ArrayList<String> result = null;

		result = getLyricFrom_gecime(song);
		if (result == null) {
			result = getLyricFrom_baidu(song);
		}

		return result;
	}

	private ArrayList<String> getLRCFromURL(String urlString) {
		try {
			ArrayList<String> lyricLines = new ArrayList<String>();

			URL lrcURL;
			lrcURL = new URL(urlString);
			URLConnection connection = lrcURL.openConnection();
			connection.connect();

			InputStream inputStream = connection.getInputStream();
			if (inputStream == null) {
				return null;
			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
						encoding));

				// 开始下载歌词
				String line;
				while ((line = reader.readLine()) != null) {
					lyricLines.add(line);
				}

				reader.close();
				return lyricLines;
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}

		return null;
	}

	private String deleteConsecutiveEmptyLines(String string) {
		string = string.replaceAll("\t", "");
		while (true) {
			if (string.contains(" \r\n")) {
				string = string.replaceAll(" \r\n", "\r\n");
			} else {
				break;
			}
		}
		while (true) {
			if (string.contains("\r\n ")) {
				string = string.replaceAll("\r\n ", "\r\n");
			} else {
				break;
			}
		}
		while (true) {
			if (string.contains("\r\n\r\n")) {
				string = string.replaceAll("\r\n\r\n", "\r\n");
			} else {
				break;
			}
		}

		return string;
	}

	private ArrayList<String> getLyricFrom_baidu(Song song) {

		ArrayList<String> result;
		String urlString;
		String urlStringBase = urlStringBase_baidu;

		urlString = new String(urlStringBase + song.title + " " + song.artist);
		result = getLyricFrom_baidu(song, urlString, true);

		if (result == null) {
			urlString = new String(urlStringBase + song.title);
			result = getLyricFrom_baidu(song, urlString, false);
		}
		if (result == null) {
			System.out.println("baidu: not found");
		}
		return result;
	}

	private ArrayList<String> getLyricFrom_baidu(Song song, String urlString, boolean hasArtist) {
		try {
			String htmlString = getHTMLFromURL(urlString);
			htmlString = deleteConsecutiveEmptyLines(htmlString);
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
				// System.out.println(node.toHtml());

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
				if(hasArtist) {
					// <span class="artist-title">歌手:<span class="author_list"
					// title="歌手名"> ......
					songNodeParser = new Parser(songNode.toHtml());
					AndFilter artistFilter = new AndFilter(new TagNameFilter("span"),
							new HasAttributeFilter("class", "artist-title"));
					Node artistNodeParent = songNodeParser.parse(artistFilter).elementAt(0);
					TagNode artistNode = (TagNode) artistNodeParent.getChildren().elementAt(1);
					artist = artistNode.getAttribute("title");
					// System.out.println(artist);
				}
				
				boolean found = false;
				if(hasArtist) {
					if (song.title.equalsIgnoreCase(title) && song.artist.equalsIgnoreCase(artist)) {
						found = true;
					}
				} else {
					if (song.title.equalsIgnoreCase(title)) {
						found = true;
					}
				}

				if (found) {
					// <span class="lyric-action">
					songNodeParser = new Parser(songNode.toHtml());
					System.out.println(songNode.toHtml());
					System.out.println("-----");
					AndFilter lyricFilter = new AndFilter(new TagNameFilter("span"),
							new HasAttributeFilter("class", "lyric-action"));
					Node lyricNodeParent = (Span) songNodeParser.parse(lyricFilter).elementAt(0);
					System.out.println(lyricNodeParent.toHtml());

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

									System.out.println("baidu: found!");
									return getLRCFromURL(urlStringBase_baidu_lrc + lrcURLString);
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

	// 将歌名或歌手名中的空格替换为%20
	private String replaceSpaces(String title) {
		return title.replaceAll(" ", "%20");
	}

	private ArrayList<String> getLyricFrom_gecime(Song song) {
		String urlStringBase = urlStringBase_gecime;
		// 使用歌曲名+歌手名来搜索
		String urlString = new String(urlStringBase + song.title + "/" + song.artist);
		JSONObject JSONResult = new JSONObject(getResultFromURL(urlString));

		// 如果没找到，则单独使用歌曲名来搜索
		if (JSONResult.getInt("count") == 0) {
			urlString = new String(urlStringBase + song.title);
			JSONResult = new JSONObject(getResultFromURL(urlString));
		}

		// 如果仍然没找到，则尝试修改歌曲名
		while (JSONResult.getInt("count") == 0 && song.hasModifyTitleMethod()) {
			if (JSONResult.getInt("count") == 0) {
				song.tryModifyTitle();
				System.out.println(song.title);
				urlString = new String(urlStringBase + song.title);
				JSONResult = new JSONObject(getResultFromURL(urlString));
			}
		}

		// 如果找到了
		if (JSONResult.getInt("count") != 0) {
			System.out.println("gecime: found!");

			// 得到歌词结果的JSONArray
			JSONArray resultArray = JSONResult.getJSONArray("result");
			// 只使用第一个歌词
			JSONObject result1 = resultArray.getJSONObject(0);
			// 得到歌词地址
			String lrcURLString = result1.getString("lrc");

			ArrayList<String> lyricLines = getLRCFromURL(lrcURLString);
			return lyricLines;
		}

		System.out.println("gecime: not found");
		return null;
	}

	private String getHTMLFromURL(String urlString) {
		try {
			urlString = replaceSpaces(urlString);
			System.out.println(urlString);
			URL requestURL = new URL(urlString);
			StringBuilder stringBuilder = new StringBuilder();
			HttpURLConnection connection;
			connection = (HttpURLConnection) requestURL.openConnection();
			connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), encoding));

			String line = null;
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append("\r\n");
			}
			connection.disconnect();

			return stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getResultFromURL(String urlString) {
		try {
			urlString = replaceSpaces(urlString);
			System.out.println(urlString);
			URL requestURL = new URL(urlString);
			StringBuilder stringBuilder = new StringBuilder();

			byte[] buffer = new byte[8192];

			InputStream inputStream = requestURL.openStream();
			while ((inputStream.read(buffer, 0, 8192)) != -1) {
				String str = new String(buffer, encoding);
				stringBuilder.append(str);
				// System.out.println(str);
			}
			inputStream.close();

			return stringBuilder.toString();
		} catch (IOException e) {
		}
		return null;
	}

	private void travel(ArrayList<MusicObject> mol, String path, String extensionName) {
		File dir = new File(path);
		File[] files = dir.listFiles();

		if (files == null) {
			return;
		}

		for (int i = 0; i != files.length; i++) {
			String absolutePath = files[i].getAbsolutePath();

			if (files[i].isDirectory()) {
				travel(mol, absolutePath, extensionName);
			} else {
				int dot = absolutePath.lastIndexOf('.');
				String fileExtensionName = absolutePath.substring(dot + 1);

				if (extensionName.equalsIgnoreCase(fileExtensionName)) {
					if (isSong(extensionName)) {
						Song song = new Song(absolutePath);
						mol.add(song);
					} else if (isLyric(extensionName)) {
						Lyric lyric = new Lyric(absolutePath);
						mol.add(lyric);
					}
				}
			}
		}
	}

	private boolean isSong(String extensionName) {
		return extensionName.equalsIgnoreCase("mp3") || extensionName.equalsIgnoreCase("wma");
	}

	private boolean isLyric(String extensionName) {
		return extensionName.equalsIgnoreCase("lrc") || extensionName.equalsIgnoreCase("txt");
	}

	public Editor(String musicdirectory, String lyricdirectory) {
		this.musicDirectory = musicdirectory;
		this.lyricDirectory = lyricdirectory;

		songs = new ArrayList<MusicObject>();
		lyrics = new ArrayList<MusicObject>();
	}
}