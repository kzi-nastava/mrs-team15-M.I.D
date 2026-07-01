package com.example.ridenow.ui.ride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;

import com.example.ridenow.dto.model.LocationDTO;
import com.example.ridenow.dto.model.PolylinePointDTO;
import com.example.ridenow.ui.components.RouteMapView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ridenow.R;
import com.example.ridenow.dto.ride.OrderRideRequestDTO;
import com.example.ridenow.dto.ride.OrderRideResponseDTO;
import com.example.ridenow.service.RideService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RidePreferenceFragment extends Fragment {

    private com.example.ridenow.ui.components.RouteMapView routeMapView;
    private boolean isFormRaised = false;
    private View formCard;
    private RideService rideService;

    public RidePreferenceFragment() {

    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_preference, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideService = com.example.ridenow.util.ClientUtils.getClient(RideService.class);

        routeMapView = view.findViewById(R.id.routeMapView);

        if (routeMapView != null) {
            Bundle args = getArguments();
            if (args != null) {
                // Start & End
                LocationDTO start = new LocationDTO(
                        args.getDouble("startLat"),
                        args.getDouble("startLon"),
                        args.getString("startAddress")
                );
                LocationDTO end = new LocationDTO(
                        args.getDouble("endLat"),
                        args.getDouble("endLon"),
                        args.getString("endAddress")
                );

                // Polyline reconstruction
                ArrayList<Double> polylineCoords = (ArrayList<Double>) args.getSerializable("polylineCoords");
                List<PolylinePointDTO> polylinePoints = new ArrayList<>();
                if (polylineCoords != null && polylineCoords.size() % 2 == 0) {
                    for (int i = 0; i < polylineCoords.size(); i += 2) {
                        polylinePoints.add(new PolylinePointDTO(polylineCoords.get(i), polylineCoords.get(i+1)));
                    }
                }

                // Display route with markers
                routeMapView.setShowMarkers(true);
                routeMapView.displayRoute(start, end, null, polylinePoints);
            }
        }


        formCard = view.findViewById(R.id.formCard);

        Spinner vehicleSpinner = view.findViewById(R.id.vehicleSpinner);
        Switch switchPet = view.findViewById(R.id.switchPet);
        Switch switchBaby = view.findViewById(R.id.switchBaby);
        EditText scheduledTime = view.findViewById(R.id.scheduledTime);
        LinearLayout guestsContainer = view.findViewById(R.id.guestsContainer);
        ImageButton addGuestBtn = view.findViewById(R.id.addGuestBtn);
        TextView finalPrice = view.findViewById(R.id.finalPrice);
        Button backBtn = view.findViewById(R.id.backBtn);
        Button orderRideBtn = view.findViewById(R.id.orderRideBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            R.layout.spinner_item_dark,
            new String[]{"Select vehicle type", "Standard", "Luxury", "Van"});
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        vehicleSpinner.setAdapter(adapter);

        vehicleSpinner.setBackgroundResource(R.drawable.edittext_with_bg);
        vehicleSpinner.setSelection(0);

        Bundle args = getArguments();
        bindPricePreview(vehicleSpinner, finalPrice, args);


        addGuestInput(guestsContainer);


        scheduledTime.setFocusable(false);
        scheduledTime.setClickable(true);
        scheduledTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view1, y, m, d) -> {
                final Calendar picked = Calendar.getInstance();
                picked.set(Calendar.YEAR, y);
                picked.set(Calendar.MONTH, m);
                picked.set(Calendar.DAY_OF_MONTH, d);

                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view2, h, min) -> {
                    picked.set(Calendar.HOUR_OF_DAY, h);
                    picked.set(Calendar.MINUTE, min);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    scheduledTime.setText(sdf.format(picked.getTime()));
                }, hour, minute, true);
                timePicker.show();
            }, year, month, day);
            datePicker.show();
        });

        addGuestBtn.setOnClickListener(v -> addGuestInput(guestsContainer));

        backBtn.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        orderRideBtn.setOnClickListener(v -> {
            submitRideOrder(vehicleSpinner, switchPet, switchBaby, scheduledTime, guestsContainer, orderRideBtn);
        });

        if (routeMapView != null && formCard != null) {

            routeMapView.setOnClickListener(v -> {

                int parentHeight = ((View) formCard.getParent()).getHeight();
                int formHeight = formCard.getHeight();
                int visiblePart = dpToPx(100);

                int shiftDown = parentHeight - visiblePart;
                shiftDown = Math.min(shiftDown, formHeight - visiblePart);

                formCard.animate()
                        .translationY(shiftDown)
                        .setDuration(300)
                        .start();

                isFormRaised = true;
            });
        }


        // clicking the card now toggles between raised and lowered
        if (formCard != null) {

            formCard.post(() -> {

                formCard.setOnClickListener(v -> {

                    int parentHeight = ((View) formCard.getParent()).getHeight();
                    int formHeight = formCard.getHeight();


                    int visiblePart = dpToPx(100);


                    int shiftDown = parentHeight - visiblePart;


                    shiftDown = Math.min(shiftDown, formHeight - visiblePart);

                    if (isFormRaised) {

                        formCard.animate()
                                .translationY(0)
                                .setDuration(300)
                                .start();

                        isFormRaised = false;

                    } else {

                        formCard.animate()
                                .translationY(shiftDown)
                                .setDuration(300)
                                .start();

                        isFormRaised = true;
                    }
                });
            });
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root != null) {
            RouteMapView rmv = root.findViewById(R.id.routeMapView);
            if (rmv != null) rmv.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        View root = getView();
        if (root != null) {
            RouteMapView rmv = root.findViewById(R.id.routeMapView);
            if (rmv != null) rmv.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        View root = getView();
        if (root != null) {
            RouteMapView rmv = root.findViewById(R.id.routeMapView);
            if (rmv != null) rmv.onDestroy();
        }
    }

    private void addGuestInput(LinearLayout container) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int topMargin = (int) (8 * requireContext().getResources().getDisplayMetrics().density);
        rowParams.setMargins(0, topMargin, 0, 0);
        row.setLayoutParams(rowParams);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(0, 0, 8, 0);
        EditText guestInput = new EditText(requireContext());
        guestInput.setHint("Enter guest email");
        guestInput.setLayoutParams(lp);

        guestInput.setBackgroundResource(R.drawable.edittext_with_bg);
        guestInput.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        guestInput.setHintTextColor(0x80FFFFFF);

        ImageButton removeBtn = new ImageButton(requireContext());
        removeBtn.setImageResource(R.drawable.ic_trash);
        removeBtn.setBackgroundResource(android.R.color.transparent);
        removeBtn.setContentDescription("Remove guest");
        removeBtn.setOnClickListener(v -> container.removeView(row));

        LinearLayout.LayoutParams removeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        removeLp.setMargins(8, 0, 0, 0);
        removeBtn.setLayoutParams(removeLp);

        row.addView(guestInput);
        row.addView(removeBtn);

        container.addView(row);
    }

    private void submitRideOrder(Spinner vehicleSpinner,
                                  Switch switchPet,
                                  Switch switchBaby,
                                  EditText scheduledTime,
                                  LinearLayout guestsContainer,
                                  Button orderRideBtn) {
        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(requireContext(), "Missing ride route details", Toast.LENGTH_SHORT).show();
            return;
        }

        String startAddress = args.getString("startAddress", "");
        String endAddress = args.getString("endAddress", "");
        double startLatitude = args.getDouble("startLat", Double.NaN);
        double startLongitude = args.getDouble("startLon", Double.NaN);
        double endLatitude = args.getDouble("endLat", Double.NaN);
        double endLongitude = args.getDouble("endLon", Double.NaN);

        if (startAddress.isEmpty() || endAddress.isEmpty() || Double.isNaN(startLatitude) || Double.isNaN(startLongitude)
                || Double.isNaN(endLatitude) || Double.isNaN(endLongitude)) {
            Toast.makeText(requireContext(), "Missing pickup or destination details", Toast.LENGTH_SHORT).show();
            return;
        }

        String vehicleType = normalizeVehicleType(vehicleSpinner.getSelectedItem() != null
                ? vehicleSpinner.getSelectedItem().toString() : null);
        if (vehicleType == null) {
            Toast.makeText(requireContext(), "Please select a vehicle type", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> linkedPassengers = collectGuestEmails(guestsContainer);
        String normalizedScheduledTime = normalizeScheduledTime(scheduledTime.getText().toString());

        ArrayList<Double> routeLatitudes = new ArrayList<>();
        ArrayList<Double> routeLongitudes = new ArrayList<>();
        ArrayList<Double> polylineCoords = extractPolylineCoords(args);
        if (!polylineCoords.isEmpty()) {
            for (int i = 0; i + 1 < polylineCoords.size(); i += 2) {
                routeLatitudes.add(polylineCoords.get(i));
                routeLongitudes.add(polylineCoords.get(i + 1));
            }
        } else {
            routeLatitudes.add(startLatitude);
            routeLatitudes.add(endLatitude);
            routeLongitudes.add(startLongitude);
            routeLongitudes.add(endLongitude);
        }

        if (routeLatitudes.isEmpty() || routeLongitudes.isEmpty()) {
            Toast.makeText(requireContext(), "Route coordinates are missing", Toast.LENGTH_SHORT).show();
            return;
        }

        double distanceKm = args.getDouble("distanceKm", 0d);
        int estimatedTimeMinutes = args.getInt("estimatedTimeMinutes", 0);
        Long favoriteRouteId = args.getLong("favoriteRouteId", -1L);
        if (favoriteRouteId != null && favoriteRouteId < 0) {
            favoriteRouteId = null;
        }

        orderRideBtn.setEnabled(false);
        orderRideBtn.setText("Ordering...");

        double priceEstimate = resolvePriceEstimateFromArgs(args, vehicleType, distanceKm);
        submitOrderRequest(startAddress, startLatitude, startLongitude,
                endAddress, endLatitude, endLongitude,
                vehicleType, switchBaby.isChecked(), switchPet.isChecked(),
                linkedPassengers, normalizedScheduledTime, distanceKm,
                estimatedTimeMinutes, priceEstimate, routeLatitudes, routeLongitudes,
                favoriteRouteId, orderRideBtn);
    }

    private void submitOrderRequest(String startAddress,
                                    double startLatitude,
                                    double startLongitude,
                                    String endAddress,
                                    double endLatitude,
                                    double endLongitude,
                                    String vehicleType,
                                    boolean babyFriendly,
                                    boolean petFriendly,
                                    ArrayList<String> linkedPassengers,
                                    @Nullable String scheduledTime,
                                    double distanceKm,
                                    int estimatedTimeMinutes,
                                    double priceEstimate,
                                    ArrayList<Double> routeLatitudes,
                                    ArrayList<Double> routeLongitudes,
                                    @Nullable Long favoriteRouteId,
                                    Button orderRideBtn) {
        OrderRideRequestDTO request = new OrderRideRequestDTO();
        request.setStartAddress(startAddress);
        request.setStartLatitude(startLatitude);
        request.setStartLongitude(startLongitude);
        request.setEndAddress(endAddress);
        request.setEndLatitude(endLatitude);
        request.setEndLongitude(endLongitude);
        request.setVehicleType(vehicleType);
        request.setBabyFriendly(babyFriendly);
        request.setPetFriendly(petFriendly);
        request.setLinkedPassengers(linkedPassengers);
        request.setScheduledTime(scheduledTime);
        request.setDistanceKm(distanceKm > 0 ? distanceKm : 1d);
        request.setEstimatedTimeMinutes(estimatedTimeMinutes > 0 ? estimatedTimeMinutes : 1);
        request.setPriceEstimate(priceEstimate > 0 ? priceEstimate : 1d);
        request.setRouteLattitudes(routeLatitudes);
        request.setRouteLongitudes(routeLongitudes);
        request.setFavoriteRouteId(favoriteRouteId);

        rideService.orderRide(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<OrderRideResponseDTO> call, @NonNull Response<OrderRideResponseDTO> response) {
                orderRideBtn.setEnabled(true);
                orderRideBtn.setText("Order ride");

                if (response.isSuccessful()) {
                    OrderRideResponseDTO rideResponse = response.body();
                    Toast.makeText(requireContext(), "Ride ordered successfully", Toast.LENGTH_SHORT).show();

                    Bundle nextArgs = new Bundle();
                    nextArgs.putString("pickupAddress", startAddress);
                    nextArgs.putString("destinationAddress", endAddress);
                    if (rideResponse != null) {
                        nextArgs.putLong("rideId", rideResponse.getId() != null ? rideResponse.getId() : -1L);
                        nextArgs.putLong("driverId", rideResponse.getDriverId() != null ? rideResponse.getDriverId() : -1L);
                        nextArgs.putString("rideStatus", rideResponse.getStatus());
                        nextArgs.putString("rejectionReason", rideResponse.getRejectionReason());
                        nextArgs.putInt("eta", rideResponse.getETA());
                        nextArgs.putString("vehicleType", rideResponse.getVehicleType() != null ? rideResponse.getVehicleType().name() : vehicleType);
                    }
                    try {
                        View root = getView();
                        if (root != null) {
                            Navigation.findNavController(root).navigate(R.id.finding_driver_fragment, nextArgs);
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Ride ordered, but navigation failed", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                String message = "Failed to order ride";
                try {
                    if (response.errorBody() != null) {
                        message = response.errorBody().string();
                    }
                } catch (Exception ignored) {
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<OrderRideResponseDTO> call, @NonNull Throwable t) {
                orderRideBtn.setEnabled(true);
                orderRideBtn.setText("Order ride");
                Toast.makeText(requireContext(), "Error ordering ride: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private ArrayList<String> collectGuestEmails(LinearLayout container) {
        ArrayList<String> emails = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View row = container.getChildAt(i);
            if (!(row instanceof LinearLayout)) {
                continue;
            }
            for (int j = 0; j < ((LinearLayout) row).getChildCount(); j++) {
                View child = ((LinearLayout) row).getChildAt(j);
                if (child instanceof EditText) {
                    String email = ((EditText) child).getText().toString().trim();
                    if (!email.isEmpty()) {
                        emails.add(email);
                    }
                    break;
                }
            }
        }
        return emails;
    }

    private ArrayList<Double> extractPolylineCoords(Bundle args) {
        ArrayList<Double> polylineCoords = new ArrayList<>();
        try {
            ArrayList<Double> coords = (ArrayList<Double>) args.getSerializable("polylineCoords");
            if (coords != null) {
                polylineCoords.addAll(coords);
            }
        } catch (Exception ignored) {
        }
        return polylineCoords;
    }

    private String normalizeVehicleType(@Nullable String rawType) {
        if (rawType == null) {
            return null;
        }
        String trimmed = rawType.trim().toUpperCase(Locale.ROOT);
        if (trimmed.equals("SELECT VEHICLE TYPE") || trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.equals("STANDARD") || trimmed.equals("LUXURY") || trimmed.equals("VAN")) {
            return trimmed;
        }
        return null;
    }

    private String normalizeScheduledTime(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            inputFormat.setLenient(false);
            Date parsed = inputFormat.parse(trimmed);
            if (parsed == null) {
                return trimmed;
            }
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return outputFormat.format(parsed);
        } catch (Exception e) {
            return trimmed;
        }
    }

    private double resolvePriceEstimateFromArgs(Bundle args,
                                                @NonNull String vehicleType,
                                                double distanceKm) {
        double fallback = Math.max(1d, distanceKm);
        String normalizedVehicleType = vehicleType.toUpperCase(Locale.ROOT);

        double standard = args.getDouble("priceEstimateStandard", Double.NaN);
        double luxury = args.getDouble("priceEstimateLuxury", Double.NaN);
        double van = args.getDouble("priceEstimateVan", Double.NaN);

        if (normalizedVehicleType.equals("STANDARD") && !Double.isNaN(standard) && standard > 0) {
            return standard;
        }
        if (normalizedVehicleType.equals("LUXURY") && !Double.isNaN(luxury) && luxury > 0) {
            return luxury;
        }
        if (normalizedVehicleType.equals("VAN") && !Double.isNaN(van) && van > 0) {
            return van;
        }
        return fallback;
    }

    private void bindPricePreview(Spinner vehicleSpinner,
                                  TextView finalPrice,
                                  @Nullable Bundle args) {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePricePreview(finalPrice, args, vehicleSpinner.getSelectedItem() != null
                        ? vehicleSpinner.getSelectedItem().toString()
                        : null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                finalPrice.setText("");
            }
        };

        vehicleSpinner.setOnItemSelectedListener(listener);
        updatePricePreview(finalPrice, args, vehicleSpinner.getSelectedItem() != null
                ? vehicleSpinner.getSelectedItem().toString()
                : null);
    }

    private void updatePricePreview(TextView finalPrice,
                                    @Nullable Bundle args,
                                    @Nullable String selectedVehicleType) {
        String normalizedVehicleType = normalizeVehicleType(selectedVehicleType);
        if (normalizedVehicleType == null || args == null) {
            finalPrice.setText("");
            return;
        }

        double distanceKm = args.getDouble("distanceKm", 0d);
        double priceEstimate = resolvePriceEstimateFromArgs(args, normalizedVehicleType, distanceKm);
        if (priceEstimate > 0) {
            finalPrice.setText(String.format(Locale.getDefault(), "Estimated price: %.2f", priceEstimate));
        } else {
            finalPrice.setText("");
        }
    }

    public void showRouteOnMap(LocationDTO start, LocationDTO end,
                               @Nullable List<LocationDTO> stops,
                               @NonNull List<PolylinePointDTO> polylinePoints) {

        routeMapView.setShowMarkers(true);
        routeMapView.displayRoute(start, end, stops, polylinePoints);
    }
}
