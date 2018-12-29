package org.teckown.hello;

public class channelItem {
    String id;
    String name;
    String img;
    String link;
    String platform_type;

    public channelItem(String id, String name, String img, String link, String platform_type) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.link = link;
        this.platform_type = platform_type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPlatform_type() {
        return platform_type;
    }

    public void setPlatform_type(String platform_type) {
        this.platform_type = platform_type;
    }
}
