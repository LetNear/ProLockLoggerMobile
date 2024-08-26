package com.example.prolockloggerv1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.prolockloggerv1.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ApiService apiService;
    private FragmentProfileBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);

        // Set up the Spinner for gender
        setUpGenderSpinner();
        setUpDateOfBirthPicker();

        // Check if user is signed in
        if (sharedPreferences.contains("user_email")) {
            // User is signed in, show the profile view
            showUserProfileView();
        } else {
            // Redirect to LoginActivity if not signed in
            redirectToLogin();
        }

        // Set up the logout button click listener
        binding.logoutButton.setOnClickListener(view -> logout());

        return binding.getRoot();
    }

    private void setUpGenderSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.genderSpinner.setAdapter(adapter);
    }

    private void setUpDateOfBirthPicker() {
        binding.dateOfBirthEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view, year1, month1, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year1, month1, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        binding.dateOfBirthEditText.setText(dateFormat.format(selectedDate.getTime()));
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void showUserProfileView() {
        binding.signInButton.setVisibility(View.GONE);
        binding.userDetailFormLayout.setVisibility(View.GONE);

        binding.userProfileLayout.setVisibility(View.VISIBLE);
        binding.logoutButton.setVisibility(View.VISIBLE);

        loadUserDetailsIntoProfile();

        binding.editProfileButton.setOnClickListener(view -> showUserDetailForm());
    }

    private void logout() {
        // Clear all user session data from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Sign out from Google if needed
        GoogleSignIn.getClient(getActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

        Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        redirectToLogin();
    }


    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();  // Optionally finish the current activity to prevent returning
    }

    private void showUserDetailForm() {
        binding.userProfileLayout.setVisibility(View.GONE);
        binding.userDetailFormLayout.setVisibility(View.VISIBLE);

        loadUserDetailsIntoForm();

        binding.saveButton.setOnClickListener(view -> saveUserDetails());
    }

    private void loadUserDetailsIntoProfile() {
        binding.userNameTextView.setText(String.format("%s %s",
                sharedPreferences.getString("user_first_name", ""),
                sharedPreferences.getString("user_last_name", "")));
        binding.userEmailTextView.setText(sharedPreferences.getString("user_email", ""));
    }

    private void loadUserDetailsIntoForm() {
        String userEmail = sharedPreferences.getString("user_email", "");

        if (userEmail.isEmpty()) {
            Toast.makeText(getActivity(), "No email found in session", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUserDetails(userEmail).enqueue(new Callback<UserDetailsResponse>() {
            @Override
            public void onResponse(Call<UserDetailsResponse> call, Response<UserDetailsResponse> response) {
                if (response.isSuccessful()) {
                    UserDetailsResponse userDetails = response.body();
                    if (userDetails != null) {
                        try {
                            // Updated date format to match API response
                            SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(apiDateFormat.parse(userDetails.getDate_of_birth()));
                            String formattedDate = displayDateFormat.format(calendar.getTime());

                            binding.dateOfBirthEditText.setText(formattedDate);
                        } catch (Exception e) {
                            e.printStackTrace();
                            binding.dateOfBirthEditText.setText("Invalid date format");
                        }

                        binding.firstNameEditText.setText(userDetails.getFirst_name());
                        binding.middleNameEditText.setText(userDetails.getMiddle_name());
                        binding.lastNameEditText.setText(userDetails.getLast_name());
                        binding.suffixEditText.setText(userDetails.getSuffix());

                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.genderSpinner.getAdapter();
                        if (adapter != null) {
                            int position = adapter.getPosition(userDetails.getGender());
                            binding.genderSpinner.setSelection(position >= 0 ? position : 0);
                        }

                        binding.contactNumberEditText.setText(userDetails.getContact_number());
                        binding.completeAddressEditText.setText(userDetails.getComplete_address());
                    } else {
                        Toast.makeText(getActivity(), "User details not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("ProfileFragment", "Failed to load user details: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<UserDetailsResponse> call, Throwable t) {
                String errorMessage = "Failed to connect to the server";
                if (t != null) {
                    errorMessage += ": " + t.getMessage();
                }
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                Log.e("ProfileFragment", errorMessage, t);
            }
        });
    }



    private void saveUserDetails() {
        String firstName = binding.firstNameEditText.getText().toString().trim();
        String middleName = binding.middleNameEditText.getText().toString().trim();
        String lastName = binding.lastNameEditText.getText().toString().trim();
        String suffix = binding.suffixEditText.getText().toString().trim();
        String dateOfBirth = binding.dateOfBirthEditText.getText().toString().trim();
        String gender = binding.genderSpinner.getSelectedItem().toString();
        String contactNumber = binding.contactNumberEditText.getText().toString().trim();
        String completeAddress = binding.completeAddressEditText.getText().toString().trim();

        // Validate first name
        if (firstName.isEmpty() || !firstName.matches("^[a-zA-Z]+$")) {
            Toast.makeText(getActivity(), "Invalid first name. Only letters are allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate middle name (optional, can be empty)
        if (!middleName.matches("^[a-zA-Z]*$")) {
            Toast.makeText(getActivity(), "Invalid middle name. Only letters are allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate last name
        if (lastName.isEmpty() || !lastName.matches("^[a-zA-Z]+$")) {
            Toast.makeText(getActivity(), "Invalid last name. Only letters are allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate suffix (optional)
        if (!suffix.matches("^[a-zA-Z]*$")) {
            Toast.makeText(getActivity(), "Invalid suffix. Only letters are allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date of birth
        if (dateOfBirth.isEmpty()) {
            Toast.makeText(getActivity(), "Date of birth is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert date format from "MMM dd, yyyy" to "yyyy-MM-dd" and validate
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDateOfBirth;

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDateFormat.parse(dateOfBirth));
            formattedDateOfBirth = outputDateFormat.format(calendar.getTime());

            // Check if the date of birth is not a future date
            if (calendar.after(Calendar.getInstance())) {
                Toast.makeText(getActivity(), "Date of birth cannot be a future date", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate gender
        if (gender.isEmpty() || !isValidGender(gender)) {
            Toast.makeText(getActivity(), "Invalid gender selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate contact number
        if (contactNumber.isEmpty() || !contactNumber.matches("^[0-9]{10,15}$")) { // Example: 10 to 15 digits
            Toast.makeText(getActivity(), "Invalid contact number. It should be between 10 and 15 digits.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate complete address
        if (completeAddress.isEmpty()) {
            Toast.makeText(getActivity(), "Address is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = sharedPreferences.getString("user_email", "");
        Log.d("ProfileFragment", "Updating user with email: " + userEmail);

        UserDetailsRequest userDetailsRequest = new UserDetailsRequest(
                userEmail,
                firstName,
                middleName,
                lastName,
                suffix,
                formattedDateOfBirth,  // Use formatted date
                gender,
                contactNumber,
                completeAddress
        );

        apiService.updateUserDetails(userDetailsRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_first_name", firstName);
                    editor.putString("user_middle_name", middleName);
                    editor.putString("user_last_name", lastName);
                    editor.putString("user_suffix", suffix);
                    editor.putString("user_date_of_birth", formattedDateOfBirth);  // Store formatted date
                    editor.putString("user_gender", gender);
                    editor.putString("user_contact_number", contactNumber);
                    editor.putString("user_complete_address", completeAddress);
                    editor.apply();

                    Toast.makeText(getActivity(), "User details updated successfully", Toast.LENGTH_SHORT).show();
                    showUserProfileView();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getActivity(), "Failed to update user details: " + errorBody, Toast.LENGTH_SHORT).show();
                        Log.e("ProfileFragment", "Update failed: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidGender(String gender) {
        // Define valid gender options or check against a predefined list
        String[] validGenders = getResources().getStringArray(R.array.gender_array);
        for (String validGender : validGenders) {
            if (validGender.equals(gender)) {
                return true;
            }
        }
        return false;
    }


}

