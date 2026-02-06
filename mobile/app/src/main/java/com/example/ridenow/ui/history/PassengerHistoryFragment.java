package com.example.ridenow.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ridenow.R;
import com.example.ridenow.dto.passenger.RideHistoryItemDTO;
import com.example.ridenow.service.PassengerService;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.DateUtils;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerHistoryFragment extends Fragment {

    private LinearLayout cardsContainer;
    private PassengerService passengerService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_history, container, false);

        try {
            passengerService = ClientUtils.getClient(PassengerService.class);
            cardsContainer = view.findViewById(R.id.cardsContainer);

            loadPassengerHistory();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error initializing page: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void loadPassengerHistory() {
        if (passengerService == null) {
            Toast.makeText(getContext(), "Service not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<RideHistoryItemDTO>> call = passengerService.getPassengerRideHistory();

        call.enqueue(new Callback<List<RideHistoryItemDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<RideHistoryItemDTO>> call, @NonNull Response<List<RideHistoryItemDTO>> response) {
                Log.d("PassengerHistory", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("PassengerHistory", "Number of rides: " + response.body().size());
                    try {
                        populatePassengerHistoryCards(response.body());
                    } catch (Exception e) {
                        Log.e("PassengerHistory", "Error populating cards: " + e.getMessage(), e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error displaying rides: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.w("PassengerHistory", "Response not successful or body is null");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load passenger history", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RideHistoryItemDTO>> call, @NonNull Throwable t) {
                Log.e("PassengerHistory", "Network error: " + t.getMessage(), t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populatePassengerHistoryCards(List<RideHistoryItemDTO> rides) {
        Log.d("PassengerHistory", "populatePassengerHistoryCards called with " + rides.size() + " rides");
        if (getContext() == null) {
            Log.w("PassengerHistory", "Context is null, cannot populate cards");
            return;
        }

        cardsContainer.removeAllViews();

        for (int i = 0; i < rides.size(); i++) {
            RideHistoryItemDTO ride = rides.get(i);
            Log.d("PassengerHistory", "Processing ride " + i + ": " + ride.getId());
            try {
                View cardView = createRideCard(ride);
                if (cardView != null) {
                    cardsContainer.addView(cardView);
                    Log.d("PassengerHistory", "Successfully added card for ride " + ride.getId());
                } else {
                    Log.w("PassengerHistory", "Card view is null for ride " + ride.getId());
                }
            } catch (Exception e) {
                Log.e("PassengerHistory", "Error creating card for ride " + ride.getId() + ": " + e.getMessage(), e);
            }
        }
        Log.d("PassengerHistory", "Finished populating cards");
    }

    private View createRideCard(RideHistoryItemDTO ride) {
        if (getContext() == null) return new View(getContext());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.item_ride_card, cardsContainer, false);

        // Populate the card views
        TextView tvRoute = cardView.findViewById(R.id.tvRoute);
        TextView tvDate = cardView.findViewById(R.id.tvDate);
        TextView tvCost = cardView.findViewById(R.id.tvCost);
        TextView tvPassengers = cardView.findViewById(R.id.tvPassengers);
        TextView tvDuration = cardView.findViewById(R.id.tvDuration);
        TextView tvTimeRange = cardView.findViewById(R.id.tvTimeRange);
        LinearLayout statusContainer = cardView.findViewById(R.id.statusContainer);
        Button btnRating = cardView.findViewById(R.id.btnRating);

        // Set route
        String startAddress = ride.getStartAddress() != null ? ride.getStartAddress() : "Unknown";
        String endAddress = ride.getEndAddress() != null ? ride.getEndAddress() : "Unknown";
        String route = startAddress + " → " + endAddress;
        tvRoute.setText(route);

        // Set date
        tvDate.setText(DateUtils.formatDateFromISO(ride.getStartTime()));

        // Set cost
        tvCost.setText(String.format(Locale.getDefault(), "%.0f RSD", ride.getPrice()));

        // Set passengers - for passenger history, this might not be relevant or might show driver
        tvPassengers.setText("Driver assigned");

        // Set duration and time range
        long durationMinutes = DateUtils.calculateDurationMinutes(ride.getStartTime(), ride.getEndTime());
        if (durationMinutes > 0) {
            tvDuration.setText(durationMinutes + " min");
        } else {
            tvDuration.setText("N/A");
        }

        String timeRange = DateUtils.formatTimeRange(ride.getStartTime(), ride.getEndTime());
        tvTimeRange.setText(timeRange);

        // Add status indicators
        if (statusContainer != null) {
            statusContainer.removeAllViews();
            if (ride.isCancelled()) {
                TextView cancelledBadge = createStatusBadge("Cancelled");
                if (cancelledBadge != null) {
                    statusContainer.addView(cancelledBadge);
                }
            }

            if (ride.isPanicTriggered()) {
                TextView panicBadge = createStatusBadge("Panic");
                if (panicBadge != null) {
                    statusContainer.addView(panicBadge);
                }
            }
        }

        // Set up rating button
        if (btnRating != null) {
            btnRating.setVisibility(View.VISIBLE); // Ensure it's visible for passenger history
            btnRating.setOnClickListener(v -> {
                try {
                    NavController navController = Navigation.findNavController(v);
                    Bundle bundle = new Bundle();
                    // Convert Long to String to match RatingFragment expectation
                    bundle.putString("rideId", String.valueOf(ride.getId()));
                    navController.navigate(R.id.rating, bundle);
                } catch (Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Cannot navigate to rating page", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return cardView;
    }

    private TextView createStatusBadge(String text) {
        if (getContext() == null) return null;

        TextView badge = new TextView(getContext());
        badge.setText(text);
        badge.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        badge.setTextSize(12);
        badge.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        badge.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.danger));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, dpToPx(8), 0);
        badge.setLayoutParams(params);

        return badge;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
