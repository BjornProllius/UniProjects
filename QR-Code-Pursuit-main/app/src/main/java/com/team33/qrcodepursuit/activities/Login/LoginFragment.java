package com.team33.qrcodepursuit.activities.Login;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.activities.MainActivity;

import java.util.concurrent.Executor;

public class LoginFragment extends Fragment{

    // gui elements
    TextInputEditText login_email_field;
    TextInputEditText login_passw_field;
    Button login_button;
    Button qr_login_button;
    TextView forgotpw_link;
    TextView signup_link;
    TextView prvpolicy_link;
    NavController navctrl;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        { return inflater.inflate(R.layout.fragment_login, container, false); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navctrl = Navigation.findNavController(view);
        // link gui elements
        login_email_field  = view.findViewById(R.id.login_email_TextInputEditText);
        login_passw_field  = view.findViewById(R.id.login_passw_TextInputEditText);
        login_button = view.findViewById(R.id.login_button);
        qr_login_button = view.findViewById(R.id.login_withqr_button);
        forgotpw_link = view.findViewById(R.id.login_forgotpw_link);
        signup_link = view.findViewById(R.id.login_signup_link);
        prvpolicy_link = view.findViewById(R.id.login_prvpolicy_link);
        // set button callbacks
        login_button.setOnClickListener(v -> {
            // log in with email+pw
            String email = login_email_field.getText().toString();
            String passw = login_passw_field.getText().toString();
            if (email.equals("") || passw.equals("")) return;
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, passw)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // switch to MainActivity
                            Log.d(TAG, "signInWithEmail:success");
                            //navctrl.navigate(R.id.action_loginFragment_to_mainActivity);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                        else {
                            Exception e = task.getException();
                            Log.w(TAG, "signInWithEmail:failure", e);
                            Toast.makeText(getContext(),
                                    "Authentication failed:\n" + e.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
        qr_login_button.setOnClickListener(v ->
                navctrl.navigate(R.id.action_loginFragment_to_loginWithQRFragment));
        forgotpw_link.setOnClickListener(v ->
                navctrl.navigate(R.id.action_loginFragment_to_forgotPwdFragment));
        prvpolicy_link.setOnClickListener(v ->
                navctrl.navigate(R.id.action_loginFragment_to_prvPolicyFragment));
        signup_link.setOnClickListener(v ->
                navctrl.navigate(R.id.action_loginFragment_to_signupFragment));

        // click outside textedits clears focus
        view.setOnTouchListener((v,m) -> {
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        });
    }
}
