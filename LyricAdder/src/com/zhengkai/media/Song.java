package com.zhengkai.media;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

public class Song extends MusicObject {
	private MP3File mp3File;
	private Tag tag;
	public boolean foundLyric;
	private int modifyTitleMethod;
	
	public String album;
	public int trackNo;
	public int discNo;
	public int year;
	public String gene;
	

	public Song() {
		super();
		foundLyric = false;
	}

	public Song(String filePath) {
		super(filePath);
		foundLyric = false;
		modifyTitleMethod = 0;
		try {
			this.mp3File = new MP3File(this.filePath);
			this.tag = mp3File.getTag();

			this.artist = getArtistNameFromTag(this.tag);
			this.title = getSongTitleFromTag(this.tag);

		} catch (IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			e.printStackTrace();
		}
	}

	private String getArtistNameFromTag(Tag tag) {
		return tag.getFirst(FieldKey.ARTIST);
	}

	private String getSongTitleFromTag(Tag tag) {
		return tag.getFirst(FieldKey.TITLE);
	}
	
	public void removeTag() {
		try {
			AbstractID3v2Tag tag = this.mp3File.getID3v2Tag();
			mp3File.delete(tag);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void outputTag() {
		Tag tag = this.mp3File.getTag();

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

	public void setLyric(Lyric lyric) {
		try {
			String lyricString = lyric.getLyricString();
			// System.out.println(lyricString);
			this.tag.setField(FieldKey.LYRICS, lyricString);
			this.mp3File.save();

			// this.outputLyric();
		} catch (KeyNotFoundException | IOException | TagException e) {
			e.printStackTrace();
		}
	}

	public void outputLyric() {
		System.out.println(this.tag.getFirst(FieldKey.LYRICS));
	}

	public void outputInfo() {
		MP3AudioHeader mp3AudioHeader = (MP3AudioHeader) this.mp3File
				.getAudioHeader();

		System.out.println(mp3AudioHeader.getTrackLength());
		System.out.println(mp3AudioHeader.getSampleRateAsNumber());
		System.out.println(mp3AudioHeader.getChannels());
		System.out.println(mp3AudioHeader.isVariableBitRate());
	}

	public void renameFileUsingTitleInTag() {
		String title = this.tag.getFirst(FieldKey.TITLE);

		int slash = filePath.lastIndexOf('\\');
		String newFilePath = filePath.substring(0, slash + 1) + title
				+ extensionName;

		File file = new File(this.filePath);
		File newFile = new File(newFilePath);

		file.renameTo(newFile);
	}

	public void tryModifyTitle() {
		modifyTitleMethod++;

		if (modifyTitleMethod == 1) {
			this.title = deleteSectionBetween(this.title, '(', ')');

		} else if (modifyTitleMethod == 2) {
			this.title = this.title.replaceAll(",", "，");
		}
	}

	public boolean hasModifyTitleMethod() {
		return (modifyTitleMethod < 2);
	}
}