package com.team33.qrcodepursuit.activities.Scoreboard;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Comment;
import com.team33.qrcodepursuit.models.GameQRCode;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment to view qr code info that already exists
 * for whatever reason, queries that work fine elsewhere break down here
 */
public class ViewQRFragment extends Fragment {

    private ImageView qrImage;
    private TextView scoreView;
    private TextView timesScannedView;
    private TextView creatorView;
    private RecyclerView commentView;
    private EditText commentEdit;
    private Button addCommentButton;

    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private String qrId;
    private DocumentSnapshot qrSnapshot;

    public ViewQRFragment() { super(R.layout.fragment_viewqr); }

    /**
     * on creation, display qr with appropriate information
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_recieveqr, container, false);
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        qrImage = view.findViewById(R.id.viewqr_imageview_qrimage);
        scoreView = view.findViewById(R.id.viewqr_textview_score);
        timesScannedView = view.findViewById(R.id.viewqr_textview_timesscanned);
        creatorView = view.findViewById(R.id.viewqr_textview_creator);
        commentView = view.findViewById(R.id.viewqr_recyclerview_comments);
        commentEdit = view.findViewById(R.id.viewqr_edittext_commenttext);
        addCommentButton = view.findViewById(R.id.viewqr_button_addcomment);

        // bundle should contain QR id in firebase
        // get qr info
        Bundle b = getArguments();
        qrId = b.getString("SCOREBOARDTOVIEW"); // change this key later

        // for whatever reason db says it contains nothing
//        DocumentReference docref = db.collection("GameQRs").document(qrId);
//        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    qrSnapshot = task.getResult();
//
//                } else {
//                    System.out.println("not found qr");
//                }
//            }
//        });

        CollectionReference check = db.collection("GameQRs");
        System.out.println(check);

        check.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    System.out.println(task.getResult().getDocuments());
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        System.out.println(doc.getId());
                        if (doc.getId() == qrId) {
                            qrSnapshot = doc;
                        }
                    }
                } else {
                    System.out.println(task.getException());
                }
            }
        });

        System.out.println(qrSnapshot);

        if (qrSnapshot.get("imageURL") != null) {
            // set qr image
            storage.getReferenceFromUrl((String) qrSnapshot.get("imageURL")).getBytes(1024 * 1024 * 10).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    if (task.isSuccessful()) {
                        byte[] bytes = task.getResult();
                        Bitmap bitm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        qrImage.setImageBitmap(bitm);
                    }
                }
            });
        }

        // set score view
        scoreView.setText((String) qrSnapshot.get("score"));

        // set times scanned view
        // timesScannedView.setText();

        // set creator view
        db.collection("Accounts").document((String) qrSnapshot.get("owner")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String username = (String) task.getResult().get("username");
                    creatorView.setText(username);
                }
            }
        });

        // set comment view
        // todo: verify that this works
        ArrayList<Comment> commentList = new ArrayList<Comment>();
        db.collection("Comments").whereEqualTo("parent", qrSnapshot.getId()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    commentList.addAll(value.toObjects(Comment.class));
                }
            }
        });
        CommentListAdapter commentListAdapter = new CommentListAdapter(commentList);
        commentView.setAdapter(commentListAdapter);

        // setup add comment button
        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Comment comment = new Comment(commentEdit.getText().toString(), qrSnapshot.getId());

                // add to db
                db.collection("Comments").add(comment);

                // add to recyclerview
                commentList.add(comment);
                commentListAdapter.notifyDataSetChanged();

            }
        });

        return view;
    }

}
