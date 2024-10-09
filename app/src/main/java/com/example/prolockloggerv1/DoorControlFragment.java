package com.example.prolockloggerv1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prolockloggerv1.databinding.FragmentDoorControlBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoorControlFragment extends Fragment {

    private FragmentDoorControlBinding binding;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDoorControlBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);

        // Set up button listeners
        binding.openDoorButton.setOnClickListener(v -> {
            controlDoor("open");
            resetSessionTimerInActivity(); // Reset session timer when "Open Door" button is clicked
        });

        binding.closeDoorButton.setOnClickListener(v -> {
            controlDoor("close");
            resetSessionTimerInActivity(); // Reset session timer when "Close Door" button is clicked
        });

        return binding.getRoot();
    }

    private void controlDoor(String action) {
        // Fetch the email from SharedPreferences
        String email = sharedPreferences.getString("user_email", null);

        if (email == null) {
            Toast.makeText(getActivity(), "No email found in preferences", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<DoorControlResponse> call;

        // Determine which API call to make based on action
        if (action.equals("open")) {
            call = apiService.openDoor(email);
        } else {
            call = apiService.closeDoor(email);
        }

        // Enqueue the call
        call.enqueue(new Callback<DoorControlResponse>() {
            @Override
            public void onResponse(@NonNull Call<DoorControlResponse> call, @NonNull Response<DoorControlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to " + action + " the door", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DoorControlResponse> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to reset session timer in the parent activity
    private void resetSessionTimerInActivity() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).resetTimerFromFragment();
        }
    }
}
