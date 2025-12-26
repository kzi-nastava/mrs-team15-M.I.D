package com.example.ridenow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class RideDetailsFragment extends Fragment {
    private static final String ARG_RIDE_DATA = "ride_data";
    private String[] rideData;

    public static RideDetailsFragment newInstance(String[] rideData) {
        RideDetailsFragment fragment = new RideDetailsFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_RIDE_DATA, rideData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rideData = getArguments().getStringArray(ARG_RIDE_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (rideData != null) {
            populateRideDetails(view);
        }
    }

    private void populateRideDetails(View view) {
        TextView tvRoute = view.findViewById(R.id.tvDetailRoute);
        TextView tvPassengers = view.findViewById(R.id.tvDetailPassengers);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvDuration = view.findViewById(R.id.tvDetailDuration);
        TextView tvCancelled = view.findViewById(R.id.tvDetailCancelled);
        TextView tvCost = view.findViewById(R.id.tvDetailCost);
        TextView tvPanicButton = view.findViewById(R.id.tvDetailPanicButton);
        TextView tvRating = view.findViewById(R.id.tvDetailRating);
        TextView tvInconsistencies = view.findViewById(R.id.tvDetailInconsistencies);

        tvRoute.setText("Route: " + rideData[0]);
        tvPassengers.setText("Passengers: " + rideData[1]);
        tvDate.setText("Date: " + rideData[2]);
        tvDuration.setText("Duration: " + rideData[3]);
        tvCancelled.setText("Cancelled: " + (rideData[5] == null ? "N/A" : rideData[5]));
        tvCost.setText("Cost: " + rideData[7]);
        tvPanicButton.setText("Panic Button: " + (rideData[8] == null ? "N/A" : rideData[8]));

        // Mock data for rating and inconsistencies
        tvRating.setText("Rating: " + rideData[10]);
        tvInconsistencies.setText("Inconsistencies: " + (rideData[11] == null ? "N/A" : rideData[11]));
    }
}
