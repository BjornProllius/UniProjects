package com.team33.qrcodepursuit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.activities.Home.ScanFragment;
import com.team33.qrcodepursuit.activities.Login.LoginActivity;
import com.team33.qrcodepursuit.activities.Scoreboard.ScoreBoardFragment;

/**
 * main activity, hosts current fragment and persistent menu bar
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView menu;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private NavController navController;

    /**
     * on creation, set up menu bar and initial fragment
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_container_host);
        navController = navHostFragment.getNavController();

        // get user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        // if not logged in, goto LoginActivity
        if (user == null)
            startActivity(new Intent(this, LoginActivity.class));

        // switch between fragments with bottom navigation menu
        // add frags in nav_main.xml
        // ensure frag id in nav_graph.xml matches id in bottomnavigationmenu.xml
        menu = findViewById(R.id.bottomnavigation);
        NavigationUI.setupWithNavController(menu, navController);
    }
}