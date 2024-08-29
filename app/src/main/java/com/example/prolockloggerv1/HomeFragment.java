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

    private TextView stat1Value, stat2Value, stat3Value, welcomeText;
    private Button getStartedButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        stat1Value = view.findViewById(R.id.stat1Value);
        stat2Value = view.findViewById(R.id.stat2Value);
        welcomeText = view.findViewById(R.id.welcomeText);

        // Fetch the user name and email from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "");
        String email = sharedPreferences.getString("user_email", "");

        // Update the welcome text
        if (!userName.isEmpty()) {
            welcomeText.setText("Welcome " + userName);
        }

        if (!email.isEmpty()) {
            fetchStudentCount(email);
            fetchScheduleCount(email);
        } else {
            Toast.makeText(getContext(), "No email found in session", Toast.LENGTH_SHORT).show();
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
        Call<StudentCountResponse> call = apiService.getScheduleCountByEmail(email);

        call.enqueue(new Callback<StudentCountResponse>() {
            @Override
            public void onResponse(Call<StudentCountResponse> call, Response<StudentCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stat2Value.setText(String.valueOf(response.body().getScheduleCount()));
                } else {
                    Toast.makeText(getContext(), "Failed to fetch schedule count", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StudentCountResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
