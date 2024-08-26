package com.example.prolockloggerv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScheduleActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private Button nextPageButton, previousPageButton, backButton;
    private TextView pageIndicator, userNameTextView;
    private List<Schedule> allSchedules;  // Store all the schedules
    private int currentPage = 0;
    private int pageSize = 15; // Number of rows per page

    private static final int REFRESH_INTERVAL_MS = 5000; // 5 seconds
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_schedule);

        // Retrieve user details from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "Guest");
        String userEmail = sharedPreferences.getString("user_email", "");

        // Display the user name
        userNameTextView = findViewById(R.id.user_name);
        userNameTextView.setText("Welcome, " + userName);

        tableLayout = findViewById(R.id.tableLayout);
        nextPageButton = findViewById(R.id.nextPageButton);
        previousPageButton = findViewById(R.id.previousPageButton);
        pageIndicator = findViewById(R.id.pageIndicator);
        backButton = findViewById(R.id.backButton); // Initialize the Back button

        allSchedules = new ArrayList<>();

        // Start periodic refresh
        handler.post(refreshRunnable);

        nextPageButton.setOnClickListener(v -> {
            currentPage++;
            displayPage(currentPage);
        });

        previousPageButton.setOnClickListener(v -> {
            currentPage--;
            displayPage(currentPage);
        });

        // Set up back button click listener
        backButton.setOnClickListener(v -> onBackPressed()); // Handle back navigation

        // Load schedules filtered by user email
        loadSchedules(userEmail);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the periodic refresh when the activity is destroyed
        handler.removeCallbacks(refreshRunnable);
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            // Retrieve user email from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String userEmail = sharedPreferences.getString("user_email", "");
            loadSchedules(userEmail);
            handler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private void loadSchedules(String userEmail) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://prolocklogger.pro/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ScheduleApi scheduleApi = retrofit.create(ScheduleApi.class);

        // Pass the user's email to the API call
        Call<List<Schedule>> call = scheduleApi.getSchedulesByEmail(userEmail);
        call.enqueue(new Callback<List<Schedule>>() {
            @Override
            public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allSchedules = response.body();
                    Log.d("ScheduleActivity", "Schedules fetched: " + allSchedules.size());
                    displayPage(currentPage);
                } else {
                    Log.d("ScheduleActivity", "Failed to load schedules. Response code: " + response.code());
                    Toast.makeText(ScheduleActivity.this, "Failed to load schedules", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Schedule>> call, Throwable t) {
                Log.e("ScheduleActivity", "Error fetching data", t);
                Toast.makeText(ScheduleActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPage(int page) {
        // Clear existing rows (except the header)
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        if (allSchedules.isEmpty()) {
            // If there are no schedules, display a message indicating no schedules
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            TextView noDataTextView = new TextView(this);
            noDataTextView.setText("No schedules yet!");
            noDataTextView.setPadding(8, 8, 8, 8);
            noDataTextView.setGravity(android.view.Gravity.CENTER);
            noDataTextView.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            row.addView(noDataTextView);
            tableLayout.addView(row);

            // Disable page navigation buttons since there's no data
            previousPageButton.setEnabled(false);
            nextPageButton.setEnabled(false);

            // Update the page indicator
            pageIndicator.setText("Page 1");
        } else {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, allSchedules.size());

            for (int i = start; i < end; i++) {
                Schedule schedule = allSchedules.get(i);

                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                // Create and add the Schedule Id TextView
                TextView id = new TextView(this);
                id.setText(String.valueOf(schedule.getId()));
                id.setPadding(8, 8, 8, 8);
                id.setGravity(android.view.Gravity.CENTER);
                id.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(id);

                // Create and add the Course TextView
                TextView course = new TextView(this);
                course.setText(schedule.getSubjectName());
                course.setPadding(8, 8, 8, 8);
                course.setGravity(android.view.Gravity.CENTER);
                course.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(course);

                // Create and add the Time and Day TextView
                TextView timeAndDay = new TextView(this);
                timeAndDay.setText(schedule.getDayOfTheWeek() + " " + schedule.getClassStart() + " - " + schedule.getClassEnd());
                timeAndDay.setPadding(8, 8, 8, 8);
                timeAndDay.setGravity(android.view.Gravity.CENTER);
                timeAndDay.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(timeAndDay);

                // Create and add the Block TextView
                TextView block = new TextView(this);
                block.setText(String.valueOf(schedule.getBlockId()));
                block.setPadding(8, 8, 8, 8);
                block.setGravity(android.view.Gravity.CENTER);
                block.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(block);

                // Add the row to the table layout
                tableLayout.addView(row);
            }

            // Calculate remaining rows needed to fill the space
            int remainingRows = pageSize - (end - start);

            // Add filler rows to take up remaining space
            for (int i = 0; i < remainingRows; i++) {
                TableRow fillerRow = new TableRow(this);
                fillerRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        0, 1f // Set the weight to 1 to evenly distribute space
                ));

                // Add empty or invisible TextViews to the filler row
                TextView emptyView = new TextView(this);
                emptyView.setText("");
                fillerRow.addView(emptyView);

                tableLayout.addView(fillerRow);
            }

            // Handle button visibility
            previousPageButton.setEnabled(currentPage > 0);
            nextPageButton.setEnabled(end < allSchedules.size());

            // Update the page indicator
            pageIndicator.setText(String.format("Page %d", currentPage + 1));
        }
    }
}
