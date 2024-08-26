package com.example.prolockloggerv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prolockloggerv1.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnGoogleSignIn.setOnClickListener(v -> signIn());

        checkIfUserAlreadySignedIn();
    }

    private void checkIfUserAlreadySignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Redirect to MainActivity if already signed in
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            handleSignInSuccess(account);
        } catch (ApiException e) {
            Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInSuccess(GoogleSignInAccount account) {
        String email = account.getEmail();
        String firstName = account.getGivenName();
        String lastName = account.getFamilyName();

        // Save user data to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", email);
        editor.putString("user_first_name", firstName);
        editor.putString("user_last_name", lastName);
        editor.apply();

        // Check if email is registered in the database
        checkEmailRegistration(email);
    }

    private void checkEmailRegistration(String email) {
        // Create the ApiService instance
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Create the call to check the email registration
        Call<CheckEmailResponse> call = apiService.checkEmailRegistration(email);

        call.enqueue(new Callback<CheckEmailResponse>() {
            @Override
            public void onResponse(Call<CheckEmailResponse> call, Response<CheckEmailResponse> response) {
                if (response.isSuccessful()) {
                    // Check if response body is not null
                    if (response.body() != null) {
                        CheckEmailResponse checkEmailResponse = response.body();
                        // Check if the email is present in the response, assuming email presence indicates registration
                        if (checkEmailResponse.getEmail() != null && !checkEmailResponse.getEmail().isEmpty()) {
                            // Email is registered, proceed with sign-in
                            redirectToMainActivity();
                        } else {
                            // Email is not registered, show error message
                            Toast.makeText(LoginActivity.this, "Email is not registered. Please sign up first.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Response body is null
                        Log.e("LoginActivity", "Response body is null");
                        Toast.makeText(LoginActivity.this, "Unexpected error: Response body is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle different HTTP response codes
                    switch (response.code()) {
                        case 400:
                            Toast.makeText(LoginActivity.this, "Bad Request: The server could not understand the request.", Toast.LENGTH_SHORT).show();
                            break;
                        case 401:
                            Toast.makeText(LoginActivity.this, "Unauthorized: Access is denied due to invalid credentials.", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(LoginActivity.this, "Not Found: The requested resource could not be found.", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(LoginActivity.this, "Server Error: The server encountered an error.", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, "Unexpected error: " + response.code(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                    Log.e("LoginActivity", "Response error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CheckEmailResponse> call, Throwable t) {
                // Handle different types of network errors
                if (t instanceof java.net.SocketTimeoutException) {
                    Toast.makeText(LoginActivity.this, "Network timeout. Please try again later.", Toast.LENGTH_SHORT).show();
                } else if (t instanceof java.net.UnknownHostException) {
                    Toast.makeText(LoginActivity.this, "No internet connection. Please check your network settings.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to connect to the server. Please try again later.", Toast.LENGTH_SHORT).show();
                }
                Log.e("LoginActivity", "Network error: " + t.getMessage(), t);
            }
        });
    }

    private void handleResponseError(int responseCode) {
        switch (responseCode) {
            case 400:
                Toast.makeText(LoginActivity.this, "Bad Request: The server could not understand the request.", Toast.LENGTH_SHORT).show();
                break;
            case 401:
                Toast.makeText(LoginActivity.this, "Unauthorized: Access is denied due to invalid credentials.", Toast.LENGTH_SHORT).show();
                break;
            case 404:
                Toast.makeText(LoginActivity.this, "Not Found: The requested resource could not be found.", Toast.LENGTH_SHORT).show();
                break;
            case 500:
                Toast.makeText(LoginActivity.this, "Server Error: The server encountered an error.", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(LoginActivity.this, "Unexpected error: " + responseCode, Toast.LENGTH_SHORT).show();
                break;
        }
        Log.e("LoginActivity", "Response error: " + responseCode);
    }





    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
