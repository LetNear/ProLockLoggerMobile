package com.example.prolockloggerv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class LandingActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Initialize GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        // Get the user name from the intent
        String userName = getIntent().getStringExtra("userName");

        // Display the user name
        TextView userNameTextView = findViewById(R.id.user_name);
        if (userName != null) {
            userNameTextView.setText("Welcome, " + userName);
        } else {
            userNameTextView.setText("Welcome, Guest");
        }

        // Set up the sign-out button
        Button signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(v -> signOut());
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    // After sign-out, return to MainActivity
                    Intent intent = new Intent(LandingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Optional: finish LandingActivity so user cannot return to it with back button
                });
    }
}
