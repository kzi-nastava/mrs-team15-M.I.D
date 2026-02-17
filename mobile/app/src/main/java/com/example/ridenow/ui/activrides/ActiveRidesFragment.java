package com.example.ridenow.ui.activrides;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.ride.ActiveRideDTO;
import com.example.ridenow.service.RideService;
import com.example.ridenow.util.ClientUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveRidesFragment extends Fragment {

    private EditText searchEditText;
    private LinearLayout ridesContainer;
    private List<ActiveRideDTO> allRides;
    private List<ActiveRideDTO> filteredRides;
    private RideService rideService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_rides, container, false);

        initializeViews(view);
        setupRetrofit();
        setupSearchFunctionality();
        loadActiveRides();

        return view;
    }

    private void initializeViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        ridesContainer = view.findViewById(R.id.ridesContainer);
        allRides = new ArrayList<>();
        filteredRides = new ArrayList<>();
    }

    private void setupRetrofit() {
        rideService = ClientUtils.getClient(RideService.class);
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRides(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadActiveRides() {
        Call<List<ActiveRideDTO>> call = rideService.getActiveRides();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ActiveRideDTO>> call,
                                   @NonNull Response<List<ActiveRideDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRides = response.body();
                    filteredRides = new ArrayList<>(allRides);
                    displayRides();
                } else {
                    showError("Failed to load active rides");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ActiveRideDTO>> call, @NonNull Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void filterRides(String searchText) {
        if (searchText.isEmpty()) {
            filteredRides = new ArrayList<>(allRides);
        } else {
            filteredRides = new ArrayList<>();
            for (ActiveRideDTO ride : allRides) {
                if (ride.getDriverName() != null &&
                    ride.getDriverName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredRides.add(ride);
                }
            }
        }
        displayRides();
    }

    private void displayRides() {
        ridesContainer.removeAllViews();

        if (filteredRides.isEmpty()) {
            View emptyView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_empty_state, ridesContainer, false);
            ridesContainer.addView(emptyView);
            return;
        }

        for (ActiveRideDTO ride : filteredRides) {
            View rideCard = createRideCard(ride);
            ridesContainer.addView(rideCard);
        }
    }

    private View createRideCard(ActiveRideDTO ride) {
        View cardView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_active_ride_card, ridesContainer, false);

        // Initialize card with ride data
        ActiveRideCardHelper.setupCard(cardView, ride);

        // Make card clickable and navigate to current ride page
        cardView.setOnClickListener(v -> {
            if (getActivity() != null) {
                Bundle bundle = new Bundle();
                bundle.putLong("rideId", ride.getRideId());
                bundle.putBoolean("isAdminView", true);

                // Pass driver and passengers information
                String driverName = ride.getDriverName();
                String passengers = ride.getPassengerNames();

                if (driverName != null && !driverName.trim().isEmpty()) {
                    bundle.putString("driverName", driverName);
                } else {
                    bundle.putString("driverName", "Unknown Driver");
                }

                if (passengers != null && !passengers.trim().isEmpty()) {
                    bundle.putString("passengers", passengers);
                } else {
                    bundle.putString("passengers", "");
                }

                // Pass complete RouteDTO object
                if (ride.getRoute() != null) {
                    bundle.putSerializable("routeDTO", ride.getRoute());
                }

                androidx.navigation.NavController navController =
                    androidx.navigation.Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.current_ride, bundle);
            }
        });

        return cardView;
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
