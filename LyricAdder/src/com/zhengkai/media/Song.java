package com.zhengkai.media;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

/**
 * 歌曲类，代表音乐文件，例如.mp3和.m4a文件等。包括抽象的歌曲文件本身，以及信息头（tag）
 * 
 * @author zhengkai
 * @date 2014年4月15日
 */
public class Song extends MusicObject {
	// 用于mp3文件
	private MP3File mp3File;
	// 用于mp4文件
	private AudioFile audioFile;
	// 歌曲的标签
	private Tag tag;

	// 当搜索不到歌词时，可能是由于歌名不标准，可以尝试修改歌名后再搜索
	private int modifyTitleMethod;

	// 歌曲的信息
	public String album;
	public int trackNo;
	public int discNo;
	public int year;
	public String genre;

	/**
	 * 默认构造函数
	 */
	public Song() {
		super();
	}

	/**
	 * 通过指定的歌曲文件位置构造歌曲对象
	 * 
	 * @param filePath
	 *        歌曲文件在硬盘中的位置
	 */
	public Song(String filePath) {
		super(filePath);
		try {
			if (extensionName.equals(".mp3")) {
				this.mp3File = new MP3File(this.filePath);
				this.tag = this.mp3File.getTag();
			} else if (extensionName.equals(".m4a")) {
				this.audioFile = AudioFileIO.read(new File(this.filePath));
				this.tag = this.audioFile.getTag();
			}

			if (this.tag == null) {
				if (extensionName.equals(".mp3")) {
					this.mp3File.setTag(new ID3v24Tag());
					this.mp3File.save();
					this.tag = this.mp3File.getTag();
				} else if (extensionName.equals(".m4a")) {
					this.tag = this.audioFile.createDefaultTag();
					this.audioFile.commit();
					this.tag = this.audioFile.getTag();
				}
			} else {
				this.title = getTitleFromTag().toLowerCase();
				this.artist = getArtistFromTag().toLowerCase();
			}
		} catch (IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException | CannotReadException | CannotWriteException e) {
		}

		modifyTitleMethod = 0;
	}

	/**
	 * 从标签中获取歌名
	 * 
	 * @return 歌名
	 */
	protected String getTitleFromTag() {
		if (this.tag != null && this.tag.hasField(FieldKey.TITLE)) {
			return this.tag.getFirst(FieldKey.TITLE);
		}
		return "";
	}

	/**
	 * 从标签中获取歌手名
	 * 
	 * @return 歌手名
	 */
	protected String getArtistFromTag() {
		if (this.tag != null && this.tag.hasField(FieldKey.ARTIST)) {
			return this.tag.getFirst(FieldKey.ARTIST);
		}
		return "";
	}

	/**
	 * 从歌曲文件中移除标签
	 */
	public void removeTag() {
		if (extensionName.equals(".mp3")) {
			try {
				AbstractID3v2Tag tag = this.mp3File.getID3v2Tag();
				mp3File.delete(tag);
				mp3File.save();
			} catch (IOException | TagException e) {
				e.printStackTrace();
			}
		} else if (extensionName.equals(".m4a")) {

		}
	}

	/**
	 * 输出标签中包含的信息
	 */
	public void outputTag() {
		if (this.tag != null) {
			// System.out.println(tag.toString());

			System.out.println(tag.getFirst(FieldKey.ARTIST));
			System.out.println(tag.getFirst(FieldKey.ALBUM));
			System.out.println(tag.getFirst(FieldKey.TITLE));
			System.out.println(tag.getFirst(FieldKey.COMMENT));
			System.out.println(tag.getFirst(FieldKey.YEAR));
			System.out.println(tag.getFirst(FieldKey.TRACK));
			System.out.println(tag.getFirst(FieldKey.DISC_NO));
			System.out.println(tag.getFirst(FieldKey.COMPOSER));
			System.out.println(tag.getFirst(FieldKey.ARTIST_SORT));
		}
	}

	/**
	 * 根据指定的歌词对象，为歌曲添加歌词
	 * 
	 * @param lyric
	 *        要添加的歌词对象
	 */
	public void setLyric(Lyric lyric) {
		try {
			String lyricString = lyric.getLyricString();
			this.tag.setField(FieldKey.LYRICS, lyricString);

			if (extensionName.equals(".mp3")) {
				this.mp3File.save();
			} else if (extensionName.equals(".m4a")) {
				this.audioFile.commit();
			}
		} catch (CannotWriteException | KeyNotFoundException | IOException | TagException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 输出歌词内容
	 */
	public void outputLyric() {
		if (this.tag != null && this.tag.hasField(FieldKey.LYRICS)) {
			System.out.println(this.tag.getFirst(FieldKey.LYRICS));
		}
	}

	/**
	 * 输出音乐文件头的内容
	 */
	public void outputInfo() {
		if (extensionName.equals(".mp3")) {
			MP3AudioHeader mp3AudioHeader = (MP3AudioHeader) this.mp3File.getAudioHeader();

			System.out.println(mp3AudioHeader.getTrackLength());
			System.out.println(mp3AudioHeader.getSampleRateAsNumber());
			System.out.println(mp3AudioHeader.getChannels());
			System.out.println(mp3AudioHeader.isVariableBitRate());
		} else if (extensionName.equals(".m4a")) {

		}
	}

	/**
	 * 根据标签中的信息重命名文件
	 */
	public void renameFileUsingTitleInTag() {
		if (this.title != null) {
			int slash = filePath.lastIndexOf('\\');
			String newFilePath = filePath.substring(0, slash + 1) + title + extensionName;

			File file = new File(this.filePath);
			File newFile = new File(newFilePath);

			file.renameTo(newFile);
		}
	}

	/**
	 * 尝试修改歌名
	 */
	public void tryModifyTitle() {
		if (modifyTitleMethod == 0) {
			// 第一次，不修改，保持原歌名

		} else if (modifyTitleMethod == 1) {
			this.title = deleteSectionBetween(this.title, '(', ')');

		} else if (modifyTitleMethod == 2) {
			this.title = this.title.replaceAll(",", "，");
		}

		modifyTitleMethod++;
	}

	/**
	 * 测试是否还有修改歌名的方法
	 * 
	 * @return 是否还有修改歌名的方法
	 */
	public boolean hasModifyTitleMethod() {
		return (modifyTitleMethod <= 2);
	}
}
