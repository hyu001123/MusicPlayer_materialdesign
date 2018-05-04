package com.example.administrator.musicplayer_materialdesign.Modle;

public class InfoMusic {
    private String title;
    private String subTitle;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    @Override
    public String toString() {
        return "InfoMusic{" +
                "title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                '}';
    }
}
