package com.example.prolockloggerv1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private static final long TIMEOUT_DURATION = 3600000; // 1 hour in milliseconds
    private Handler handler;
    private Runnable sessionTimeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        sessionTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                handleSessionTimeout(); // Handle session timeout here
            }
        };

        startSessionTimer();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetSessionTimer();  // Reset the timer on user interaction in Activity
    }

    private void startSessionTimer() {
        handler.postDelayed(sessionTimeoutRunnable, TIMEOUT_DURATION);
    }

    private void resetSessionTimer() {
        handler.removeCallbacks(sessionTimeoutRunnable); // Remove any existing callbacks
        startSessionTimer();  // Restart the timer
    }

    private void handleSessionTimeout() {
        // Logic for handling session timeout, e.g., logging out
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Optionally finish current activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sessionTimeoutRunnable);  // Clean up when activity is destroyed
    }

    // This method allows fragments to reset the session timer
    public void resetTimerFromFragment() {
        resetSessionTimer();
    }
}
