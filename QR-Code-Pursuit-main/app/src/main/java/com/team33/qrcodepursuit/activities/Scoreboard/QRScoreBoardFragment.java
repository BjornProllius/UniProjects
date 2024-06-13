package com.team33.qrcodepursuit.activities.Scoreboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.team33.qrcodepursuit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * display qr codes to user
 */
public class QRScoreBoardFragment extends Fragment {

    private TextView sortMethodView;
    private ListView qrListView;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private NavController controller;

    public QRScoreBoardFragment() {
        super(R.layout.fragment_qrscoreboard);
    }

    /**
     * on creation, fetch and display qrs
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrscoreboard, container, false);
        db = FirebaseFirestore.getInstance();
        controller = Navigation.findNavController(container);
        sortMethodView = view.findViewById(R.id.qrscoreboard_textview_sortmethod);
        qrListView = view.findViewById(R.id.qrscoreboard_listview_qrs);


        // later on, take sort method
        // can sort by score, or by clicked map location
        Bundle b = getArguments();
        ArrayList<String> qrIds = new ArrayList<String>();
        QRListAdapter qrListAdapter = new QRListAdapter(qrIds);
        qrListView.setAdapter(qrListAdapter);

        db.collection("GameQRs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        if (!qrIds.contains(doc.getId())) {
                            qrIds.add(doc.getId());
                            qrListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        qrListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle args = new Bundle();
                System.out.println(qrIds.get(i));
                args.putString("SCOREBOARDTOVIEW", qrIds.get(i));
                controller.navigate(R.id.action_QRScoreBoardFragment_to_viewQRFragment, args);
            }
        });

        return view;
    }

}
