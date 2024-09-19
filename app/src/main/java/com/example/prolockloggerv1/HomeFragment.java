package com.example.prolockloggerv1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView stat1Title, stat1Value, stat2Title, stat2Value, welcomeText;
    private Button getStartedButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        stat1Title = view.findViewById(R.id.stat1Title);
        stat1Value = view.findViewById(R.id.stat1Value);
        stat2Title = view.findViewById(R.id.stat2Title);
        stat2Value = view.findViewById(R.id.stat2Value);
        welcomeText = view.findViewById(R.id.welcomeText);

        // Fetch the user data from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "");
        String email = sharedPreferences.getString("user_email", "");
        int roleNumber;

        // Use a default value if role_number is not present or invalid
        try {
            roleNumber = sharedPreferences.getInt("role_number", 2);  // Default to 2 if not set
        } catch (ClassCastException e) {
            // Handle the case where the stored value is not an integer
            String roleNumberString = sharedPreferences.getString("role_number", "2");
            try {
                roleNumber = Integer.parseInt(roleNumberString);
            } catch (NumberFormatException ex) {
                roleNumber = 2; // Default to 2 if parsing fails
            }
        }

        // Update the welcome text
        if (!userName.isEmpty()) {
            welcomeText.setText("Welcome " + userName);
        }

        // Set initial visibility to GONE
        stat1Title.setVisibility(View.GONE);
        stat1Value.setVisibility(View.GONE);
        stat2Title.setVisibility(View.GONE);
        stat2Value.setVisibility(View.GONE);

        if (!email.isEmpty()) {
            if (roleNumber == 2) {
                // Role number 2: Fetch student and schedule counts
                stat1Title.setText("Total Students: ");
                stat2Title.setText("Total Schedules: ");
                fetchStudentCount(email);
                fetchScheduleCount(email);
                // Show the labels and values for role 2
                stat1Title.setVisibility(View.VISIBLE);
                stat1Value.setVisibility(View.VISIBLE);
                stat2Title.setVisibility(View.VISIBLE);
                stat2Value.setVisibility(View.VISIBLE);
            } else if (roleNumber == 3) {
                // Role number 3: Fetch student schedule count and total logs count
                stat1Title.setText("Student Schedules: ");
                stat2Title.setText("Total Logs: ");
                fetchStudentScheduleCount(email);
                fetchTotalLogsCount(email);
                // Show the labels and values for role 3
                stat1Title.setVisibility(View.VISIBLE);
                stat1Value.setVisibility(View.VISIBLE);
                stat2Title.setVisibility(View.VISIBLE);
                stat2Value.setVisibility(View.VISIBLE);
            } else {
                // Hide all statistics if the role number is invalid
                Toast.makeText(getContext(), "Invalid role number", Toast.LENGTH_SHORT).show();
                stat1Title.setVisibility(View.GONE);
                stat1Value.setVisibility(View.GONE);
                stat2Title.setVisibility(View.GONE);
                stat2Value.setVisibility(View.GONE);
            }
        } else {
            // Hide all statistics if no email is found
            Toast.makeText(getContext(), "No email found in session", Toast.LENGTH_SHORT).show();
            stat1Title.setVisibility(View.GONE);
            stat1Value.setVisibility(View.GONE);
            stat2Title.setVisibility(View.GONE);
            stat2Value.setVisibility(View.GONE);
        }

        return view;
    }


    private void fetchStudentCount(String email) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<StudentCountResponse> call = apiService.getStudentCountByEmail(email);

        call.enqueue(new Callback<StudentCountResponse>() {
            @Override
            public void onResponse(Call<StudentCountResponse> call, Response<StudentCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Ensure you are using the correct method to get the student count
                    stat1Value.setText(String.valueOf(response.body().getStudentCount()));
                } else {
                    Toast.makeText(getContext(), "Failed to fetch student count", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StudentCountResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchScheduleCount(String email) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ScheduleCountResponse> call = apiService.getScheduleCountByEmail(email);

        call.enqueue(new Callback<ScheduleCountResponse>() {
            @Override
            public void onResponse(Call<ScheduleCountResponse> call, Response<ScheduleCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ScheduleCountResponse scheduleResponse = response.body();

                    // Ensure the schedule count is not null
                    if (scheduleResponse.getScheduleCount() != null) {
                        stat2Value.setText(String.valueOf(scheduleResponse.getScheduleCount()));
                        Log.i("HomeFragment", "Instructor: " + scheduleResponse.getInstructor() +
                                ", Schedule Count: " + scheduleResponse.getScheduleCount());
                    } else {
                        Log.e("HomeFragment", "Error: Schedule count is null");
                        Toast.makeText(getContext(), "Invalid schedule count received", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("HomeFragment", "Error: Response unsuccessful or body is null");
                    Toast.makeText(getContext(), "Failed to fetch schedule count", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScheduleCountResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchStudentScheduleCount(String email) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<StudentScheduleCountResponse> call = apiService.getStudentScheduleCount(email);

        call.enqueue(new Callback<StudentScheduleCountResponse>() {
            @Override
            public void onResponse(Call<StudentScheduleCountResponse> call, Response<StudentScheduleCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stat1Value.setText(String.valueOf(response.body().getScheduleCount()));
                } else {
                    Toast.makeText(getContext(), "Failed to fetch schedule count", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StudentScheduleCountResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTotalLogsCount(String email) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LogCountResponse> call = apiService.getTotalLogsCount(email);

        call.enqueue(new Callback<LogCountResponse>() {
            @Override
            public void onResponse(Call<LogCountResponse> call, Response<LogCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stat2Value.setText(String.valueOf(response.body().getLogCount()));
                } else {
                    Toast.makeText(getContext(), "Failed to fetch log count", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LogCountResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
