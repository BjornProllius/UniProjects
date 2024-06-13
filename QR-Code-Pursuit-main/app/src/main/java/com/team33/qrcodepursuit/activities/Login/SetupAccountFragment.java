package com.team33.qrcodepursuit.activities.Login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Account;

/**
 * create Account in FirestoreDB
 * after signing up with FirebaseAuth in SignUpFragment
 */
public class SetupAccountFragment extends Fragment {

    private final String TAG = "SetupAccountFragment";

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String uid;
    CollectionReference db;
    // ui elements
    Button skip_button;
    TextInputEditText username_field;
    TextInputLayout username_til;
    TextInputEditText contactinfo_field;
    TextInputLayout contactinfo_til;
    EditText bio_field;
    Button done_button;
    NavController navctrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        { return inflater.inflate(R.layout.fragment_signup_setupaccount, container, false); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        uid = mUser.getUid();
        db = FirebaseFirestore.getInstance().collection("Accounts");
        // link ui
        skip_button = view.findViewById(R.id.setupacc_skip);
        username_til = view.findViewById(R.id.setupacc_username_TextInput);
        username_field = view.findViewById(R.id.setupacc_username_field);
        contactinfo_til = view.findViewById(R.id.setupacc_contactinf_TextInput);
        contactinfo_field = view.findViewById(R.id.setupacc_contactinfo_field);
        bio_field = view.findViewById(R.id.setupacc_bio_field);
        done_button = view.findViewById(R.id.setupacc_done);
        navctrl = Navigation.findNavController(view);

        // set callbacks
        // skip profile creation, init with empty
        skip_button.setOnClickListener(v ->
            db.document(uid)
                .set(new Account())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "created empty Account");
                        navctrl.navigate(R.id.action_setupAccountFragment_to_mainActivity);
                    }
                    else {
                        Exception e = task.getException();
                        Log.w(TAG, "failed to make empty Account", e);
                        Toast.makeText(getContext(),
                                "Firestore: could not set up empty Account\n"
                                + e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }));
        // create new Account and add to DB
        done_button.setOnClickListener(v -> {
            Account acc = new Account();
            acc.setUid(uid);
            acc.setUsername(username_field.getText().toString());
            acc.setBio(bio_field.getText().toString());
            acc.setContactinfo(contactinfo_field.getText().toString());

            // add Account to DB
            acc.toDB().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Log.d(TAG, "created new Account");
                        navctrl.navigate(R.id.action_setupAccountFragment_to_mainActivity);
                    } else {
                        Exception e = task.getException();
                        Log.w(TAG, "failed to create new Account doc in DB", e);
                        Toast.makeText(getContext(),
                                "DB error: could not make Profile\n"
                                        + e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
            });

        });

        // click outside textedits clears focus
        view.setOnTouchListener((v,m) -> {
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        });
    }
}
