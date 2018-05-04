package com.example.administrator.musicplayer_materialdesign.db;

import org.litepal.crud.DataSupport;

public class Music extends DataSupport{
    private String title;
    private String album;
    private String artist;
    private String genre;
    private String sourceUrl;
    private String imageUrl;
    private int trackNumber;
    private int totalTrackCount;
    private int duration;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public int getTotalTrackCount() {
        return totalTrackCount;
    }

    public void setTotalTrackCount(int totalTrackCount) {
        this.totalTrackCount = totalTrackCount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Music{" +
                " title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", trackNumber=" + trackNumber +
                ", totalTrackCount=" + totalTrackCount +
                ", duration=" + duration +
                '}';
    }
}
