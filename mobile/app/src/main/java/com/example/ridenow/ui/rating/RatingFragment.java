package com.example.ridenow.ui.rating;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.rating.RatingRequestDTO;
import com.example.ridenow.dto.rating.RatingResponseDTO;
import com.example.ridenow.service.RideService;
import com.example.ridenow.util.ClientUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatingFragment extends Fragment {

    private static final String ARG_RIDE_ID = "rideId";

    private RatingBar driverRatingBar;
    private RatingBar vehicleRatingBar;
    private EditText etDriverComment;
    private EditText etVehicleComment;
    private Button btnSubmitRating;

    private String rideId;
    private RideService rideService;

    public static RatingFragment newInstance(String rideId) {
        RatingFragment fragment = new RatingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RIDE_ID, rideId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rideId = getArguments().getString(ARG_RIDE_ID);
        }
        rideService = ClientUtils.getClient(RideService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        // Initialize views
        driverRatingBar = view.findViewById(R.id.driverRatingBar);
        vehicleRatingBar = view.findViewById(R.id.vehicleRatingBar);
        etDriverComment = view.findViewById(R.id.etDriverComment);
        etVehicleComment = view.findViewById(R.id.etVehicleComment);
        btnSubmitRating = view.findViewById(R.id.btnSubmitRating);

        setupRatingBars();
        setupSubmitButton();

        return view;
    }

    private void setupRatingBars() {
        // Set up driver rating bar
        driverRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> validateSubmitButton());

        // Set up vehicle rating bar
        vehicleRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> validateSubmitButton());
    }

    private void setupSubmitButton() {
        btnSubmitRating.setOnClickListener(v -> submitRating());
        validateSubmitButton(); // Initial state
    }

    private void validateSubmitButton() {
        boolean driverRated = driverRatingBar.getRating() > 0;
        boolean vehicleRated = vehicleRatingBar.getRating() > 0;

        btnSubmitRating.setEnabled(driverRated && vehicleRated);
    }

    private void submitRating() {
        float driverRating = driverRatingBar.getRating();
        float vehicleRating = vehicleRatingBar.getRating();

        if (driverRating == 0 || vehicleRating == 0) {
            Toast.makeText(getContext(), "Please rate both driver and vehicle", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rideId == null) {
            Toast.makeText(getContext(), "Error: Ride ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String driverComment = etDriverComment.getText().toString().trim();
        String vehicleComment = etVehicleComment.getText().toString().trim();

        // Create rating request
        RatingRequestDTO request = new RatingRequestDTO(
            (int) driverRating,
            (int) vehicleRating,
            driverComment.isEmpty() ? null : driverComment,
            vehicleComment.isEmpty() ? null : vehicleComment
        );

        // Disable button during API call
        btnSubmitRating.setEnabled(false);
        btnSubmitRating.setText("Submitting...");

        // Make API call
        Call<RatingResponseDTO> call = rideService.rateRide(rideId, request);
        call.enqueue(new Callback<RatingResponseDTO>() {
            @Override
            public void onResponse(Call<RatingResponseDTO> call, Response<RatingResponseDTO> response) {
                btnSubmitRating.setEnabled(true);
                btnSubmitRating.setText(R.string.rating_submit);

                // HTTP 201 (Created) is a successful response
                if (response.isSuccessful() || response.code() == 201) {
                    Toast.makeText(getContext(), "Rating submitted successfully!", Toast.LENGTH_LONG).show();
                    clearForm();

                    // Navigate back or close the fragment
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to submit rating. Please try again. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RatingResponseDTO> call, Throwable t) {
                btnSubmitRating.setEnabled(true);
                btnSubmitRating.setText(R.string.rating_submit);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        driverRatingBar.setRating(0);
        vehicleRatingBar.setRating(0);
        etDriverComment.setText("");
        etVehicleComment.setText("");
        validateSubmitButton();
    }
}
