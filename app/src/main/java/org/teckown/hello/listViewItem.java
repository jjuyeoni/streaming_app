package org.teckown.hello;

public class listViewItem {
    private String title;
    private String user;
    private String time;
    private String thumnail; //썸네일 이미지
    private String url;   //영상링크

    public listViewItem (String title, String user, String time, String url, String thumnail){
        this.title = title;
        this.user = user;
        this.time = time;
        this.url = url;
        this.thumnail = thumnail;
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

    public void setName(String user) {
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
}
