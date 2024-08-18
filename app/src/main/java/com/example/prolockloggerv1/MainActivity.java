package com.example.prolockloggerv1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.prolockloggerv1.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(binding.getRoot());

        // Check if intent has extra data to show HomeFragment
        if (getIntent().getBooleanExtra("show_home_fragment", false)) {
            replaceFragment(new HomeFragment());  // Show HomeFragment directly
        } else {
            replaceFragment(new HomeFragment());  // Default behavior, show HomeFragment
        }

        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.dashboard:
                    replaceFragment(new HomeFragment());
                    return true;

                case R.id.sched:
                    replaceFragment(new ScheduleFragment());
                    return true;

                case R.id.profile:
                    replaceFragment(new ProfileFragment());
                    return true;

                default:
                    return false;
            }
        });

        // Check if user session exists
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        if (sharedPreferences.contains("user_email")) {
            // Load user session if exists
            String userEmail = sharedPreferences.getString("user_email", "");
            Log.d("MainActivity", "User email from session: " + userEmail);
        }
    }

    // Method to replace fragment
    private void replaceFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)  // Make sure you have a FrameLayout with this id in your layout file
                .commit();
    }
}
