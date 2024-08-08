package com.example.prolockloggerv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        findViewById(R.id.sign_in_button).setOnClickListener(view -> signIn());
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

            // Redirect to MainActivity
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optionally finish the current activity to prevent returning
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
                    Account account = response.body();

//                    TODO: LOGIN ACCOUNT AND REDIRECT
                }
                Account newUser = new Account(name, email, googleId);
                createAccount(newUser);
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {

//                TODO: Make error when email is not found
//                TODO: Email was not found, redirect to registration

            }
        });

    }

    private void createAccount(Account account) {
        apiService.addAccount(account).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Account Creation response", response.toString());
//                    Account is created
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();

//                    TODO: Redirect to another page
                    Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                    startActivity(intent);

                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
//                There was an error
//                TODO: Handle error
                Log.d("Account Error", t.getMessage());
            }
        });
    }
}


