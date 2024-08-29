package com.example.prolockloggerv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private TextView totalUsers, activeSessions;
    private PieChart statisticsChart;

    private static final String BASE_URL = "https://yourapiendpoint.com/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        totalUsers = view.findViewById(R.id.totalUsers);
        activeSessions = view.findViewById(R.id.activeSessions);
        statisticsChart = view.findViewById(R.id.statisticsChart);

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Assuming you have a StatisticsApi interface for API calls
        StatisticsApi api = retrofit.create(StatisticsApi.class);

        // Fetch data from API
        api.getStatistics().enqueue(new Callback<StatisticsResponse>() {
            @Override
            public void onResponse(Call<StatisticsResponse> call, Response<StatisticsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatisticsResponse stats = response.body();
                    updateUI(stats);
                }
            }

            @Override
            public void onFailure(Call<StatisticsResponse> call, Throwable t) {
                // Handle the error
            }
        });

        return view;
    }

    private void updateUI(StatisticsResponse stats) {
        totalUsers.setText("Total Users: " + stats.getTotalUsers());
        activeSessions.setText("Active Sessions: " + stats.getActiveSessions());

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(stats.getTotalUsers(), "Total Users"));
        entries.add(new PieEntry(stats.getActiveSessions(), "Active Sessions"));
        // Add more entries as needed

        PieDataSet dataSet = new PieDataSet(entries, "Statistics");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);

        statisticsChart.setData(pieData);
        statisticsChart.invalidate(); // Refresh the chart
    }
}
