package com.example.prolockloggerv1;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.prolockloggerv1.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
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

        // Check if user is signed in
        if (sharedPreferences.contains("user_email")) {
            // User is signed in, show the profile view
            showUserProfileView();

            // Check user's role and load course details if the user is an instructor (role_number == 2)
            int roleNumber = sharedPreferences.getInt("role_number", -1);
            if (roleNumber == 2) {
                loadAndDisplayCourseDetails(); // Load course details for instructors
            }

            loadLabSchedules();  // You may still load schedules based on your role logic
        } else {
            // Redirect to LoginActivity if not signed in
            redirectToLogin();
        }

        // Set up the logout button click listener
        binding.logoutButton.setOnClickListener(view -> logout());

        return binding.getRoot();
    }

    // Load and display course details (only for instructors)
    // Load and display course details (only for instructors)
    private void loadAndDisplayCourseDetails() {
        String userEmail = sharedPreferences.getString("user_email", "");

        // Log the email being passed to the API
        Log.d("ProfileFragment", "Email being sent to API: " + userEmail);

        apiService.getCourseDetails(userEmail).enqueue(new Callback<CourseDetailsResponse>() {
            @Override
            public void onResponse(Call<CourseDetailsResponse> call, Response<CourseDetailsResponse> response) {
                if (response.isSuccessful()) {
                    // Log the raw response body to see what is returned
                    try {
                        Log.d("ProfileFragment", "Raw response: " + response.body().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CourseDetailsResponse courseDetailsResponse = response.body();
                    if (courseDetailsResponse != null) {
                        List<CourseDetails> courseDetailsList = courseDetailsResponse.getCourseDetails();
                        if (courseDetailsList != null && !courseDetailsList.isEmpty()) {
                            displayCourseDetails(courseDetailsList);
                        } else {
                            Toast.makeText(getActivity(), "No course details available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to parse course details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to load course details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CourseDetailsResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error fetching course details", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Display course details dynamically in the profile layout
    private void displayCourseDetails(List<CourseDetails> courseDetailsList) {
        if (courseDetailsList == null || courseDetailsList.isEmpty()) {
            // Safeguard if the list is null or empty
            Toast.makeText(getActivity(), "No course details to display", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout courseDetailsLayout = binding.courseDetailsLayout;
        courseDetailsLayout.removeAllViews(); // Clear previous views

        for (CourseDetails course : courseDetailsList) {
            // Create a new TextView for each course
            TextView courseView = new TextView(getActivity());
            courseView.setText(String.format("%s (%s)\nDescription: %s\nYear: %s Block: %s",
                    course.getCourseName(),
                    course.getCourseCode(),
                    course.getCourseDescription(),
                    course.getYear(),
                    course.getBlock()));
            courseView.setPadding(16, 16, 16, 16);
            courseView.setTextSize(16f);
            courseView.setOnClickListener(view -> showEditCourseDialog(course));

            // Add the TextView to the layout
            courseDetailsLayout.addView(courseView);
        }
    }

    // Show dialog to edit course details
    private void showEditCourseDialog(CourseDetails course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Course: " + course.getCourseName());

        // Inflate the dialog with a custom layout containing two EditText fields
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_edit_course, null);
        EditText courseDescriptionEditText = dialogView.findViewById(R.id.course_description_edit_text);
        EditText schedulePasswordEditText = dialogView.findViewById(R.id.schedule_password_edit_text);

        // Set initial values
        courseDescriptionEditText.setText(course.getCourseDescription());
        schedulePasswordEditText.setText(course.getSchedulePassword() != null ? course.getSchedulePassword() : "");

        // Disable the password field if the course has no schedule
        if (course.getYear().equals("N/A") && course.getBlock().equals("N/A")) {
            schedulePasswordEditText.setEnabled(false);
        }

        builder.setView(dialogView);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedDescription = courseDescriptionEditText.getText().toString().trim();
            String updatedPassword = schedulePasswordEditText.getText().toString().trim();

            // Call API to update course details
            updateCourseDetails(course, updatedDescription, updatedPassword);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateCourseDetails(CourseDetails course, String description, String password) {
        // Create the request body
        CourseUpdateRequest updateRequest = new CourseUpdateRequest(
                course.getCourseCode(),
                description,
                password.equals("N/A") ? null : password  // Set password to null if it's "N/A"
        );

        // Make API call to update course details
        apiService.updateCourseDetails(updateRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Course updated successfully", Toast.LENGTH_SHORT).show();
                    loadAndDisplayCourseDetails();  // Reload the courses after successful update
                } else {
                    Toast.makeText(getActivity(), "Failed to update course", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to update course", Toast.LENGTH_SHORT).show();
            }
        });
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

                            // Parse the date and display it
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(apiDateFormat.parse(userDetails.getDate_of_birth()));
                            String formattedDate = displayDateFormat.format(calendar.getTime());

                            binding.dateOfBirthEditText.setText(formattedDate);
                        } catch (Exception e) {
                            // If there's an issue with parsing the date, set a default message or handle it gracefully
                            e.printStackTrace();
                            binding.dateOfBirthEditText.setText("Enter date");
                        }

                        // Other user details loading
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
    }// Method to load available lab schedules into the ScrollView
    private void loadLabSchedules() {
        int roleNumber = sharedPreferences.getInt("role_number", -1);  // Fetch role number from shared preferences

        // Check if the user is a student (role_number == 3)
        if (roleNumber == 3) {
            // User is a student, proceed with loading lab schedules
            apiService.getLabSchedules().enqueue(new Callback<List<LabSchedule>>() {
                @Override
                public void onResponse(@NonNull Call<List<LabSchedule>> call, @NonNull Response<List<LabSchedule>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<LabSchedule> labSchedules = response.body();
                        displayLabSchedules(labSchedules);
                    } else {
                        Log.e("ProfileFragment", "Failed to load lab schedules: " + response.code());
                        Toast.makeText(getActivity(), "Failed to load lab schedules", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<LabSchedule>> call, @NonNull Throwable t) {
                    Log.e("ProfileFragment", "Failed to fetch lab schedules: " + t.getMessage());
                    Toast.makeText(getActivity(), "Error fetching lab schedules", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User is not a student, hide the courses layout or show a message
            binding.coursesLayout.setVisibility(View.GONE);  // Hide courses layout
//            Toast.makeText(getActivity(), "No courses available for your role.", Toast.LENGTH_SHORT).show();
        }
    }


    // Method to display lab schedules in the ScrollView
    private void displayLabSchedules(List<LabSchedule> labSchedules) {
        LinearLayout coursesLayout = binding.coursesLayout;

        for (LabSchedule schedule : labSchedules) {
            TextView courseView = new TextView(getActivity());
            courseView.setText(String.format("%s (%s) - %s to %s on %s",
                    schedule.getCourseName(),
                    schedule.getCourseCode(),
                    schedule.getClassStart(),
                    schedule.getClassEnd(),
                    schedule.getDayOfTheWeek()));

            courseView.setPadding(16, 16, 16, 16);
            courseView.setTextSize(16f);
//            courseView.setBackgroundResource(R.drawable.course_item_background);
            courseView.setOnClickListener(view -> showPasswordDialog(schedule));

            coursesLayout.addView(courseView);
        }
    }

    // Show a dialog with a password input for enrolling in a course
    private void showPasswordDialog(LabSchedule schedule) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enroll in " + schedule.getCourseName());

        final EditText passwordInput = new EditText(getActivity());
        passwordInput.setHint("Enter password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setView(passwordInput);

        builder.setPositiveButton("Enroll", (dialog, which) -> {
            String enteredPassword = passwordInput.getText().toString().trim();

            // Check if the user is a student (role_number == 3)


            // Check if the entered password matches the course password
            if (!enteredPassword.equals(schedule.getPassword())) {
                Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed to enroll in the course
            enrollInCourse(schedule);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Enroll in a course using API call
    private void enrollInCourse(LabSchedule schedule) {
        String userEmail = sharedPreferences.getString("user_email", "");

        // Prepare the API call
        Call<EnrollmentResponse> enrollCall = apiService.enrollStudent(userEmail, schedule.getId());

        enrollCall.enqueue(new Callback<EnrollmentResponse>() {
            @Override
            public void onResponse(Call<EnrollmentResponse> call, Response<EnrollmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Enrollment successful
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    // Enrollment failed
                    String errorBody = "Failed to enroll: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorBody += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(getActivity(), errorBody, Toast.LENGTH_SHORT).show();
                    Log.e("ProfileFragment", errorBody);
                }
            }

            @Override
            public void onFailure(Call<EnrollmentResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to connect to the server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Enrollment failed: ", t);
            }
        });
    }
}

