package com.zhengkai.media;

import java.io.File;

/**
 * 所有音乐文件类的基类
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class MusicObject {
	protected File file;
	protected String filePath;

	public String fileFullName;
	public String fileName;
	public String extensionName;

	// 歌名和歌手名
	public String title;
	public String artist;

	public boolean hasTitle;
	public boolean hasArtist;

	// 用于和搜索结果比较的歌名和歌手名
	public String titleLowerCase;
	public String artistLowerCase;

	/**
	 * 默认构造函数
	 */
	public MusicObject() {
	}

	/**
	 * 通过指定的音乐文件位置构造音乐对象
	 * 
	 * @param filePath
	 */
	public MusicObject(String filePath) {
		this.file = new File(filePath);
		this.filePath = filePath;
		this.fileFullName = getFileFullName(this.filePath);
		this.fileName = getFileName(this.fileFullName);
		this.extensionName = getExtensionName(this.fileFullName);

		this.title = getTitle(this.fileName);
		if (this.title != null) {
			this.hasTitle = true;
			this.titleLowerCase = this.title.toLowerCase();
		} else {
			this.hasTitle = false;
		}

		this.artist = getArtist(this.fileName);
		if (this.artist != null) {
			this.hasArtist = true;
			this.artistLowerCase = this.artist.toLowerCase();
		} else {
			this.hasArtist = false;
		}
	}

	/**
	 * 根据音乐文件的路径得到音乐文件的完整文件名
	 * 
	 * @param filePath
	 *            路径
	 * @return 完整文件名
	 */
	private String getFileFullName(String filePath) {
		int slash = filePath.lastIndexOf('\\');
		return filePath.substring(slash + 1);
	}

	/**
	 * 根据音乐文件的完整文件名得到文件名（就是去掉了扩展名）
	 * 
	 * @param fileFullName
	 *            完整文件名
	 * @return 文件名
	 */
	private String getFileName(String fileFullName) {
		int dot = fileFullName.lastIndexOf('.');
		return fileFullName.substring(0, dot);
	}

	/**
	 * 根据音乐文件的完整文件名得到扩展名
	 * 
	 * @param fileFullName
	 *            完整文件名
	 * @return 扩展名
	 */
	private String getExtensionName(String fileFullName) {
		int dot = fileFullName.lastIndexOf('.');
		return fileFullName.substring(dot);
	}

	/**
	 * 根据文件名得到歌名
	 * 
	 * @param fileName
	 *            文件名
	 * @return 歌名
	 */
	protected String getTitle(String fileName) {
		int dash = fileName.indexOf('-');
		if (dash <= -1 || dash + 1 >= fileName.length()) {
			return fileName;
		} else {
			return fileName.substring(dash + 1, fileName.length()).trim();
		}
	}

	/**
	 * 根据文件名得到歌手名
	 * 
	 * @param fileName
	 *            文件名
	 * @return 歌手名
	 */
	protected String getArtist(String fileName) {
		int dash = fileName.indexOf('-');
		if (dash <= 0) {
			return null;
		} else {
			return fileName.substring(0, dash - 1).trim();
		}
	}

	/**
	 * 删除字符串中，指定符号中间的内容（包括符号本身）
	 * 
	 * @param string
	 *            输入字符串
	 * @param left
	 *            左侧符号
	 * @param right
	 *            右侧符号
	 * @return 删除了内容的字符串
	 */
	protected String deleteSectionBetween(String string, char left, char right) {
		do {
			int begin = string.indexOf(left);
			int end = string.lastIndexOf(right);

			if (begin == -1 || end == -1) {
				break;
			} else {
				if (begin < end) {
					if (begin == 0) {
						if (end == string.length() - 1) {
							string = "";
						} else {
							string = string.substring(end + 1);
						}
					} else {
						if (end == string.length() - 1) {
							string = string.substring(0, begin);
						} else {
							String part1 = string.substring(0, begin);
							String part2 = string.substring(end + 1);
							string = part1 + part2;
						}
					}
				}
			}
		} while (true);
		return string;
	}
}
