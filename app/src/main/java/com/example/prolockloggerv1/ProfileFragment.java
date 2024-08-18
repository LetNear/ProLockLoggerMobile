package com.example.prolockloggerv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ApiService apiService;
    private FragmentProfileBinding binding;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001; // Request code for Google Sign-In

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Initialize Google Sign-In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id)) // replace with your server client ID
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Set up the Spinner for gender
        setUpGenderSpinner();

        // Check if user is signed in
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        if (sharedPreferences.contains("user_email")) {
            // User is signed in, show the profile view
            showUserProfileView();
        } else {
            // User is not signed in, show Google Sign-In button
            showGoogleSignInButton();
        }

        // Set up the logout button click listener
        binding.logoutButton.setOnClickListener(view -> logout());

        return binding.getRoot();
    }


    private void setUpGenderSpinner() {
        Spinner genderSpinner = binding.genderSpinner;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void showGoogleSignInButton() {
        // Show Google Sign-In button
        binding.signInButton.setVisibility(View.VISIBLE);

        // Set up the Google Sign-In button
        binding.signInButton.setOnClickListener(view -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void showUserProfileView() {
        // Hide Google Sign-In button and user detail form
        binding.signInButton.setVisibility(View.GONE);
        binding.userDetailFormLayout.setVisibility(View.GONE);

        // Show user profile view
        binding.userProfileLayout.setVisibility(View.VISIBLE);

        // Show Logout button
        binding.logoutButton.setVisibility(View.VISIBLE);

        // Load existing user details
        loadUserDetailsIntoProfile();

        // Set up Edit Profile button click listener
        binding.editProfileButton.setOnClickListener(view -> {
            showUserDetailForm();
        });

        // Set up Logout button click listener
        binding.logoutButton.setOnClickListener(view -> {
            logout();
        });
    }

    private void logout() {
        // Clear user session from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
            // Redirect to MainActivity
            Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish(); // Optionally finish the current activity to prevent returning
        });
    }


    private void showUserDetailForm() {
        // Hide user profile view
        binding.userProfileLayout.setVisibility(View.GONE);

        // Show user detail form
        binding.userDetailFormLayout.setVisibility(View.VISIBLE);

        // Load existing user details into form
        loadUserDetailsIntoForm();

        // Save button click listener
        binding.saveButton.setOnClickListener(view -> {
            saveUserDetails();
        });
    }

    private void loadUserDetailsIntoProfile() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        binding.userNameTextView.setText(String.format("%s %s",
                sharedPreferences.getString("user_first_name", ""),
                sharedPreferences.getString("user_last_name", "")));
        binding.userEmailTextView.setText(sharedPreferences.getString("user_email", ""));
        // Add more fields here if needed
    }

    private void loadUserDetailsIntoForm() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        binding.firstNameEditText.setText(sharedPreferences.getString("user_first_name", ""));
        binding.middleNameEditText.setText(sharedPreferences.getString("user_middle_name", ""));
        binding.lastNameEditText.setText(sharedPreferences.getString("user_last_name", ""));
        binding.suffixEditText.setText(sharedPreferences.getString("user_suffix", ""));
        binding.dateOfBirthEditText.setText(sharedPreferences.getString("user_date_of_birth", ""));

        // Ensure the Spinner is properly initialized
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.genderSpinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(sharedPreferences.getString("user_gender", ""));
            binding.genderSpinner.setSelection(position >= 0 ? position : 0);
        }

        binding.contactNumberEditText.setText(sharedPreferences.getString("user_contact_number", ""));
        binding.completeAddressEditText.setText(sharedPreferences.getString("user_complete_address", ""));
    }

    private void saveUserDetails() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_first_name", binding.firstNameEditText.getText().toString());
        editor.putString("user_middle_name", binding.middleNameEditText.getText().toString());
        editor.putString("user_last_name", binding.lastNameEditText.getText().toString());
        editor.putString("user_suffix", binding.suffixEditText.getText().toString());
        editor.putString("user_date_of_birth", binding.dateOfBirthEditText.getText().toString());
        editor.putString("user_gender", binding.genderSpinner.getSelectedItem().toString());
        editor.putString("user_contact_number", binding.contactNumberEditText.getText().toString());
        editor.putString("user_complete_address", binding.completeAddressEditText.getText().toString());
        editor.apply();

        Toast.makeText(getActivity(), "User details updated successfully", Toast.LENGTH_SHORT).show();

        // After saving, show the profile view again
        showUserProfileView();
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
                Log.d("ProfileFragment", "Signed in with email: " + account.getEmail()); // Log the email here
                checkAndUpdateAccount(account);
            }
        } catch (ApiException e) {
            Log.w("ProfileFragment", "signInResult:failed code=" + e.getStatusCode());

            Toast.makeText(getActivity(), "Use a CSPC email account", Toast.LENGTH_LONG).show();

            // Redirect to MainActivity
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish(); // Optionally finish the current activity to prevent returning
        }
    }

    private void checkAndUpdateAccount(GoogleSignInAccount account) {
        String email = account.getEmail();
        String internalDomain = "@my.cspc.edu.ph";

        if (email != null && !email.endsWith(internalDomain)) {
            Toast.makeText(getActivity(), "External accounts are not allowed.", Toast.LENGTH_LONG).show();

            // Log the invalid email and redirect to MainActivity
            Log.d("ProfileFragment", "Invalid email domain: " + email);

            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish(); // Optionally finish the current activity to prevent returning
            return;
        }

        Log.d("ProfileFragment", "Checking account with email: " + email);

        apiService.getAccount(email).enqueue(new Callback<Account>() {
            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                Log.d("ProfileFragment", "API Response Code: " + response.code());
                Log.d("ProfileFragment", "API Response: " + response.toString());

                if (response.isSuccessful() && response.body() != null) {
                    // User exists in the database
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_email", email);
                    editor.putString("user_first_name", account.getGivenName());
                    editor.putString("user_last_name", account.getFamilyName());
                    editor.putBoolean("is_signed_in", true); // Store sign-in status
                    editor.apply();

                    // Hide Google Sign-In button and show user profile view
                    showUserProfileView();

                    // Redirect to HomeFragment
                    Fragment fragment = new HomeFragment(); // Replace with your HomeFragment class
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                } else {
                    // User does not exist in the database
                    Toast.makeText(getActivity(), "Account not recognized", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
