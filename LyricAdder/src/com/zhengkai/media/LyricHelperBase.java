package com.zhengkai.media;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词助手类的基类，包含一些公共操作
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public abstract class LyricHelperBase {
	protected String siteName = "";
	protected final static String encoding = "UTF-8";
	private final static int connectTimeout = 5000;
	private final static int readTimeout = 10000;

	protected static String urlStringBase;

	/**
	 * @param urlString
	 *        指定的URL
	 * @return HTML内容字符串
	 */
	protected String getHTMLFromURL(String urlString) {
		try {
			System.out.println(urlString);
			URL requestURL = new URL(urlString);
			URLConnection connection = requestURL.openConnection();
			connection.setConnectTimeout(connectTimeout);
			connection.setReadTimeout(readTimeout);
			InputStream inStream = null;
			try {
				inStream = connection.getInputStream();
			} catch (SocketTimeoutException e) {
				// System.out.println("连接超时。");
				return null;
			} catch (UnknownHostException | FileNotFoundException e) {
				// System.out.println("无法连接到服务器。");
				return null;
			} catch (IOException e) {
				// System.out.println("错误的请求，很可能是由于编码错误。");
				return null;
			}
			Scanner in = new Scanner(inStream);

			StringBuilder stringBuilder = new StringBuilder();
			while (in.hasNextLine()) {
				String string = in.nextLine();
				stringBuilder.append(string);
			}
			in.close();
			return stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * * 从指定的URL下载LRC歌词，并存入歌词容器
	 * 
	 * @param urlString
	 *        LRC歌词文件的地址
	 * @return 歌词
	 */
	protected ArrayList<String> getLRCFromURL(String urlString) {
		try {
			System.out.println(urlString);
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
		}

		return null;
	}

	/**
	 * 将歌名或歌手名中的空格替换为%20
	 * 
	 * @param string
	 *        输入字符串
	 * @return 替换后的字符串
	 */
	protected String replaceSpaces(String string) {
		return string.replaceAll(" ", "%20");
	}

	/**
	 * 判定歌曲与歌名、歌手名是否匹配
	 * 
	 * @param song
	 *        歌曲
	 * @param title
	 *        歌名
	 * @param artist
	 *        歌手名
	 * @return 是否匹配
	 */
	protected boolean matched(Song song, String title, String artist) {
		if (song.title == null || song.artist == null || title == null || artist == null) {
			return false;
		} else {
			return (song.titleLowerCase.contains(title.toLowerCase()) && song.artistLowerCase
					.contains(artist.toLowerCase()))
					|| (title.toLowerCase().contains(song.titleLowerCase) && song.artistLowerCase
							.contains(artist.toLowerCase()))
					|| (title.toLowerCase().contains(song.titleLowerCase) && artist
							.toLowerCase().contains(song.artistLowerCase))
					|| (song.titleLowerCase.contains(title.toLowerCase()) && artist
							.toLowerCase().contains(song.artistLowerCase));
		}
	}

	/**
	 * 判断字符串中是否含有中文
	 * 
	 * @param string
	 *        待判断字符串
	 * @return 是否含有中文
	 */
	protected boolean hasChinese(String string) {
		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher matcher = pattern.matcher(string);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	/**
	 * 处理URL中的用于搜索的字符串（歌名、歌手名）
	 * 1. 使用URLEncode.encode()编码中文
	 * 2. 由于encode()会将空格替换为“+”，而有些网站不认识“+”，所以还要将“+”替换为“%20”
	 * 
	 * @param string
	 *        待处理的字符串
	 * @return 处理完毕的字符串
	 */
	protected String processString(String string) {
		if (string == null) {
			return "";
		}
		String result = null;
		try {
			// System.out.println(string);
			result = URLEncoder.encode(string, encoding).replaceAll("\\+", "%20");
			// System.out.println(result);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 判定歌曲与歌名是否匹配
	 * 
	 * @param song
	 *        歌曲
	 * @param title
	 *        歌名
	 * @return 是否匹配
	 */
	protected boolean matched(Song song, String title) {
		if (song.title == null || title == null) {
			return false;
		} else {
			return (song.titleLowerCase.contains(title.toLowerCase()))
					|| (title.toLowerCase().contains(song.titleLowerCase));
		}
	}

	/**
	 * 在找到歌词后，输出提示信息
	 */
	protected void foundMessage() {
		System.out.println("在" + siteName + "找到了歌词！");
	}
}
