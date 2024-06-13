package com.team33.qrcodepursuit.activities.Scoreboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Account;
import com.team33.qrcodepursuit.models.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * fragment to display scoreboards
 */
public class ScoreBoardFragment extends Fragment {

    private final String TAG = "ScoreBoardFragment";

    // Declare the variables so that you will be able to reference it later.
    Button sortByButton;
    Button regionButton;
    Button goToQRButton;
    ListView playerList;

    FirebaseFirestore db;

    ArrayAdapter<Account> playerAdapter;
    ArrayList<Account> playerDataList;

    NavController controller;

    //ScoreBoardList scoreBoardList;

    public ScoreBoardFragment() {
        // Required empty public constructor
    }

    // I MOVED A BUNCH OF STUFF INTO onCreateView
    // was crashing when switching sometimes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_score_board, container, false);


        playerDataList = new ArrayList<Account>();

        //setContentView(R.layout.activity_main);

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        // Create a reference to the accounts collection
        CollectionReference accounts = db.collection("Accounts");

        // Create a query against the collection.
        accounts.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Account acc = new Account(document.getId(), true);
                        playerDataList.add(acc);
                    }

                    Collections.sort(playerDataList, Comparator.comparing(Account::getTotalScore));
                    playerAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error while querying for accounts");
                }
            }
        });


        controller = Navigation.findNavController(container);

        playerList = rootView.findViewById(R.id.player_list);
        sortByButton = rootView.findViewById(R.id.sort_by_button);
        goToQRButton = rootView.findViewById(R.id.score_board_button_toqr);

        goToQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigate(R.id.action_bottomnavigation_menu_scoreboard_to_QRScoreBoardFragment);
            }
        });

        // display the scoreboard
        playerAdapter = new ScoreBoardList(this.getActivity(), playerDataList);
        playerList.setAdapter(playerAdapter);

        // display current user
        Account currentAccount = new Account(FirebaseAuth.getInstance().getCurrentUser().getUid(), true);

        TextView currentRanking = rootView.findViewById(R.id.current_ranking);
        TextView currentUsername = rootView.findViewById(R.id.current_username);
        TextView currentTotalScore = rootView.findViewById(R.id.current_totalscore);

        currentRanking.setText(String.valueOf(playerDataList.size()));
        currentUsername.setText("Me");
        currentTotalScore.setText("Total Score: " + currentAccount.getTotalScore());

        return rootView;
    }
}