package org.teckown.hello;

public class listViewItem {
    private String title;  //제목
    private String user;   //채널명
    private String time;
    private String thumnail; //썸네일 이미지
    private String url;   //영상링크
    private String platform;

    public listViewItem (String title, String user, String time, String url, String thumnail, String platform){
        this.title = title;
        this.user = user;
        this.time = time;
        this.url = url;
        this.thumnail = thumnail;
        this.platform = platform;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getThumnail() {
        return thumnail;
    }

    public void setThumnail(String thumnail) {
        this.thumnail = thumnail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
