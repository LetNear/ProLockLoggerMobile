//package com.example.prolockloggerv1;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//
//public class LandingActivity extends AppCompatActivity {
//
//    private GoogleSignInClient mGoogleSignInClient;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().hide();
//        }
//        setContentView(R.layout.activity_landing);
//
//        // Initialize GoogleSignInClient
//        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
//
//        // Retrieve user details from SharedPreferences
//        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
//        String userName = sharedPreferences.getString("user_name", "Guest");
//
//        // Display the user name
//        TextView userNameTextView = findViewById(R.id.user_name);
//        userNameTextView.setText("Welcome, " + userName);
//
//        // Set up the sign-out button
//        Button signOutButton = findViewById(R.id.sign_out_button);
//        signOutButton.setOnClickListener(v -> signOut());
//        Button scheduleButton = findViewById(R.id.schedule);
//        scheduleButton.setOnClickListener(v -> schedule());
//    }
//
//    private void signOut() {
//        mGoogleSignInClient.signOut()
//                .addOnCompleteListener(this, task -> {
//                    // Clear session data
//                    SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.clear();
//                    editor.apply();
//
//                    // After sign-out, return to MainActivity
//                    Intent intent = new Intent(LandingActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish(); // Optional: finish LandingActivity so user cannot return to it with back button
//                });
//    }
//
//    public void schedule() {
//        Intent intent = new Intent(LandingActivity.this, ScheduleActivity.class);
//        startActivity(intent);
//    }
//}
