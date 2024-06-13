package com.team33.qrcodepursuit.activities.Login;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.activities.MainActivity;

import java.util.concurrent.Executor;

/**
 * Create new Account with email and password
 */
public class SignupFragment extends Fragment {

    // firebase auth
    FirebaseAuth mAuth;
    // UI elements
    TextInputEditText signup_email_field;
    TextInputLayout signup_email_til;
    TextInputEditText signup_passw_field;
    TextInputLayout signup_passw_til;
    TextInputEditText signup_pwconf_field;
    TextInputLayout signup_pwconf_til;
    Button signup_button;
    TextView prvpolicy_link;
    NavController navctrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        { return inflater.inflate(R.layout.fragment_signup, container, false); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // link UI elements
        signup_email_field = view.findViewById(R.id.signup_email_TextInputEditText);
        signup_email_til = view.findViewById(R.id.signup_email_TextInput);
        signup_passw_field = view.findViewById(R.id.signup_passw_TextInputEditText);
        signup_passw_til = view.findViewById(R.id.signup_passw_TextInput);
        signup_pwconf_field = view.findViewById(R.id.signup_pwconf_TextInputEditText);
        signup_pwconf_til = view.findViewById(R.id.signup_passwconf_TextInput);
        signup_button = view.findViewById(R.id.signup_button);
        prvpolicy_link = view.findViewById(R.id.signup_prvpolicy_link);
        navctrl = Navigation.findNavController(view);
        // get auth
        mAuth = FirebaseAuth.getInstance();
        // callbacks
        // create account and log in
        signup_button.setOnClickListener(view1 -> {
            String email  = signup_email_field.getText().toString();
            String passw  = signup_passw_field.getText().toString();
            String pwconf = signup_pwconf_field.getText().toString();
            if (email.equals("") || passw.equals("")) return;
            // check passw == pwconf
            if (!passw.equals(pwconf)) {
                Toast.makeText(getContext(),
                        "passwords do not match",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email,passw)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // switch to SetupAccount
                            Log.d(TAG, "signUpWithEmail:success");
                            navctrl.navigate(R.id.action_signupFragment_to_setupAccountFragment);
                        } else {
                            Exception e = task.getException();
                            Log.w("W/FirebaseAuth", "failed to create user", e);
                            Toast.makeText(getContext(),
                                    "failed to make account:\n"
                                            + e.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
        // view privacy policy
        prvpolicy_link.setOnClickListener(view1 ->
                navctrl.navigate(R.id.action_signupFragment_to_prvPolicyFragment));

        // click outside textedits clears focus
        view.setOnTouchListener((v,m) -> {
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        });
    }
}
