package com.obb.backup.restore.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String id;
    private String name;
    private String version;
    private String size;
    private String developer;
    private String status;
    private String imageUrl;
    private String description;
    private String dllink;
    private int timer;

    private String icon_url;

    public Product(String id, String name, String version, String size, String developer, String status, String imageUrl, String description, String dllink, int timer,String icon_url) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.size = size;
        this.developer = developer;
        this.status = status;
        this.imageUrl = imageUrl;
        this.description = description;
        this.dllink = dllink;
        this.timer = timer;
        this.icon_url = icon_url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getSize() {
        return size;
    }

    public String getDeveloper() {
        return developer;
    }

    public String getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public String getDownloadUrl() {
        return dllink;
    }
    public String geticon_url() {
        return icon_url;
    }
    public String getDescription() {
        return description;
    }
    public int getTimer() {
        return timer;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(version);
        dest.writeString(size);
        dest.writeString(developer);
        dest.writeString(status);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeString(dllink);
        dest.writeInt(timer);
        dest.writeString(icon_url);
    }

    protected Product(Parcel in) {
        id = in.readString();
        name = in.readString();
        version = in.readString();
        size = in.readString();
        developer = in.readString();
        status = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        dllink = in.readString();
        timer = in.readInt();
        icon_url = in.readString();
    }

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
