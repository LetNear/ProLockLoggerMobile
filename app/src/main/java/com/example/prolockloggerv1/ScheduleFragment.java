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
    private Button nextPageButton, previousPageButton, backButton, searchButton;
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
        boolean isSignedIn = sharedPreferences.getBoolean("is_signed_in", false); // Retrieve sign-in status
        String userEmail = sharedPreferences.getString("user_email", ""); // Retrieve user email

        userNameTextView = rootView.findViewById(R.id.user_name);
        userNameTextView.setText("Welcome, " + userName);

        tableLayout = rootView.findViewById(R.id.tableLayout);
        nextPageButton = rootView.findViewById(R.id.nextPageButton);
        previousPageButton = rootView.findViewById(R.id.previousPageButton);
        pageIndicator = rootView.findViewById(R.id.pageIndicator);
        backButton = rootView.findViewById(R.id.backButton);
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

        // Only load schedules if the user is signed in
        if (isSignedIn) {
            handler.post(refreshRunnable);
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

        backButton.setOnClickListener(v -> getActivity().onBackPressed());

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
            loadSchedules();
            handler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private void loadSchedules() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", "");

        Call<List<Schedule>> call = scheduleApi.getSchedulesByEmail(userEmail);
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
            } else if (schedule.getSubjectName() != null && schedule.getSubjectName().toLowerCase().contains(query)) {
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
        if (tableLayout.getChildCount() > 1) {
            tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
        }

        if (filteredSchedules.isEmpty()) {
            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            TextView noDataTextView = new TextView(getActivity());
            noDataTextView.setText("No schedules yet!");
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
        } else {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, filteredSchedules.size());

            for (int i = start; i < end; i++) {
                Schedule schedule = filteredSchedules.get(i);
                TableRow row = new TableRow(getActivity());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                // Create LayoutParams with weight for each TextView
                TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1.0f
                );

                TextView idTextView = new TextView(getActivity());
                idTextView.setText(String.valueOf(schedule.getId()));
                idTextView.setPadding(8, 8, 8, 8);
                idTextView.setLayoutParams(textViewParams);

                TextView subjectTextView = new TextView(getActivity());
                subjectTextView.setText(schedule.getSubjectName() != null ? schedule.getSubjectName() : "N/A");
                subjectTextView.setPadding(8, 8, 8, 8);
                subjectTextView.setLayoutParams(textViewParams);

                TextView timeTextView = new TextView(getActivity());
                String classTime = (schedule.getDayOfTheWeek() != null ? schedule.getDayOfTheWeek() : "N/A") + ": " +
                        (schedule.getClassStart() != null ? schedule.getClassStart() : "N/A") + " - " +
                        (schedule.getClassEnd() != null ? schedule.getClassEnd() : "N/A");
                timeTextView.setText(classTime);
                timeTextView.setPadding(8, 8, 8, 8);
                timeTextView.setLayoutParams(textViewParams);



                TextView blockYearTextView = new TextView(getActivity());

                // Ensure both block and year are not null
                String block = schedule.getBlock() != null ? schedule.getBlock().getBlock() : "N/A";
                String year = schedule.getYear() != null ? schedule.getYear() : "N/A";
                blockYearTextView.setText(block + "-" + year); // Concatenate block and year

                blockYearTextView.setPadding(8, 8, 8, 8);
                blockYearTextView.setLayoutParams(textViewParams);

                row.addView(idTextView);
                row.addView(subjectTextView);
                row.addView(timeTextView);

                row.addView(blockYearTextView);

                tableLayout.addView(row);
            }

            previousPageButton.setEnabled(currentPage > 0);
            nextPageButton.setEnabled(currentPage < getMaxPage());

            pageIndicator.setText("Page " + (currentPage + 1) + " of " + (getMaxPage() + 1));
        }
    }





    private int getMaxPage() {
        return (int) Math.floor((double) filteredSchedules.size() / pageSize) - 1;
    }
}
