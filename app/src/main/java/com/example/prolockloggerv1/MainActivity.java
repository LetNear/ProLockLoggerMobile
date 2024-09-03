package com.example.prolockloggerv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set the background color of the bottom navigation to blue
        binding.bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.blue));

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
                case R.id.fab:
                    replaceFragment(new DoorControlFragment()); // Open Door Control Fragment
                    return true;


                default:
                    return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user session exists
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        if (!sharedPreferences.contains("user_email")) {
            // Redirect to LoginActivity if no user session
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();  // Prevent returning to MainActivity
        }
    }

    // Method to replace fragment
    private void replaceFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
