package com.example.prolockloggerv1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import java.io.IOException;
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
        Call<List<Schedule>> call;
        if (roleNumber == 2) {
            call = scheduleApi.getSchedulesByEmail(userEmail); // Initial schedule API
        } else if (roleNumber == 3) {
            call = scheduleApi.getAlternativeSchedulesByEmail(userEmail); // Alternative schedule API
        } else {
            call = scheduleApi.getSchedulesByEmail(userEmail); // Fallback to default
        }

        call.enqueue(new Callback<List<Schedule>>() {
            @Override
            public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        allSchedules = response.body();
                        filteredSchedules.clear();
                        filteredSchedules.addAll(allSchedules);
                        Log.d("ScheduleFragment", "Schedules fetched: " + allSchedules.size());
                        displayPage(currentPage);
                    } else {
                        Log.d("ScheduleFragment", "Empty response body.");
                        Toast.makeText(getActivity(), "No schedules found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("ScheduleFragment", "Failed to load schedules. Response code: " + response.code());
                    Log.d("ScheduleFragment", "Response body: " + errorBody);
                    Toast.makeText(getActivity(), "No schedule found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Schedule>> call, Throwable t) {
                Log.e("ScheduleFragment", "Error fetching data", t);
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void performSearch() {
        String query = searchBar.getText().toString().toLowerCase();

        filteredSchedules.clear();
        for (Schedule schedule : allSchedules) {
            boolean matches = false;
            if (String.valueOf(schedule.getId()).toLowerCase().contains(query)) {
                matches = true;
            } else if (schedule.getCourseName() != null && schedule.getCourseName().toLowerCase().contains(query)) {
                matches = true;
            } else if (schedule.getDayOfTheWeek() != null && schedule.getDayOfTheWeek().toLowerCase().contains(query)) {
                matches = true;
            } else if (schedule.getClassStart() != null && schedule.getClassStart().toLowerCase().contains(query)) {
                matches = true;
            } else if (schedule.getClassEnd() != null && schedule.getClassEnd().toLowerCase().contains(query)) {
                matches = true;
            } else if (schedule.getBlock() != null && schedule.getBlock().getBlock().toLowerCase().contains(query)) {
                matches = true;
            }

            if (matches) {
                filteredSchedules.add(schedule);
            }
        }

        currentPage = 0;
        displayPage(currentPage);
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
        // Clear existing rows (except the header)
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        if (allSchedules.isEmpty()) {
            // If there are no schedules, display a message indicating no schedules
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            TextView noDataTextView = new TextView(requireContext());
            noDataTextView.setText("No schedules yet!");
            noDataTextView.setPadding(8, 8, 8, 8);
            noDataTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
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

                TableRow row = new TableRow(requireContext());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                // Create and add the Schedule Id TextView
                TextView id = new TextView(requireContext());
                id.setText(String.valueOf(schedule.getId()));
                id.setPadding(8, 8, 8, 8);
                id.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center text
                id.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(id);

                // Create and add the Course TextView
                TextView course = new TextView(requireContext());
                course.setText(schedule.getCourseName());
                course.setPadding(8, 8, 8, 8);
                course.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center text
                course.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(course);

                // Create and add the Time and Day TextView
                TextView timeAndDay = new TextView(requireContext());
                timeAndDay.setText(schedule.getDayOfTheWeek() + " " + schedule.getClassStart() + " - " + schedule.getClassEnd());
                timeAndDay.setPadding(8, 8, 8, 8);
                timeAndDay.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center text
                timeAndDay.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(timeAndDay);

                // Create and add the Block TextView
                TextView block = new TextView(requireContext());
                block.setText(getBlockLetter(schedule.getBlockId())); // Use the method to convert block ID to letter
                block.setPadding(8, 8, 8, 8);
                block.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center text
                block.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(block);

                // Add the row to the table layout
                tableLayout.addView(row);
            }

            // Calculate remaining rows needed to fill the space
            int remainingRows = pageSize - (end - start);

            // Add filler rows to take up remaining space
            for (int i = 0; i < remainingRows; i++) {
                TableRow fillerRow = new TableRow(requireContext());
                fillerRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        0, 1f // Set the weight to 1 to evenly distribute space
                ));

                // Add empty or invisible TextViews to the filler row
                TextView emptyView = new TextView(requireContext());
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



    private int getMaxPage() {
        return (int) Math.ceil((double) filteredSchedules.size() / pageSize) - 1;
    }

    private String getBlockLetter(int blockId) {
        if (blockId < 1) return "N/A"; // Handle invalid blockId
        return String.valueOf((char) ('A' + blockId - 1)); // Converts 1 to 'A', 2 to 'B', etc.
    }

}
