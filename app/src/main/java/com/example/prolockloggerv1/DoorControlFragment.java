package com.example.prolockloggerv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDoorControlBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Set up button listeners
        binding.openDoorButton.setOnClickListener(v -> controlDoor("open"));
        binding.closeDoorButton.setOnClickListener(v -> controlDoor("close"));

        return binding.getRoot();
    }

    private void controlDoor(String action) {
        // Assuming action is either "open" or "close"
        Call<DoorControlResponse> call;
        if (action.equals("open")) {
            call = apiService.openDoor();  // Define this in ApiService
        } else {
            call = apiService.closeDoor(); // Define this in ApiService
        }

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
}
