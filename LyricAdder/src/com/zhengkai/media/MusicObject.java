package com.zhengkai.media;

public class MusicObject {
	protected String filePath;
	
	public String fileFullName;
	public String fileName;
	public String extensionName;

	public String artist;
	public String title;

	public MusicObject() {
	}

	public MusicObject(String filePath) {
		this.filePath = filePath;
		this.fileFullName = getFileFullName(this.filePath);
		this.fileName = getFileName(this.fileFullName);

		this.extensionName = getExtensionName(this.fileFullName);

		this.artist = getArtistNameFromFile(this.fileName);
		this.title = getSongNameFromFile(this.fileName);
	}

	private String getFileFullName(String filePath) {
		int slash = filePath.lastIndexOf('\\');
		return filePath.substring(slash + 1);
	}

	private String getFileName(String fileFullName) {
		int dot = fileFullName.lastIndexOf('.');
		return fileFullName.substring(0, dot);
	}

	private String getExtensionName(String fileFullName) {
		int dot = fileFullName.lastIndexOf('.');
		return fileFullName.substring(dot);
	}

	private String getArtistNameFromFile(String fileName) {
		int dash = fileName.indexOf('-');
		if (dash == -1) {
			return null;
		} else {
			return fileName.substring(0, dash - 1).trim();
		}
	}

	private String getSongNameFromFile(String fileName) {
		int dash = fileName.indexOf('-');
		if (dash == -1) {
			return fileName;
		} else {
			return fileName.substring(dash + 1, fileName.length()).trim();
		}
	}

	protected String deleteSectionBetween(String line, char left, char right) {
		do {
			int begin = line.indexOf(left);
			int end = line.indexOf(right);

			if (begin == -1 || end == -1) {
				break;
			} else {
				if (begin < end) {
					if (begin == 0) {
						if (end == line.length() - 1) {
							line = "";
						} else {
							line = line.substring(end + 1);
						}
					} else {
						if (end == line.length() - 1) {
							line = line.substring(0, begin);
						} else {
							String part1 = line.substring(0, begin);
							String part2 = line.substring(end + 1);
							line = part1 + part2;
						}
					}
				}
			}
		} while (true);
		return line;
	}
}
