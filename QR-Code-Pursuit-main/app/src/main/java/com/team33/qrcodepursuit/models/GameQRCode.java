package com.team33.qrcodepursuit.models;

import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import net.glxn.qrgen.android.MatrixToImageWriter;

import java.util.ArrayList;

/**
 * class to store and handle QR data from zxing result
 */
public class GameQRCode implements Parcelable {

    private ArrayList<Integer> qrHash;
    private String imageURL;
    private String owner;
    private Location location;
    private int score;

    /**
     * using the "raw" qr information, create a visual bitmap
     * @return a bitmap representing the QR code visually
     */
    public static Bitmap getQRImage(String qrText) {
        QRCodeWriter writer = new QRCodeWriter(); // this should be in other class later on
        BitMatrix bitm = null;
        try {
            bitm = writer.encode(qrText, BarcodeFormat.QR_CODE, 300, 300);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return MatrixToImageWriter.toBitmap(bitm);
    }

    // for firestore
    public GameQRCode() {}

    /**
     * construct a GameQRCode using Result from qrscanner
     * additionally, calculate the score
     * @param code raw data from scanner result
     */
    public GameQRCode(Result code) {
        // does store original qr very briefly
        qrHash = new ArrayList<Integer>();
        for (byte value : code.getRawBytes()) {
            qrHash.add((int) value);
        }
        location = null;
        imageURL = null;
        owner = null;
        score = 0;

        /* calculate score
         * extra good because some data is tossed, original qr not stored
         * n^2 time but qr codes are never going to get large enough
         */
        for (int i = 1; i < qrHash.size(); i++) {
            qrHash.set(i, (qrHash.get(i) * qrHash.get(i)));
        }
        int streak = 1;
        for (int i = 1; i < qrHash.size(); i++) {
            if (qrHash.get(i) > qrHash.get(i-1)) {
                score += streak;
                streak += 1;
            } else { streak = 1; }
        }
    }

    /**
     * create a GameQRCode from its parceled form
     * @param in the parcel to unpack
     */
    protected GameQRCode(Parcel in) {
        imageURL = in.readString();
        owner = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        score = in.readInt();
        qrHash = (ArrayList<Integer>) in.readSerializable();
    }

    public static final Creator<GameQRCode> CREATOR = new Creator<GameQRCode>() {
        @Override
        public GameQRCode createFromParcel(Parcel in) {
            return new GameQRCode(in);
        }

        @Override
        public GameQRCode[] newArray(int size) {
            return new GameQRCode[size];
        }
    };

    // getters
    public ArrayList<Integer> getQrHash() { return qrHash; }
    public String getImageURL() { return imageURL; }
    public Location getLocation() { return location; }
    public int getScore() { return score; }
    public String getOwner() { return owner; }

    public void setLocation(Location loc) {
        location = loc;
    }

    public void setOwner(String own) {
        owner = own;
    }

    public void setImageURL(String str) { imageURL = str; }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * pack a GameQRCode into a parcel
     * @param parcel
     * @param i
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageURL);
        parcel.writeString(owner);
        parcel.writeParcelable(location, 0);
        parcel.writeInt(score);
        parcel.writeSerializable(qrHash);

    }
}
