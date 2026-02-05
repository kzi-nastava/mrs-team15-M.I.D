package com.example.ridenow.ui.upcoming;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.ride.CancelRideRequestDTO;
import com.example.ridenow.dto.ride.UpcomingRideResponse;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.service.RideService;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpcomingRidesFragment extends Fragment {

    private static final String TAG = "UpcomingRidesFragment";

    private ProgressBar progressBar;
    private TextView tvNoRides;
    private LinearLayout ridesContainer;
    private DriverService driverService;

    private RideService rideService;
    private boolean isUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        driverService = ClientUtils.getClient(DriverService.class);
        rideService = ClientUtils.getClient(RideService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upcoming_rides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        if(!isUser){
            loadDriverUpcomingRides();
        }
        else{
            loadUserUpcomingRides();
        }
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        tvNoRides = view.findViewById(R.id.tvNoRides);
        ridesContainer = view.findViewById(R.id.ridesContainer);
        TokenUtils tokenUtils = ClientUtils.getTokenUtils();
        String userRole = tokenUtils.getRole();
        isUser = "USER".equals(userRole);
    }

    private void loadDriverUpcomingRides() {
        showLoading(true);

        Call<List<UpcomingRideResponse>> call = driverService.getUpcomingRides();
        call.enqueue(new Callback<List<UpcomingRideResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<UpcomingRideResponse>> call, @NonNull Response<List<UpcomingRideResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<UpcomingRideResponse> rides = response.body();
                    displayRides(rides);
                } else {
                    Log.e(TAG, "Failed to load upcoming rides: " + response.code());
                    showError("Failed to load upcoming rides");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UpcomingRideResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error loading upcoming rides", t);
                showError("Network error. Please check your connection.");
            }
        });
    }
    private void loadUserUpcomingRides() {
        showLoading(true);

        Call<List<UpcomingRideResponse>> call = rideService.getUpcomingRides();
        call.enqueue(new Callback<List<UpcomingRideResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<UpcomingRideResponse>> call, @NonNull Response<List<UpcomingRideResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<UpcomingRideResponse> rides = response.body();
                    displayRides(rides);
                } else {
                    Log.e(TAG, "Failed to load upcoming rides: " + response.code());
                    showError("Failed to load upcoming rides");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UpcomingRideResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error loading upcoming rides", t);
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void displayRides(List<UpcomingRideResponse> rides) {
        ridesContainer.removeAllViews();

        if (rides == null || rides.isEmpty()) {
            showNoRides(true);
            showRidesContainer(false);
            return;
        }

        showNoRides(false);
        showRidesContainer(true);

        for (UpcomingRideResponse ride : rides) {
            View rideCard = createRideCard(ride);
            ridesContainer.addView(rideCard);
        }
    }

    private View createRideCard(UpcomingRideResponse ride) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View cardView = inflater.inflate(R.layout.item_upcoming_ride, ridesContainer, false);

        TextView tvRoute = cardView.findViewById(R.id.tvRoute);
        TextView tvStartTime = cardView.findViewById(R.id.tvStartTime);
        TextView tvPassengers = cardView.findViewById(R.id.tvPassengers);
        Button btnCancel = cardView.findViewById(R.id.btnCancel);
        Button btnStart = cardView.findViewById(R.id.btnStart);

        // Set route with formatted addresses
        String formattedRoute = formatRoute(ride.getRoute());
        tvRoute.setText(formattedRoute);

        // Set formatted start time
        String formattedTime = formatStartTime(ride.getStartTime());
        tvStartTime.setText(formattedTime);

        // Set passengers
        String passengerText = ride.getPassengers() != null && !ride.getPassengers().trim().isEmpty()
            ? ride.getPassengers()
            : "No passengers assigned";
        tvPassengers.setText(passengerText);

        // Show cancel button only if cancellable
        if (ride.isCanCancel()) {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(v -> handleCancelRide(ride));
        } else {
            btnCancel.setVisibility(View.GONE);
        }

        if(isUser){
            btnStart.setVisibility(View.GONE);
        }

        // Set start button click listener
        btnStart.setOnClickListener(v -> handleStartRide(ride));

        return cardView;
    }

    private String formatRoute(String route) {
        if (route == null || route.trim().isEmpty()) {
            return "Route not available";
        }

        // Split by arrow if present
        String[] parts = route.split("→");
        if (parts.length == 2) {
            String start = AddressUtils.formatAddress(parts[0].trim());
            String end = AddressUtils.formatAddress(parts[1].trim());
            return start + " → " + end;
        }

        return route;
    }

    private String formatStartTime(String startTime) {
        if (startTime == null || startTime.trim().isEmpty()) {
            return "Time not available";
        }

        try {
            // Try to parse the date and format it nicely
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(startTime);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse date: " + startTime, e);
        }

        return startTime;
    }

    private void handleCancelRide(UpcomingRideResponse ride) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_ride, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextInputEditText etCancelReason = dialogView.findViewById(R.id.etCancelReason);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        btnCancel.setOnClickListener(v -> {dialog.dismiss();});
        btnConfirm.setOnClickListener(v -> {
            String reason = etCancelReason.getText() == null ? "" : etCancelReason.getText().toString().trim();
            if(!isUser && reason.isEmpty()){
                etCancelReason.setError("Please provide a reason");
                Toast.makeText(requireContext(),"Please provide a reason" , Toast.LENGTH_LONG).show();

                return;
            }
            dialog.dismiss();
            performCancelRide(ride.getId(), reason);
        });
        dialog.show();
    }

    private void performCancelRide(Long id, String reason) {
        showLoading(true);
        CancelRideRequestDTO dto = new CancelRideRequestDTO(reason);
        rideService.cancel(id, dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if(response.isSuccessful()){
                    Toast.makeText(requireContext(), "Ride cancelled successfully", Toast.LENGTH_SHORT).show();
                    if (!isUser) {
                        loadDriverUpcomingRides();
                    } else {
                        loadUserUpcomingRides();
                    }
                }
                else{
                    String errorMessage = "Ride cancellation failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMessage = errorBody;
                            if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                                errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                            }
                        }
                    } catch (Exception e) {
                        errorMessage = "Ride cancellation failed (code: " + response.code() + ")";
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(),"Error: " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleStartRide(UpcomingRideResponse ride) {
        // TODO: Implement start ride functionality
        Toast.makeText(requireContext(), "Start ride functionality will be implemented", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Start ride requested for ID: " + ride.getId());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showNoRides(boolean show) {
        tvNoRides.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showRidesContainer(boolean show) {
        ridesContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        showNoRides(true);
        showRidesContainer(false);
    }
}
