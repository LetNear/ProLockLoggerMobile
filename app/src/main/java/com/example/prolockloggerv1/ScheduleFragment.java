package com.example.prolockloggerv1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private Button nextPageButton, previousPageButton, backButton;
    private TextView pageIndicator, userNameTextView;
    private List<Schedule> allSchedules;
    private int currentPage = 0;
    private int pageSize = 15;
    private static final int REFRESH_INTERVAL_MS = 5000;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "Guest");
        boolean isSignedIn = sharedPreferences.getBoolean("is_signed_in", false); // Retrieve sign-in status

        userNameTextView = rootView.findViewById(R.id.user_name);
        userNameTextView.setText("Welcome, " + userName);

        tableLayout = rootView.findViewById(R.id.tableLayout);
        nextPageButton = rootView.findViewById(R.id.nextPageButton);
        previousPageButton = rootView.findViewById(R.id.previousPageButton);
        pageIndicator = rootView.findViewById(R.id.pageIndicator);
        backButton = rootView.findViewById(R.id.backButton);

        allSchedules = new ArrayList<>();

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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://prolocklogger.pro/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ScheduleApi scheduleApi = retrofit.create(ScheduleApi.class);

        Call<List<Schedule>> call = scheduleApi.getSchedules();
        call.enqueue(new Callback<List<Schedule>>() {
            @Override
            public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allSchedules = response.body();
                    Log.d("ScheduleFragment", "Schedules fetched: " + allSchedules.size());
                    displayPage(currentPage);
                } else {
                    Log.d("ScheduleFragment", "Failed to load schedules. Response code: " + response.code());
                    Toast.makeText(getActivity(), "Failed to load schedules", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Schedule>> call, Throwable t) {
                Log.e("ScheduleFragment", "Error fetching data", t);
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNotSignedInMessage() {
        // Clear existing rows (except the header)
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

        // Disable page navigation buttons
        previousPageButton.setEnabled(false);
        nextPageButton.setEnabled(false);

        // Update the page indicator
        pageIndicator.setText("Page 1");
    }

    private void displayPage(int page) {
        // Clear existing rows (except the header)
        if (tableLayout.getChildCount() > 1) {
            tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
        }

        if (allSchedules.isEmpty()) {
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
            int end = Math.min(start + pageSize, allSchedules.size());

            for (int i = start; i < end; i++) {
                Schedule schedule = allSchedules.get(i);

                TableRow row = new TableRow(getActivity());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                TextView id = new TextView(getActivity());
                id.setText(String.valueOf(schedule.getId()));
                id.setPadding(8, 8, 8, 8);
                id.setGravity(android.view.Gravity.CENTER);
                id.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(id);

                TextView course = new TextView(getActivity());
                course.setText(schedule.getSubjectName());
                course.setPadding(8, 8, 8, 8);
                course.setGravity(android.view.Gravity.CENTER);
                course.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(course);

                TextView timeAndDay = new TextView(getActivity());
                timeAndDay.setText(schedule.getDayOfTheWeek() + " " + schedule.getClassStart() + " - " + schedule.getClassEnd());
                timeAndDay.setPadding(8, 8, 8, 8);
                timeAndDay.setGravity(android.view.Gravity.CENTER);
                timeAndDay.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(timeAndDay);

                TextView block = new TextView(getActivity());
                block.setText(String.valueOf(schedule.getBlockId()));
                block.setPadding(8, 8, 8, 8);
                block.setGravity(android.view.Gravity.CENTER);
                block.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(block);

                tableLayout.addView(row);
            }

            // Add filler rows if needed to reach pageSize
            int remainingRows = pageSize - (end - start);
            for (int i = 0; i < remainingRows; i++) {
                TableRow fillerRow = new TableRow(getActivity());
                fillerRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                TextView fillerCell = new TextView(getActivity());
                fillerCell.setText(" ");
                fillerCell.setPadding(8, 8, 8, 8);
                fillerCell.setGravity(android.view.Gravity.CENTER);
                fillerCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
                fillerRow.addView(fillerCell);

                tableLayout.addView(fillerRow);
            }

            // Update page indicator and navigation buttons
            previousPageButton.setEnabled(page > 0);
            nextPageButton.setEnabled(end < allSchedules.size());
            pageIndicator.setText("Page " + (page + 1));
        }
    }

    private int getMaxPage() {
        return (allSchedules.size() - 1) / pageSize;
    }
}
