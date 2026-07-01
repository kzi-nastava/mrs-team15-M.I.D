package com.example.ridenow.ui.ride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.ridenow.dto.user.UserResponseDTO;
import com.example.ridenow.R;
import com.example.ridenow.service.AdminService;
import com.example.ridenow.util.ClientUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindingDriverFragment extends Fragment {

    private LinearLayout layoutSearching, layoutFound, layoutNotFound;
    private TextView tvSearchingPickup, tvSearchingDestination, tvDriverName, tvDriverVehicle, tvDriverEta;
    private Button btnCancelSearch, btnAcceptAndBack, btnGoToOrdering;
    private AdminService adminService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finding_driver, container, false);

        adminService = ClientUtils.getClient(AdminService.class);

        // Initialize state containers
        layoutSearching = view.findViewById(R.id.layoutSearching);
        layoutFound = view.findViewById(R.id.layoutFound);
        layoutNotFound = view.findViewById(R.id.layoutNotFound);

        // Initialize dynamic text views
        tvSearchingPickup = view.findViewById(R.id.tvSearchingPickup);
        tvSearchingDestination = view.findViewById(R.id.tvSearchingDestination);
        tvDriverName = view.findViewById(R.id.tvDriverName);
        tvDriverVehicle = view.findViewById(R.id.tvDriverVehicle);
        tvDriverEta = view.findViewById(R.id.tvDriverEta);

        // Initialize actions
        btnCancelSearch = view.findViewById(R.id.btnCancelSearch);
        btnAcceptAndBack = view.findViewById(R.id.btnAcceptAndBack);
        btnGoToOrdering = view.findViewById(R.id.btnGoToOrdering);

        // Extract parameters passed from RidePreferenceFragment
        if (getArguments() != null) {
            String pickup = getArguments().getString("pickupAddress", "");
            String destination = getArguments().getString("destinationAddress", "");
            tvSearchingPickup.setText("Pickup: " + pickup);
            tvSearchingDestination.setText("Destination: " + destination);
            renderInitialStateFromArguments();
        } else {
            showSearchingState();
        }

        // Setup routing bindings
        btnCancelSearch.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.ride_ordering));
        btnGoToOrdering.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.ride_ordering));
        btnAcceptAndBack.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.ride_ordering));

        return view;
    }

    /**
     * Updates the active view layout depending on backend synchronization lifecycle metrics.
     * Use this method when a WebSocket message is received.
     */
    public void updateState(@NonNull String state, @Nullable String driverName, @Nullable String vehicleInfo, int eta) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            renderState(state, driverName, vehicleInfo, eta);
        });
    }

    private void renderInitialStateFromArguments() {
        Bundle args = getArguments();
        if (args == null) {
            showSearchingState();
            return;
        }

        long driverId = args.getLong("driverId", -1L);
        String rideStatus = args.getString("rideStatus", "REQUESTED");
        String rejectionReason = args.getString("rejectionReason", "");
        int eta = args.getInt("eta", 0);
        String vehicleType = args.getString("vehicleType", "");

        if ((rejectionReason != null && !rejectionReason.isEmpty()) || "CANCELLED".equalsIgnoreCase(rideStatus)) {
            renderState("notfound", null, null, 0);
            tvDriverName.setText(rejectionReason != null && !rejectionReason.isEmpty()
                    ? rejectionReason
                    : "No driver could be assigned");
            tvDriverVehicle.setText("");
            tvDriverEta.setText("");
            return;
        }

        if (driverId > 0) {
            loadDriverDetails(driverId, eta);
            return;
        }

        renderState("searching", null, null, 0);
    }

    private void loadDriverDetails(long driverId, int eta) {
        adminService.getUserById(driverId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDTO> call, @NonNull Response<UserResponseDTO> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO driver = response.body();
                    String driverName = formatDriverName(driver, driverId);
                    String vehicleInfo = formatVehicleInfo(driver);
                    renderState("found", driverName, vehicleInfo, eta);
                } else {
                    renderState("found", "Driver #" + driverId, "Assigned ride", eta);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }

                renderState("found", "Driver #" + driverId, "Assigned ride", eta);
            }
        });
    }

    private String formatDriverName(@Nullable UserResponseDTO driver, long driverId) {
        if (driver == null) {
            return "Driver #" + driverId;
        }

        String firstName = driver.getFirstName() == null ? "" : driver.getFirstName().trim();
        String lastName = driver.getLastName() == null ? "" : driver.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "Driver #" + driverId : fullName;
    }

    private String formatVehicleInfo(@Nullable UserResponseDTO driver) {
        if (driver == null) {
            return "";
        }

        String licensePlate = driver.getLicensePlate() == null ? "" : driver.getLicensePlate().trim();
        String vehicleModel = driver.getVehicleModel() == null ? "" : driver.getVehicleModel().trim();

        if (licensePlate.isEmpty() && vehicleModel.isEmpty()) {
            return "Assigned ride";
        }
        if (licensePlate.isEmpty()) {
            return "Vehicle model: " + vehicleModel;
        }
        if (vehicleModel.isEmpty()) {
            return "License plate: " + licensePlate;
        }
        return "License plate: " + licensePlate + "\nVehicle model: " + vehicleModel;
    }

    private void showSearchingState() {
        renderState("searching", null, null, 0);
    }

    private void renderState(@NonNull String state, @Nullable String driverName, @Nullable String vehicleInfo, int eta) {
        layoutSearching.setVisibility(View.GONE);
        layoutFound.setVisibility(View.GONE);
        layoutNotFound.setVisibility(View.GONE);

        switch (state.toLowerCase()) {
            case "found":
                layoutFound.setVisibility(View.VISIBLE);
                tvDriverName.setText(driverName != null ? driverName : "");
                tvDriverVehicle.setText(vehicleInfo != null ? vehicleInfo : "");
                tvDriverEta.setText(eta > 0 ? "ETA: " + eta + " min" : "ETA: --");
                break;
            case "notfound":
                layoutNotFound.setVisibility(View.VISIBLE);
                tvDriverName.setText("");
                tvDriverVehicle.setText("");
                tvDriverEta.setText("");
                break;
            case "searching":
            default:
                layoutSearching.setVisibility(View.VISIBLE);
                tvDriverName.setText("");
                tvDriverVehicle.setText("");
                tvDriverEta.setText("");
                break;
        }
    }
}