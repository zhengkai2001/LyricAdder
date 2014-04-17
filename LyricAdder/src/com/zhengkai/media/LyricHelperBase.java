package com.zhengkai.media;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 歌词助手类的基类，包含一些公共操作
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public abstract class LyricHelperBase {
	protected final static String encoding = "UTF-8";
	private final static int connectTimeout = 2000;
	private final static int readTimeout = 5000;

	protected static String urlStringBase;

	/**
	 * @param urlString
	 *        指定的URL
	 * @return HTML内容字符串
	 */
	protected String getHTMLFromURL(String urlString) {
		try {
			urlString = replaceSpaces(urlString);
			System.out.println(urlString);
			URL requestURL = new URL(urlString);
			URLConnection connection = requestURL.openConnection();
			connection.setConnectTimeout(connectTimeout);
			connection.setReadTimeout(readTimeout);
			InputStream inStream = null;
			try {
				inStream = connection.getInputStream();
			} catch (SocketTimeoutException e) {
				System.out.println("Timed out!!!");
				return null;
			} catch (UnknownHostException e) {
				System.out.println("Service unavailable!!!");
				return null;
			} catch (FileNotFoundException e) {
				System.out.println("Tag Error!!!");
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
	private String replaceSpaces(String string) {
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
		if (song.title == null || song.artist == null) {
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
	 * 判定歌曲与歌名是否匹配
	 * 
	 * @param song
	 *        歌曲
	 * @param title
	 *        歌名
	 * @return 是否匹配
	 */
	protected boolean matched(Song song, String title) {
		return (song.titleLowerCase.contains(title.toLowerCase()))
				|| (title.toLowerCase().contains(song.titleLowerCase));
	}

}
