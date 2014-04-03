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
		this.fileFullName = getFileFullName();
		this.fileName = getFileName();

		this.extensionName = getExtensionName(this.fileFullName);

		this.artist = getArtist();
		if (this.artist != null) {
			this.artist = this.artist.toLowerCase();
		}

		this.title = getTitle().toLowerCase();
		if (this.title != null) {
			this.title = this.title.toLowerCase();
		}
	}

	private String getFileFullName() {
		int slash = this.filePath.lastIndexOf('\\');
		return filePath.substring(slash + 1);
	}

	private String getFileName() {
		int dot = this.fileFullName.lastIndexOf('.');
		return fileFullName.substring(0, dot);
	}

	private String getExtensionName(String fileFullName) {
		int dot = fileFullName.lastIndexOf('.');
		return fileFullName.substring(dot);
	}

	protected String getArtist() {
		int dash = this.fileName.indexOf('-');
		if (dash == -1) {
			return null;
		} else {
			return this.fileName.substring(0, dash - 1).trim();
		}
	}

	protected String getTitle() {
		int dash = this.fileName.indexOf('-');
		if (dash == -1) {
			return this.fileName;
		} else {
			return this.fileName.substring(dash + 1, this.fileName.length()).trim();
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
