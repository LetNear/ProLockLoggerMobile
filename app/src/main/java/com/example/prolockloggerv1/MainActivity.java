package com.example.prolockloggerv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.prolockloggerv1.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private ApiService apiService;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    return true;

                case R.id.shorts:
                    replaceFragment(new ScheduleFragment());
                    return true;
                case R.id.login:
                    replaceFragment(new ProfileFragment());
                    return true;

                default:
                    return false;
            }
        });

        // Check if user session exists
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        if (sharedPreferences.contains("user_email")) {
            // User is already logged in, redirect to LandingActivity
            Intent intent = new Intent(MainActivity.this, LandingActivity.class);
            startActivity(intent);
            finish();
            return; // Return early to prevent further execution
        }

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        findViewById(R.id.sign_in_button).setOnClickListener(view -> signIn());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                checkAndUpdateAccount(account);
            }
        } catch (ApiException e) {
            Log.w("MainActivity", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(MainActivity.this, "Use a CSPC email account", Toast.LENGTH_LONG).show();
            // Optional: You can also clear the Google sign-in client state if needed.
        }
    }

    private void checkAndUpdateAccount(GoogleSignInAccount account) {
        String email = account.getEmail();
        String name = account.getDisplayName();
        String googleId = account.getId();

        // Log email and name
        Log.d("MainActivity", "Email: " + email);
        Log.d("MainActivity", "Name: " + name);

        String internalDomain = "@my.cspc.edu.ph";
        if (email != null && !email.endsWith(internalDomain)) {
            Toast.makeText(MainActivity.this, "External accounts are not allowed.", Toast.LENGTH_LONG).show();
            return;
        }

        apiService.getAccount(email).enqueue(new Callback<Account>() {

            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                Log.d("Get Account Response: ", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    // Account found, log in and redirect
                    Account existingAccount = response.body();
                    loginAndRedirect(existingAccount);
                } else {
                    // Account not found, show toast
                    Toast.makeText(MainActivity.this, "Account not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                Log.d("Account Error", t.getMessage());
                // Handle error
                Toast.makeText(MainActivity.this, "An error occurred while fetching account details", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginAndRedirect(Account account) {
        // Save user details in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", account.getName());
        editor.putString("user_email", account.getEmail());
        editor.putString("user_google_id", account.getGoogleId());
        editor.apply();

        // Redirect to LandingActivity
        Intent intent = new Intent(MainActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }
}
