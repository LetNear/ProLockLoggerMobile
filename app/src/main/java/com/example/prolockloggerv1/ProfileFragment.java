package com.example.prolockloggerv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prolockloggerv1.databinding.FragmentProfileBinding;
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
    FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Set up the Google Sign-In button
        binding.signInButton.setOnClickListener(view -> signIn());

        return binding.getRoot();
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
            Toast.makeText(getActivity(), "Use a CSPC email account", Toast.LENGTH_LONG).show();
        }
    }

    private void checkAndUpdateAccount(GoogleSignInAccount account) {
        String email = account.getEmail();
        String name = account.getDisplayName();
        String googleId = account.getId();

        // Log email and name
        Log.d("ProfileFragment", "Email: " + email);
        Log.d("ProfileFragment", "Name: " + name);

        String internalDomain = "@my.cspc.edu.ph";
        if (email != null && !email.endsWith(internalDomain)) {
            Toast.makeText(getActivity(), "External accounts are not allowed.", Toast.LENGTH_LONG).show();
            return;
        }

        apiService.getAccount(email).enqueue(new Callback<Account>() {

            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                Log.d("Get Account Response: ", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    Account existingAccount = response.body();
                    loginAndRedirect(existingAccount);
                } else {
                    Toast.makeText(getActivity(), "Account not found!, Consult your Instructor", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                Log.d("Account Error", t.getMessage());
                Toast.makeText(getActivity(), "An error occurred while fetching account details", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginAndRedirect(Account account) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", account.getName());
        editor.putString("user_email", account.getEmail());
        editor.putString("user_google_id", account.getGoogleId());
        editor.apply();

        // Redirect to MainActivity and pass extra data to show HomeFragment
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("show_home_fragment", true);  // Passing data to show HomeFragment
        startActivity(intent);
        getActivity().finish();
    }
}
