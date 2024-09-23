package com.example.prolockloggerv1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScheduleFragment extends Fragment {

    private TableLayout tableLayout;
    private Button nextPageButton, previousPageButton, searchButton;
    private TextView pageIndicator, userNameTextView;
    private EditText searchBar;
    private List<Schedule> allSchedules;
    private List<Schedule> filteredSchedules;
    private int currentPage = 0;
    private int pageSize = 5;
    private static final int REFRESH_INTERVAL_MS = 5000;
    private Handler handler = new Handler();
    private ScheduleApi scheduleApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "Guest");
        boolean isSignedIn = sharedPreferences.getBoolean("is_signed_in", false);
        String userEmail = sharedPreferences.getString("user_email", "");
        int roleNumber = sharedPreferences.getInt("role_number", 2); // Ensure role_number is an integer

        userNameTextView = rootView.findViewById(R.id.user_name);
        userNameTextView.setText("Welcome, " + userName);

        tableLayout = rootView.findViewById(R.id.tableLayout);
        nextPageButton = rootView.findViewById(R.id.nextPageButton);
        previousPageButton = rootView.findViewById(R.id.previousPageButton);
        pageIndicator = rootView.findViewById(R.id.pageIndicator);

        searchBar = rootView.findViewById(R.id.searchBar);
        searchButton = rootView.findViewById(R.id.searchButton);

        allSchedules = new ArrayList<>();
        filteredSchedules = new ArrayList<>();

        // Initialize Retrofit and ScheduleApi
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://prolocklogger.pro/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        scheduleApi = retrofit.create(ScheduleApi.class);

        // Load schedules based on role_number
        if (isSignedIn) {
            handler.post(() -> loadSchedules(roleNumber, userEmail));
        } else {
            displayNotSignedInMessage();
        }

        nextPageButton.setOnClickListener(v -> {
            if (currentPage < getMaxPage()) {
                currentPage++;
                displayPage(currentPage);
            }
        });

        previousPageButton.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                displayPage(currentPage);
            }
        });

        searchButton.setOnClickListener(v -> performSearch());

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            // Load schedules based on role_number
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
            int roleNumber = sharedPreferences.getInt("role_number", 2);
            String userEmail = sharedPreferences.getString("user_email", "");
            loadSchedules(roleNumber, userEmail);
            handler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private void loadSchedules(int roleNumber, String userEmail) {
        Call<ScheduleResponse> call;

        // Log the email being used
        Log.d("ScheduleFragment", "User Email: " + userEmail);

        // Check roleNumber to decide which API to call
        if (roleNumber == 2) {
            call = scheduleApi.getSchedulesByEmail(userEmail); // Main schedule API for students
            Log.d("ScheduleFragment", "Calling API: https://prolocklogger.pro/api/lab-schedules/email/" + userEmail);
        } else if (roleNumber == 3) {
            call = scheduleApi.getAlternativeSchedulesByEmail(userEmail); // Alternative schedule API for instructors/admins
            Log.d("ScheduleFragment", "Calling API: https://prolocklogger.pro/student-schedule/" + userEmail);
        } else {
            call = scheduleApi.getSchedulesByEmail(userEmail);
            Log.d("ScheduleFragment", "Calling API: https://prolocklogger.pro/api/lab-schedules/email/" + userEmail);
        }

        // Make the API call
        call.enqueue(new Callback<ScheduleResponse>() {
            @Override
            public void onResponse(Call<ScheduleResponse> call, Response<ScheduleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ScheduleResponse scheduleResponse = response.body();
                    allSchedules.clear();
                    allSchedules.addAll(scheduleResponse.getSchedules()); // Fetch schedules from response

                    // Now set filteredSchedules to be the same as allSchedules initially
                    filteredSchedules.clear();
                    filteredSchedules.addAll(allSchedules); // Initialize the filtered list

                    Log.d("ScheduleFragment", "Schedules fetched: " + allSchedules.size());
                    displayPage(currentPage);
                } else {
                    Log.d("ScheduleFragment", "Failed to load schedules. Response code: " + response.code());
                    Toast.makeText(getActivity(), "No schedule found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                Log.e("ScheduleFragment", "Error fetching data", t);
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void performSearch() {
        String query = searchBar.getText().toString().trim().toLowerCase();  // Trim the input and convert to lower case

        // If the query is empty, reset the filtered list to show all schedules
        if (query.isEmpty()) {
            filteredSchedules.clear();
            filteredSchedules.addAll(allSchedules);
            displayPage(currentPage);
            return;
        }

        filteredSchedules.clear();

        for (Schedule schedule : allSchedules) {
            if (matchesSearchQuery(schedule, query)) {
                filteredSchedules.add(schedule);
            }
        }

        // Reset to the first page after search
        currentPage = 0;
        displayPage(currentPage);
    }

    // Helper method to check if a schedule matches the query
    private boolean matchesSearchQuery(Schedule schedule, String query) {
        return containsIgnoreCase(schedule.getCourseCode(), query)
                || containsIgnoreCase(schedule.getCourseName(), query)
                || containsIgnoreCase(schedule.getDayOfTheWeek(), query)
                || containsIgnoreCase(schedule.getClassStart(), query)
                || containsIgnoreCase(schedule.getClassEnd(), query)
                || containsIgnoreCase(schedule.getSpecificDate(), query);
    }

    // Utility method to safely check if a string contains the query (ignores null and case)
    private boolean containsIgnoreCase(String source, String query) {
        return source != null && source.toLowerCase().contains(query);
    }


    private void displayNotSignedInMessage() {
        if (tableLayout.getChildCount() > 1) {
            tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
        }

        TableRow row = new TableRow(getActivity());
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView noDataTextView = new TextView(getActivity());
        noDataTextView.setText("Please sign in to view your schedules.");
        noDataTextView.setPadding(8, 8, 8, 8);
        noDataTextView.setGravity(android.view.Gravity.CENTER);
        noDataTextView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        row.addView(noDataTextView);
        tableLayout.addView(row);

        previousPageButton.setEnabled(false);
        nextPageButton.setEnabled(false);

        pageIndicator.setText("Page 1");
    }

    private void displayPage(int page) {
        // Clear existing rows (except the header row)
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        // Check if there are schedules to display
        if (filteredSchedules.isEmpty()) {
            // Display no schedules message
            TableRow row = new TableRow(requireContext());
            TextView noDataTextView = new TextView(requireContext());
            noDataTextView.setText("No schedules yet!");
            noDataTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            noDataTextView.setPadding(12, 12, 12, 12); // Add padding
            row.addView(noDataTextView);
            tableLayout.addView(row);
        } else {
            // Determine the range of items to show on this page
            int start = page * pageSize;
            int end = Math.min(start + pageSize, filteredSchedules.size());

            // Iterate over the schedules to display them
            for (int i = start; i < end; i++) {
                Schedule schedule = filteredSchedules.get(i);

                // Adding a dynamic row in your Java code for each schedule item
                TableRow row = new TableRow(requireContext());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

// Course Code
                TextView courseCodeView = new TextView(requireContext());
                courseCodeView.setText(schedule.getCourseCode());
                courseCodeView.setPadding(12, 12, 12, 12); // Padding
                courseCodeView.setGravity(Gravity.CENTER); // Center the text
                courseCodeView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
                row.addView(courseCodeView);

// Course Name
                TextView courseNameView = new TextView(requireContext());
                courseNameView.setText(schedule.getCourseName());
                courseNameView.setPadding(12, 12, 12, 12); // Padding
                courseNameView.setGravity(Gravity.CENTER); // Center the text
                courseNameView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f));
                row.addView(courseNameView);

// Time and Day
                TextView timeAndDayView = new TextView(requireContext());
                timeAndDayView.setText(schedule.getDayOfTheWeek() + "\n" + schedule.getClassStart() + " - " + schedule.getClassEnd());
                timeAndDayView.setPadding(12, 12, 12, 12); // Padding
                timeAndDayView.setGravity(Gravity.CENTER); // Center the text
                timeAndDayView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f));
                row.addView(timeAndDayView);

// Block
                TextView blockView = new TextView(requireContext());
                blockView.setText(schedule.getBlockYear());
                blockView.setPadding(12, 12, 12, 12); // Padding
                blockView.setGravity(Gravity.CENTER); // Center the text
                blockView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(blockView);

// Add the row to the table layout
                tableLayout.addView(row);

            }

            // Handle the visibility of navigation buttons
            previousPageButton.setEnabled(currentPage > 0);
            nextPageButton.setEnabled(end < filteredSchedules.size());

            // Update the page indicator
            pageIndicator.setText(String.format("Page %d", currentPage + 1));
        }
    }



    private int getMaxPage() {
        return (int) Math.ceil((double) filteredSchedules.size() / pageSize) - 1;
    }

    private String getBlockLetter(int blockId) {
        if (blockId < 1) return "N/A"; // Handle invalid blockId
        return String.valueOf((char) ('A' + blockId - 1)); // Converts 1 to 'A', 2 to 'B', etc.
    }

}
