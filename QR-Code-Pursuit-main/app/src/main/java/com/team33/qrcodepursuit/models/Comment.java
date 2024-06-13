package com.team33.qrcodepursuit.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * class to store and handle comment data
 */
public class Comment implements Parcelable {

    private String author;
    private String content;
    private String parent;
    private Timestamp time;

    // for firestore
    public Comment() {}

    /**
     * create a GameQRCode from its parceled form
     * @param in the parcel to unpack
     */
    protected Comment(Parcel in) {
        author = in.readString();
        content = in.readString();
        parent = in.readString();
        time = in.readParcelable(Timestamp.class.getClassLoader());
    }

    /**
     * construct a new comment
     * @param text the comment text
     * @param qr the qr id
     */
    public Comment(String text, String qr) {
        author = FirebaseAuth.getInstance().getCurrentUser().getUid();
        content = text;
        parent = qr;
        time = Timestamp.now();
    }

    // getters
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public String getParent() { return parent; }
    public Timestamp getTime() { return time; }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(author);
        parcel.writeString(content);
        parcel.writeString(parent);
        parcel.writeParcelable(time, 0);
    }
}
