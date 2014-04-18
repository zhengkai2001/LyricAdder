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
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.mp4.Mp4Tag;

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
	public String albumArtist;
	public String year;
	public String genre;

	public boolean hasAlbum;
	public boolean hasAlbumArtist;
	public boolean hasYear;
	public boolean hasGenre;

	public int trackNo;
	public int discNo;

	public Song() {
		super();
	}

	/**
	 * 通过指定的歌曲文件位置构造歌曲对象
	 * 
	 * @param filePath
	 *        歌曲文件的位置
	 */
	public Song(String filePath) {
		super(filePath);
		try {
			// 为mp3文件和m4a文件分别构造音频文件对象
			if (extensionName.equals(".mp3")) {
				this.mp3File = (MP3File) AudioFileIO.read(this.file);

				// 尝试从 id3 v1 标签中读取信息
				if (this.mp3File.hasID3v1Tag()) {
					ID3v1Tag v1Tag = mp3File.getID3v1Tag();

					if (v1Tag.hasField(FieldKey.TITLE)) {
						this.hasTitle = true;
						this.title = v1Tag.getFirst(FieldKey.TITLE);
					}

					if (v1Tag.hasField(FieldKey.ARTIST)) {
						this.hasArtist = true;
						this.artist = v1Tag.getFirst(FieldKey.ARTIST);
					}

					if (v1Tag.hasField(FieldKey.ALBUM)) {
						this.hasAlbum = true;
						this.album = v1Tag.getFirst(FieldKey.ALBUM);
					} else {
						this.hasAlbum = false;
					}

					if (v1Tag.hasField(FieldKey.YEAR)) {
						this.hasYear = true;
						this.year = v1Tag.getFirst(FieldKey.YEAR);
					} else {
						this.hasYear = false;
					}

					if (v1Tag.hasField(FieldKey.GENRE)) {
						this.hasGenre = true;
						this.genre = v1Tag.getFirst(FieldKey.GENRE);
					} else {
						this.hasGenre = false;
					}

					if (v1Tag.hasField(FieldKey.COMMENT)) {
						v1Tag.setField(FieldKey.COMMENT, "");
					}
				}

				ID3v24Tag v24Tag;
				// 尝试从 id3 v2 标签中读取信息
				if (this.mp3File.hasID3v2Tag()) {
					v24Tag = mp3File.getID3v2TagAsv24();
				} else {
					v24Tag = new ID3v24Tag();
				}

				if (isEmpty(v24Tag, FieldKey.TITLE) && this.hasTitle) {
					v24Tag.setField(FieldKey.TITLE, this.title);
				}

				if (isEmpty(v24Tag, FieldKey.ARTIST) && this.hasArtist) {
					v24Tag.setField(FieldKey.ARTIST, this.artist);
				}

				if (isEmpty(v24Tag, FieldKey.ALBUM) && this.hasAlbum) {
					v24Tag.setField(FieldKey.ALBUM, this.album);
				}

				if (isEmpty(v24Tag, FieldKey.YEAR) && this.hasYear) {
					v24Tag.setField(FieldKey.YEAR, this.year);
				}

				if (isEmpty(v24Tag, FieldKey.GENRE) && this.hasGenre) {
					v24Tag.setField(FieldKey.GENRE, this.genre);
				}

				this.mp3File.setID3v2Tag(v24Tag);
				this.mp3File.save();

				this.tag = this.mp3File.getID3v2TagAsv24();

			} else if (extensionName.equals(".m4a")) {
				this.audioFile = AudioFileIO.read(this.file);
				this.tag = this.audioFile.getTag();

				if (this.tag == null) {
					System.out.println("null~");
					this.tag = new Mp4Tag();
				}
				if (isEmpty(this.tag, FieldKey.TITLE) && this.hasTitle) {
					this.tag.setField(FieldKey.TITLE, this.title);
				}

				if (isEmpty(this.tag, FieldKey.ARTIST) && this.hasArtist) {
					this.tag.setField(FieldKey.ARTIST, this.artist);
				}

				this.audioFile.setTag(this.tag);
				this.audioFile.commit();

				this.tag = this.audioFile.getTag();
			}

			getTitleFromTag();
			getArtistFromTag();
		} catch (IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException | CannotReadException | CannotWriteException e) {
			e.printStackTrace();
		}

		modifyTitleMethod = 0;
	}

	/**
	 * 判断指定的tag中，指定的字段key是否为空，即不存在或者全部为空格
	 * 
	 * @param tag
	 * @param key
	 * @return
	 */
	private boolean isEmpty(Tag tag, FieldKey key) {
		if (!tag.hasField(key)) {
			return true;
		} else if (tag.getFirst(key).trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 从标签中获取歌名
	 */
	protected void getTitleFromTag() {
		if (this.tag != null && this.tag.hasField(FieldKey.TITLE)) {
			this.title = this.tag.getFirst(FieldKey.TITLE);
			this.titleLowerCase = this.title.toLowerCase();
		}
	}

	/**
	 * 从标签中获取歌手名
	 */
	protected void getArtistFromTag() {
		if (this.tag != null && this.tag.hasField(FieldKey.ARTIST)) {
			this.artist = this.tag.getFirst(FieldKey.ARTIST);
			this.artistLowerCase = this.artist.toLowerCase();
		}
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
		} catch (KeyNotFoundException | IOException | TagException | CannotWriteException e) {
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

			File newFile = new File(newFilePath);

			this.file.renameTo(newFile);
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
