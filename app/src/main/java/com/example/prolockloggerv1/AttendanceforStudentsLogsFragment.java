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

public class AttendanceforStudentsLogsFragment extends Fragment {

    private TableLayout tableLayout;
    private Button nextPageButton, previousPageButton;
    private TextView pageIndicator, userNameTextView;
    private List<AttendanceResponse.AttendanceLog> allLogs;
    private List<AttendanceResponse.AttendanceLog> filteredLogs;
    private int currentPage = 0;
    private int pageSize = 11;
    private static final int REFRESH_INTERVAL_MS = 3000; // Refresh every 3 seconds
    private Handler handler = new Handler();
    private AttendanceApi attendanceApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attendancefor_students_logs, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "Guest");
        String userEmail = sharedPreferences.getString("user_email", "");

        userNameTextView = rootView.findViewById(R.id.user_name);
        userNameTextView.setText(userName);

        tableLayout = rootView.findViewById(R.id.tableLayout);
        nextPageButton = rootView.findViewById(R.id.nextPageButton);
        previousPageButton = rootView.findViewById(R.id.previousPageButton);
        pageIndicator = rootView.findViewById(R.id.pageIndicator);

        allLogs = new ArrayList<>();
        filteredLogs = new ArrayList<>();

        // Initialize Retrofit and AttendanceApi
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://prolocklogger.pro/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        attendanceApi = retrofit.create(AttendanceApi.class);

        // Start refreshing data at 3-second intervals
        handler.post(refreshRunnable);

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

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable); // Stop refreshing when fragment is destroyed
    }

    // Runnable to refresh the data every 3 seconds
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
            String userEmail = sharedPreferences.getString("user_email", "");
            loadAttendanceLogs(userEmail); // Fetch the latest data
            handler.postDelayed(this, REFRESH_INTERVAL_MS); // Schedule the next refresh
        }
    };

    private void loadAttendanceLogs(String userEmail) {
        Call<AttendanceResponse> call = attendanceApi.getAttendanceLogsByInstructor(userEmail);

        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allLogs.clear();
                    allLogs.addAll(response.body().getAttendanceLogs());

                    filteredLogs.clear();
                    filteredLogs.addAll(allLogs);

                    displayPage(currentPage);
                } else {
                    Log.d("AttendanceFragment", "Failed to load logs. Response code: " + response.code());
                    Toast.makeText(getActivity(), "No logs found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Log.e("AttendanceFragment", "Error fetching data", t);
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPage(int page) {
        // Clear existing rows (except the header row)
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        if (filteredLogs.isEmpty()) {
            TableRow row = new TableRow(requireContext());
            TextView noDataTextView = new TextView(requireContext());
            noDataTextView.setText("No logs yet!");
            noDataTextView.setPadding(16, 16, 16, 16);
            row.addView(noDataTextView);
            tableLayout.addView(row);
        } else {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, filteredLogs.size());

            for (int i = start; i < end; i++) {
                AttendanceResponse.AttendanceLog log = filteredLogs.get(i);

                TableRow row = new TableRow(requireContext());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                // Student Name
                TextView studentNameView = new TextView(requireContext());
                studentNameView.setText(log.getStudentName());
                studentNameView.setPadding(16, 16, 16, 16); // Add padding
                studentNameView.setGravity(Gravity.CENTER); // Center the text
                studentNameView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
                row.addView(studentNameView);

                // Course Name
                TextView courseNameView = new TextView(requireContext());
                courseNameView.setText(log.getCourseName());
                courseNameView.setPadding(16, 16, 16, 16); // Add padding
                courseNameView.setGravity(Gravity.CENTER); // Center the text
                courseNameView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
                row.addView(courseNameView);

                // Time In - Time Out
                TextView timeInOutView = new TextView(requireContext());
                timeInOutView.setText(log.getTimeIn() + " - " + log.getTimeOut());
                timeInOutView.setPadding(16, 16, 16, 16); // Add padding
                timeInOutView.setGravity(Gravity.CENTER); // Center the text
                timeInOutView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f)); // Larger weight for time
                row.addView(timeInOutView);

                // Status
                TextView statusView = new TextView(requireContext());
                statusView.setText(log.getStatus());
                statusView.setPadding(16, 16, 16, 16); // Add padding
                statusView.setGravity(Gravity.CENTER); // Center the text
                statusView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f));
                row.addView(statusView);

                tableLayout.addView(row);
            }

            previousPageButton.setEnabled(currentPage > 0);
            nextPageButton.setEnabled(end < filteredLogs.size());
            pageIndicator.setText(String.format("Page %d", currentPage + 1));
        }
    }

    private int getMaxPage() {
        return (int) Math.ceil((double) filteredLogs.size() / pageSize) - 1;
    }
}
