package com.team33.qrcodepursuit.models;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * app-wide singleton keeps local copies of Accounts, GameQRs, and maintains scoreboard
 * handles getting HiScoreQR, LowScoreQR, TotalScore, and rankings for Accounts
 */
public class Scoring {
    private final static String TAG = "Scoring";
    public enum SortBy { TOTALSCANS, HISCORE, TOTALSCORE }
    private boolean needs_refresh = false;

    FirebaseFirestore db;
    CollectionReference accounts_db;
    CollectionReference gameqrs_db;
    HashMap<String, Account> accounts;
    HashMap<String, Integer> qrscores;
    TreeSet<String> sortedbyscans
            = new TreeSet<>(Comparator.comparingInt(this::getTotalScore).reversed());
    TreeSet<String> sortedbyhiscore
            = new TreeSet<>(Comparator.comparingInt(e -> {
                Map.Entry<String, Integer> hs = getHiScoreQR((String) e);
                return (hs != null) ? hs.getValue() : 0;
            }).reversed());
    TreeSet<String> sortedbytotalscore
            = new TreeSet<>(Comparator.comparingInt(this::getTotalScore).reversed());


    private static final Scoring singleton = new Scoring();
    public static Scoring getInstance() { return singleton; }
    private Scoring() {
        db = FirebaseFirestore.getInstance();
        accounts = new HashMap<>();
        qrscores = new HashMap<>();
        accounts_db = db.collection("Accounts");
        gameqrs_db = db.collection("GameQRs");
        accounts_db.get().addOnSuccessListener(result -> result.getDocuments().forEach(doc ->
                accounts.put(doc.getId(), doc.toObject(Account.class))));
        gameqrs_db.get().addOnSuccessListener(result -> result.getDocuments().forEach(doc ->
                qrscores.put(doc.getId(), doc.get("score", Integer.class))));
        accounts_db.addSnapshotListener((value, error) -> {
            List<DocumentChange> changelist =
                    (value != null) ? value.getDocumentChanges() : Collections.emptyList();
            changelist.forEach(change -> {
                QueryDocumentSnapshot doc = change.getDocument();
                String id = doc.getId();
                Account acc = doc.toObject(Account.class);
                needs_refresh = true;
                switch (change.getType()) {
                    case ADDED: case MODIFIED: accounts.put(id, acc);
                    case REMOVED: accounts.remove(id);
                }
            });
        });
        gameqrs_db.addSnapshotListener((value, error) -> {
            List<DocumentChange> changelist =
                    (value != null) ? value.getDocumentChanges() : Collections.emptyList();
            changelist.forEach(change -> {
                QueryDocumentSnapshot doc = change.getDocument();
                String id = doc.getId();
                Integer score = doc.get("score", Integer.class);
                needs_refresh = true;
                switch (change.getType()) {
                    case ADDED: case MODIFIED: qrscores.put(id, score);
                    case REMOVED: qrscores.remove(id);
                }
            });
        });

    }

    /**
     * refresh scoreboards
     * @param s sort by TOTALSCANS / HISCORE / TOTALSCORE
     */
    private void refreshscoreboard(SortBy s) {
        if (!needs_refresh) return;
        Set<String> ids = accounts.keySet();
        switch (s) {
            case TOTALSCANS:
                if (sortedbyscans.retainAll(ids)
                        || sortedbyscans.addAll(ids))
                    Log.d(TAG, "refreshed totalscans scoreboard");
            case HISCORE:
                if (sortedbyhiscore.retainAll(accounts.keySet())
                        || sortedbyscans.addAll(ids))
                    Log.d(TAG, "refreshed totalscore scoreboard");
            case TOTALSCORE:
                if (sortedbytotalscore.retainAll(accounts.keySet())
                    || sortedbyscans.addAll(ids))
                    Log.d(TAG, "refreshed hiscore scoreboard");
        }
    }

    /**
     * get total number of players
     */
    public int getTotalPlayers()
        { return accounts.size(); }

    /**
     * get id and score of highest scoring QR code for one Account
     * @return null if uid does not exist or if account doesn't have any scanned QRs
     */
    @Nullable
    public Map.Entry<String, Integer> getHiScoreQR(String uid) {
        Account a = accounts.get(uid);
        if (a == null) return null;
        ArrayList<String> scanned = a.getScannedQRs();
        if (scanned.isEmpty()) return null;
        return qrscores.entrySet().stream()
                .filter(e -> scanned.contains(e.getKey()))
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);
    }

    /**
     * get id and score of lowest scoring QR code for one Account
     * @return null if uid does not exist or if account doesn't have any scanned QRs
     */
    @Nullable
    public Map.Entry<String, Integer> getLowScoreQR(String uid) {
        Account a = accounts.get(uid);
        if (a == null) return null;
        ArrayList<String> scanned = a.getScannedQRs();
        if (scanned.isEmpty()) return null;
        return qrscores.entrySet().stream()
                .filter(e -> scanned.contains(e.getKey()))
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);
    }

    /**
     * get sum of scanned QR scores for one Account
     * @return -1 if uid does not exist, 0 if account doesn't have any scanned QRs
     */
    public int getTotalScore(String uid) {
        Account a = accounts.get(uid);
        if (a == null) return -1;
        ArrayList<String> scanned = a.getScannedQRs();
        if (scanned.isEmpty()) return 0;
        return qrscores.entrySet().stream()
                .filter(e -> scanned.contains(e.getKey()))
                .mapToInt(Map.Entry::getValue).sum();
    }

    /**
     * get ranking for a single player out of all players
     * @return -1 if uid does not exist in Scoring local
     */
    public int getRank(String uid, SortBy sort) {
        if (!accounts.containsKey(uid)) return -1;
        refreshscoreboard(sort);
        int rank = -1;
        switch (sort) {
            case TOTALSCANS:
                rank = sortedbyscans.headSet(uid, true).size();
            case HISCORE:
                rank = sortedbyhiscore.headSet(uid, true).size();
            case TOTALSCORE:
                rank = sortedbytotalscore.headSet(uid, true).size();
        }
        return rank + 1;
    }

    /**
     * get top ranking N accounts based on sort
     * @param top number of top ranking accounts to return
     * @return ArrayList of top account uids
     */
    public ArrayList<String> getTopPlayers(int top, SortBy sort) {
        if (top < 1) return new ArrayList<>();
        refreshscoreboard(sort);
        ArrayList<String> s;
        switch (sort) {
            case TOTALSCANS:
               s = new ArrayList<>(sortedbyscans);
            case HISCORE:
                s = new ArrayList<>(sortedbyhiscore);
            case TOTALSCORE:
                s = new ArrayList<>(sortedbytotalscore);
                break;
            default:
                s = new ArrayList<>();
        }
        return (ArrayList<String>) s.subList(0, top-1);
    }


}
