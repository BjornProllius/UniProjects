package com.team33.qrcodepursuit.activities.Account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.activities.Login.LoginActivity;
import com.team33.qrcodepursuit.activities.MainActivity;
import com.team33.qrcodepursuit.models.Account;

public class MyAccountFragment extends AccountFragment {

    public static final String TAG = "MyAccountFragment";

    private Button loginQRButton;
    private Button logOutButton;

    private NavController navController;

    @Override
    protected boolean setAccount() {
        // check login and set account accordingly
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return false; // not logged in
        this.acc = new Account(user.getUid(), false);
        this.acc.fromDB();
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        loginQRButton = view.findViewById(R.id.account_button_loginqr);
        logOutButton = view.findViewById(R.id.account_button_logout);
        navController = Navigation.findNavController(container);

        loginQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_bottomnavigation_menu_account_to_generateLoginQRFragment);
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void settings_button_onclick(View v) {
        // navigate to settings
    }

    @Override
    protected void editpf_button_onclick(View v) {
        // edit profile (username, contactinfo, bio)
    }

    @Override
    protected void collection_button_onclick(View v) {
        // navigate to my QRcollection
    }
}
