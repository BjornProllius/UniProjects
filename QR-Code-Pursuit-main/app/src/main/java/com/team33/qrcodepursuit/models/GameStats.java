package com.team33.qrcodepursuit.models;


// User issues
// View total number of QR codes scanned
// View sum of scores scanned
// View highest and lowest scoring QR codes
//
// Classes
// Scoreboard
// GameStats
//

// Game Stats responsibilities
// Summarize stats of a Game History
// Can be viewed by other players
// Stores ranking for account


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team33.qrcodepursuit.models.GameHistory;

public class GameStats extends GameHistory {
    private static final String TAG = "Test";
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    int totalScore = 0;
    int totalQRCodes = 0;
    String associatedUsername;
    int highestQRAttained;
    int lowestQRAttained;
    int currentRanking;


    //GameQRCode currentLowest;
    //GameQRCode currentHighest;

    /**
     * Generate a GameStats class that stores the statistics for a specific GameHistory
     * @param username - The associated username
     */
    public GameStats(String username){
        super();
        DocumentReference doc = database.collection("Accounts").document(username);

        doc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    Log.d(TAG,"username is" + document.get("username"));
                    associatedUsername = (String) document.get("username");
                    Log.d(TAG,"totalScore is" + document.get("totalScore"));
                    totalScore = (int) document.get("totalScore");
                    Log.d(TAG,"highestQR is" + document.get("highestQR"));
                    highestQRAttained = (int) document.get("highestQR");
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }

        });
    }


//    /**
//     * Set the total score for this user stats from the collected QR codes
//     */
//    public void setInitialScoreFromData(){
//        if (totalScore != 0)
//            //GameQRProcessor processor = new GameQRProcessor();
//            ;
//        List<String> codes = super.qrCodeHashes;
//        int points = 0;
//        for(int i = 0; i < codes.size(); i++){
//            //int value = processor.calculateScore(codes[i]);
//            points += value;
//        }
//        setTotalScore(points);
//    }

    /**
     * Add points to this associated user's total score
     * @param points - a number of points to add
     */
    public void addPoints(int points){
        this.totalScore += points;
    }

//    /**
//     * Add score to the total points for this associated user.
//     * @param code - A QR code object
//     */
//    public void addPoints(GameQRCode code){
//        GameQRProcessor processor = new GameQRProcessor();
//        this.totalPoints += processor.getScore(code);
//    }

    /**
     * Get the total score calculated for this associated user.
     * @return totalScore - the total score calculated
     */
    public int getTotalScore() {
        return totalScore;
    }

//    /**
//     * Get the number of QR codes collected from this associated user.
//     * @return  number of QR codes
//     */
    //public int getTotalQRCodes() {
        //return super.getCodeHistory().size();
    //}

//    /**
//     * Get the QR code of the lowest value collected from this associated user.
//     * @return lowest value QR code
//     */
//    public GameQRCode getLowestQRCode(){
//        return this.currentLowest;
//    }

//    /**
//     * Get the QR code of the lowest value from this associated user.
//     * @return highest value QR code
//     */
//    public GameQRCode getHighestQRCode(){
//        return this.currentHighest;
//    }

    /**
     * Set total QR code points total to a specific value.
     * Warning: This is likely to cause differences between the actual cumulated value and
     * the forced value.
     * @param totalScore - the score to set for this user
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;

    }

    /**
     * Set total number of QR codes collected to a specific integer.
     * @param totalQRCodes - the number of QR codes to set for this user
     */
    public void setTotalQRCodes(int totalQRCodes){
        this.totalQRCodes = totalQRCodes;
    }
    public int getLowestQRAttained(){
        return this.lowestQRAttained;
    }
    public int getHighestQRAttained(){
        return this.highestQRAttained;
    }
    public int getTotalQRCodes(){
        return this.totalQRCodes;
    }
}
