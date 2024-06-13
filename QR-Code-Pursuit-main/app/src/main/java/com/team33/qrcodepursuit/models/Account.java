package com.team33.qrcodepursuit.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class Account implements Parcelable {
    private static final String TAG = "Account";
    private static final String UID_UNSET = "unset";

    // DB fields
    protected String username;
    protected String contactinfo;
    protected String bio;
    protected ArrayList<String> OwnedQRs;
    protected ArrayList<String> ScannedQRs;
    // non-DB fields
    @Exclude protected String uid;
    @Exclude protected FirebaseFirestore db;
    @Exclude protected DocumentReference dbAccount;

    /**
     * default constructor, initialize all super fields with empty values
     * i.e. username, contactinfo, bio = ""
     * OwnedQRs, ScannedQRs = []
     */
    public Account() {
        uid = UID_UNSET;
        db = FirebaseFirestore.getInstance();
        username = "";
        contactinfo = "";
        bio = "";
        OwnedQRs = new ArrayList<>();
        ScannedQRs = new ArrayList<>();
    }
    /**
     * main constructor
     * construct instance and create ref to Firestore document
     * optionally perform sync from Firestore
     * @param uid FirebaseAuth uid (= document id in Firestore/Accounts)
     * @param sync_now update this object with Firestore data
     */
    public Account(String uid, boolean sync_now) {
        this();
        this.setUid(uid);
        if (sync_now) fromDB();
    }

    // basic getters
    public String getUsername() { return username; }
    public String getContactinfo() { return contactinfo; }
    public String getBio() { return bio; }
    public ArrayList<String> getOwnedQRs() { return OwnedQRs; }
    public ArrayList<String> getScannedQRs() { return ScannedQRs; }
    // basic setters
    public void setUsername(String username) { this.username = username; }
    public void setContactinfo(String contactinfo) { this.contactinfo = contactinfo; }
    public void setBio(String bio) { this.bio = bio; }
    public void setOwnedQRs(ArrayList<String> ownedQRs) { OwnedQRs = ownedQRs; }
    public void setScannedQRs(ArrayList<String> scannedQRs) { ScannedQRs = scannedQRs; }

    /**
     * sync DB to local
     * @return task document.set(Account)
     */
    @Exclude
    public Task<Void> toDB() {
        return dbAccount.set(this);
    }
    /**
     * sync local to DB
     * MUST ONLY BE CALLED AFTER SETTING UID
     */
    @Exclude
    public void fromDB() {
        if (uid.equals(UID_UNSET)) {
            Log.w(TAG, "uid not provided, not synced from DB");
            return;
        }
        CountDownLatch done = new CountDownLatch(1);
        dbAccount.get().addOnCompleteListener(
            Executors.newSingleThreadExecutor(),
            task -> {
                if (task.isSuccessful()) {
                    Account from = task.getResult().toObject(Account.class);
                    if (from == null) return;
                    // i feel like there should be a less stupid way to do this but whatever
                    // this = from;
                    this.setUsername(from.username);
                    this.setBio(from.bio);
                    this.setContactinfo(from.contactinfo);
                    this.setOwnedQRs(from.OwnedQRs);
                    this.setScannedQRs(from.ScannedQRs);
                    Log.d(TAG, "retrieved Account from DB");
                } else {
                    Exception e = task.getException();
                    Log.w(TAG, "failed to get Account from DB", e);
                }
                done.countDown();
        });
        try { done.await(); }
        catch (InterruptedException ignored) { }
    }

    /**
     * get uid associated with Account
     * @return uid of FirebaseAuth user
     */
    @Exclude
    public String getUid() { return uid; }
    /**
     * set uid associated with this Account
     * @param uid uid of FirebaseAuth user
     */
    @Exclude
    public void setUid(String uid) {
        if (!this.uid.equals(UID_UNSET)) {
            Log.d(TAG, "set uid");
        }
        this.uid = uid;
        dbAccount = db.collection("Accounts").document(uid);
    }

    // WARNING: THIS WILL BE DEPRECATED IN FAVOR OF Scoring.getHiScore(uid)
    /**
     * get highest score out of scanned QRs
     * @return highest score out of scanned QRs
     */
    @Exclude
    public int getHiScore() {
        final int[] max = {0};
        if (this.ScannedQRs.size() > 0) {
            db.collection("GameQRs")
                    .whereIn(FieldPath.documentId(), this.ScannedQRs)
                    .orderBy("score", Query.Direction.DESCENDING).limit(1)
                    .get().addOnSuccessListener(result -> {
                Integer res = result.getDocuments().get(0).get("score", Integer.class);
                max[0] = (res != null) ? res : 0;
            });
        }
        return max[0];
    }

    // WARNING: THIS WILL BE DEPRECATED IN FAVOR OF Scoring.getTotalScore(uid)
    /**
     * get total sum of scores of Scanned QRs
     * @return sum of scanned scores
     */
    @Exclude
    public int getTotalScore() {
        final int[] sum = {0};
        if (this.ScannedQRs.size() > 0) {
            db.collection("GameQRs")
                    .whereIn(FieldPath.documentId(), this.ScannedQRs)
                    .get().addOnSuccessListener(result -> {
                for (QueryDocumentSnapshot doc : result) {
                    Number score = (Number) doc.get("score");
                    sum[0] += (score != null) ? score.intValue() : 0;
                }
            });
        }
        return sum[0];
    }

    /**
     * get total number of scanned QRcodes
     * @return this.ScannedQRs.size()
     */
    @Exclude
    public int getScannedQRsCount() {
        return this.ScannedQRs.size();
    }

    /**
     * get total number of owned QRcodes
     * @return this.OwnedQRs.size()
     */
    @Exclude
    public int getOwnedQRsCount() {
        return this.OwnedQRs.size();
    }

    /**
     * compares based on uid
     * true if obj isinstanceof Account, and obj.uid == this.uid
     */
    @Exclude
    @Override
    public boolean equals(@Nullable Object obj)
        { return obj instanceof Account && ((Account) obj).uid.equals(this.uid); }

    // --- Parcelable implementation below --- //

    @Exclude
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(username);
        out.writeString(contactinfo);
        out.writeString(bio);
        out.writeStringList(ScannedQRs);
        out.writeStringList(OwnedQRs);
        out.writeString(uid);
    }

    protected Account(Parcel in) {
        this();
        username = in.readString();
        contactinfo = in.readString();
        bio = in.readString();
        in.readStringList(ScannedQRs);
        in.readStringList(OwnedQRs);
        uid = in.readString();
        this.setUid(uid);
    }

    @Exclude
    public static final Parcelable.Creator<Account> CREATOR
        = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel parcel) {
            return new Account(parcel);
        }

        @Override
        public Account[] newArray(int i) {
            return new Account[0];
        }
    };

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

}
