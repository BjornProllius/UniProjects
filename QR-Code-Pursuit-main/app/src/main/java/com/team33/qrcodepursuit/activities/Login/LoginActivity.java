package com.team33.qrcodepursuit.activities.Login;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.team33.qrcodepursuit.R;

/**
 * login to user account using FirebaseAuth
 * if already logged in -> switch to MainActivity
 */

public class LoginActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
