package com.team33.qrcodepursuit.models;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


/**
 * A user's game history.
 * This contains all the QR codes that they have scanned, plus statistics associated with the codes.
 * A local copy is stored in the application.
 * A remote origin is stored on a Firebase database.
 */
public class GameHistory {
    List<String> qrCodeHashes;
    //List<GameQRCode> codeHistory;

    GameStats stats;
    String associatedUsername;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public GameHistory(){
        //
    }
    /**
     * Create a GameHistory with the associated username attached.
     * @param username
     */
    public GameHistory(String username){
        stats = new GameStats(username);

    }

//    /**
//     * Force sync of local contents with remote copy.
//     */
//    private void forceSyncWithServer(){
//        DocumentReference docRef = db.collection("Accounts").document(associatedUsername);
//        List<String> qrCodeNames = new List<String>();
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                DocumentSnapshot document = task.getResult();
//                Log.d("Confirm", "Data is " + document.getData());
//                qrCodeNames = (List<String>) document.getData();
//            }
//        });
//
//        for(int i = 0; i < qrCodeNames.size(); i++)
//        {
//            docRef = db.collection("GameQRs").document(qrCodeNames.get(i));
//
//            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    DocumentSnapshot document = task.getResult();
//                    Log.d("Confirm", "Game QR code hash is" + document.getString("hash"));
//                    qrCodeHashes.add(document.getString("hash"));
//                }
//            });
//        }
//
//        //some other method to make the necessary QR classes and to replace the codeHistory contents
//    }

//    /**
//     * Add a QR code to the QR codes collected, both remotely and locally.
//     * @param gameQRCode
//     */
//    public void addCode(GameQRCode gameQRCode){
//        codeHistory.add(gameQRCode);
//    }

    /**
     * Get the GameStats class associated with this GameHistory
     * @return GameStats - the class that can allow modification and access of stats related to the codes.
     */
    public GameStats getStats(){
        return this.stats;
    }

    /**
     * Set the username of which this game history is associated to.
     * @param associatedUsername - the username of the user
     */
    public void setAssociatedUser(String associatedUsername){
        this.associatedUsername = associatedUsername;
    }

    /**
     * Get the associated username of this game history
     * @return associatedUsername - a string of the associated username
     */
    public String getAssociatedUser(){
        return associatedUsername;
    }

//    /**
//     * Get the array of QR codes collected by this user.
//     * @return codeHistory - an ArrayList of QR code data
//     */
//    public ArrayList<GameQRCode> getCodeHistory(){
//        return this.codeHistory;
//    }
}
