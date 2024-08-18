package com.example.prolockloggerv1;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private ApiService apiService;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Check if user session exists
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", MODE_PRIVATE);
        if (sharedPreferences.contains("user_email")) {
            // User is already logged in, show profile details
            showProfileDetails(view);
        } else {
            // User is not logged in, show sign-in button
            showSignInButton(view);
        }

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        return view;
    }

    private void showProfileDetails(View view) {
        // Inflate your profile details layout here
        // e.g., TextViews for displaying user details
        // You can retrieve user details from SharedPreferences and set them to the views
    }

    private void showSignInButton(View view) {
        Button signInButton = view.findViewById(R.id.sign_in_button);
        signInButton.setVisibility(View.VISIBLE);
        signInButton.setOnClickListener(v -> signIn());
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
            Log.w("ProfileFragment", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(requireActivity(), "Use a CSPC email account", Toast.LENGTH_LONG).show();
        }
    }

    private void checkAndUpdateAccount(GoogleSignInAccount account) {
        String email = account.getEmail();

        // Log email
        Log.d("ProfileFragment", "Email: " + email);

        String internalDomain = "@my.cspc.edu.ph";
        if (email != null && !email.endsWith(internalDomain)) {
            Toast.makeText(requireActivity(), "External accounts are not allowed.", Toast.LENGTH_LONG).show();
            return;
        }

        apiService.getAccount(email).enqueue(new Callback<Account>() {
            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Account found, log in and redirect
                    Account existingAccount = response.body();
                    loginAndRedirect(existingAccount);
                } else {
                    // Account not found, show toast
                    Toast.makeText(requireActivity(), "Account not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                Log.d("Account Error", t.getMessage());
                Toast.makeText(requireActivity(), "An error occurred while fetching account details", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginAndRedirect(Account account) {
        // Save user details in SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", account.getName());
        editor.putString("user_email", account.getEmail());
        editor.putString("user_google_id", account.getGoogleId());
        editor.apply();

        // Redirect to LandingActivity
        Intent intent = new Intent(requireActivity(), LandingActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
