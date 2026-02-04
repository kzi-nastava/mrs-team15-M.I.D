package com.example.ridenow.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.model.PolylinePoint;
import com.example.ridenow.dto.ride.RideEstimateResponseDTO;
import com.example.ridenow.dto.ride.RoutePointDTO;
import com.example.ridenow.dto.vehicle.VehicleResponse;
import com.example.ridenow.service.RideService;
import com.example.ridenow.service.VehicleService;
import com.example.ridenow.ui.components.RouteMapView;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements MapEventsReceiver {

    private static final long VEHICLE_UPDATE_INTERVAL = 10000;

    // Map components
    private RouteMapView routeMapView;
    private LocationManager locationManager;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    // Services
    private VehicleService vehicleService;
    private RideService rideService;
    private TokenUtils tokenUtils;

    // Vehicle update timer
    private Handler vehicleUpdateHandler;
    private Runnable vehicleUpdateRunnable;
    private double currentCenterLatitude;
    private double currentCenterLongitude;
    private boolean isVehicleUpdateActive = false;

    // Ride estimation UI components
    private EditText startAddressInput;
    private EditText endAddressInput;
    private Button btnEstimate;
    private LinearLayout resultsLayout;
    private TextView tvDistance;
    private TextView tvDuration;

    // Suggestion components
    private ListPopupWindow startSuggestionsPopup;
    private ListPopupWindow endSuggestionsPopup;
    private SuggestionAdapter startSuggestionsAdapter;
    private SuggestionAdapter endSuggestionsAdapter;
    private java.util.List<org.json.JSONObject> startSuggestionObjects = new java.util.ArrayList<>();
    private java.util.List<org.json.JSONObject> endSuggestionObjects = new java.util.ArrayList<>();
    private String lastStartQuery = "";
    private String lastEndQuery = "";
    private Handler suggestionHandler = new Handler(Looper.getMainLooper());
    private Runnable startPendingRunnable;
    private Runnable endPendingRunnable;
    private EditText activeInput;

    // Selected addresses
    private double selectedStartLat;
    private double selectedStartLon;
    private String selectedStartDisplayName;
    private double selectedEndLat;
    private double selectedEndLon;
    private String selectedEndDisplayName;
    private boolean hasSelectedStart = false;
    private boolean hasSelectedEnd = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize location services
        locationManager = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);

        // Initialize API services
        vehicleService = ClientUtils.getClient(VehicleService.class);
        rideService = ClientUtils.getClient(RideService.class);

        // Initialize token utils
        tokenUtils = new TokenUtils(requireContext());

        // Initialize vehicle update handler
        vehicleUpdateHandler = new Handler(Looper.getMainLooper());
        setupVehicleUpdateRunnable();

        // Setup permission launcher
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if ((fineLocationGranted != null && fineLocationGranted) ||
                            (coarseLocationGranted != null && coarseLocationGranted)) {
                        centerMapOnUserLocation();
                    } else {
                        // Center on default location (Novi Sad)
                        centerMapOnLocation(45.2671, 19.8335);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupMap();
        setupSuggestionPopups();
        setupAddressInputs();
        setupEstimateButton();

        return view;
    }

    private void initializeViews(View view) {
        // Map
        routeMapView = view.findViewById(R.id.routeMapView);

        // Ride estimation form
        startAddressInput = view.findViewById(R.id.startAddress);
        endAddressInput = view.findViewById(R.id.endAddressLayout);
        btnEstimate = view.findViewById(R.id.btnEstimate);
        resultsLayout = view.findViewById(R.id.resultsLayout);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvDuration = view.findViewById(R.id.tvDuration);
    }

    private void setupMap() {
        // Set up map events receiver for detecting map movements
        routeMapView.setMapEventsReceiver(this);

        // Always start at Novi Sad (default location)
        centerMapOnLocation(45.2671, 19.8335);

        // Request location permission and use device location if it's valid
        // NOTE: If testing on Android emulator and getting wrong location:
        // 1. Click "..." (More) in emulator toolbar → Location
        // 2. Set custom location: Latitude 45.2671, Longitude 19.8335
        // 3. Click "Send" to apply
        requestLocationPermission();
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Transliterate Serbian Cyrillic to Latin for display
    private String toLatin(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'А': sb.append('A'); break;
                case 'а': sb.append('a'); break;
                case 'Б': sb.append('B'); break;
                case 'б': sb.append('b'); break;
                case 'В': sb.append('V'); break;
                case 'в': sb.append('v'); break;
                case 'Г': sb.append('G'); break;
                case 'г': sb.append('g'); break;
                case 'Д': sb.append('D'); break;
                case 'д': sb.append('d'); break;
                case 'Ђ': sb.append('Đ'); break;
                case 'ђ': sb.append('đ'); break;
                case 'Е': sb.append('E'); break;
                case 'е': sb.append('e'); break;
                case 'Ж': sb.append('Ž'); break;
                case 'ж': sb.append('ž'); break;
                case 'З': sb.append('Z'); break;
                case 'з': sb.append('z'); break;
                case 'И': sb.append('I'); break;
                case 'и': sb.append('i'); break;
                case 'Ј': sb.append('J'); break;
                case 'ј': sb.append('j'); break;
                case 'К': sb.append('K'); break;
                case 'к': sb.append('k'); break;
                case 'Л': sb.append('L'); break;
                case 'л': sb.append('l'); break;
                case 'Љ': sb.append("Lj"); break;
                case 'љ': sb.append("lj"); break;
                case 'М': sb.append('M'); break;
                case 'м': sb.append('m'); break;
                case 'Н': sb.append('N'); break;
                case 'н': sb.append('n'); break;
                case 'Њ': sb.append("Nj"); break;
                case 'њ': sb.append("nj"); break;
                case 'О': sb.append('O'); break;
                case 'о': sb.append('o'); break;
                case 'П': sb.append('P'); break;
                case 'п': sb.append('p'); break;
                case 'Р': sb.append('R'); break;
                case 'р': sb.append('r'); break;
                case 'С': sb.append('S'); break;
                case 'с': sb.append('s'); break;
                case 'Т': sb.append('T'); break;
                case 'т': sb.append('t'); break;
                case 'Ћ': sb.append('Ć'); break;
                case 'ћ': sb.append('ć'); break;
                case 'У': sb.append('U'); break;
                case 'у': sb.append('u'); break;
                case 'Ф': sb.append('F'); break;
                case 'ф': sb.append('f'); break;
                case 'Х': sb.append('H'); break;
                case 'х': sb.append('h'); break;
                case 'Ц': sb.append('C'); break;
                case 'ц': sb.append('c'); break;
                case 'Ч': sb.append('Č'); break;
                case 'ч': sb.append('č'); break;
                case 'Џ': sb.append("Dž"); break;
                case 'џ': sb.append("dž"); break;
                case 'Ш': sb.append('Š'); break;
                case 'ш': sb.append('š'); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }

    private class SuggestionAdapter extends ArrayAdapter<org.json.JSONObject> {
        private java.util.List<org.json.JSONObject> items;
        private String currentQuery;

        public SuggestionAdapter(@NonNull android.content.Context ctx, java.util.List<org.json.JSONObject> items) {
            super(ctx, android.R.layout.simple_list_item_2);
            this.items = items;
        }

        public void setCurrentQuery(String query) {
            this.currentQuery = query;
        }

        @Override
        public int getCount() { return items.size(); }

        @Nullable
        @Override
        public org.json.JSONObject getItem(int position) { return items.get(position); }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView t1 = v.findViewById(android.R.id.text1);
            TextView t2 = v.findViewById(android.R.id.text2);

            org.json.JSONObject f = items.get(position);
            org.json.JSONObject props = f.optJSONObject("properties");
            String primary = null;
            if (props != null) {
                String street = props.optString("street", "");
                String housenumber = props.optString("housenumber", "");
                String city = props.optString("city", "");
                if (!street.isEmpty() && !housenumber.isEmpty()) {
                    primary = street + " " + housenumber + (city.isEmpty() ? "" : ", " + city);
                }
            }
            if (primary == null) {
                primary = f.optString("display_name", null);
                if (primary == null && props != null) primary = props.optString("name", props.optString("label", ""));
            }
            if (primary == null) primary = "";
            primary = toLatin(primary);

            // secondary info
            String secondary = "";
            if (props != null) {
                String city = props.optString("city", "");
                String street = props.optString("street", "");
                String housenumber = props.optString("housenumber", "");
                String country = props.optString("country", "");
                java.util.ArrayList<String> parts = new java.util.ArrayList<>();
                if (!street.isEmpty()) parts.add(street + (housenumber.isEmpty() ? "" : " " + housenumber));
                if (!city.isEmpty()) parts.add(city);
                if (!country.isEmpty()) parts.add(country);
                secondary = String.join(", ", parts);
            }
            secondary = toLatin(secondary);

            // highlight match in primary
            if (currentQuery != null && !currentQuery.isEmpty()) {
                String lower = primary.toLowerCase();
                String qlower = currentQuery.toLowerCase();
                int idx = lower.indexOf(qlower);
                if (idx >= 0) {
                    android.text.SpannableString ss = new android.text.SpannableString(primary);
                    ss.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), idx, idx + currentQuery.length(), 0);
                    t1.setText(ss);
                } else {
                    t1.setText(primary);
                }
            } else {
                t1.setText(primary);
            }

            t2.setText(secondary);
            return v;
        }

        public void clear() { items.clear(); notifyDataSetChanged(); }

        public void addAll(java.util.List<org.json.JSONObject> list) { items.addAll(list); notifyDataSetChanged(); }
    }

    private void setupSuggestionPopups() {
        // Setup start address suggestions
        startSuggestionsAdapter = new SuggestionAdapter(requireContext(), new java.util.ArrayList<>());
        startSuggestionsPopup = new ListPopupWindow(requireContext());
        startSuggestionsPopup.setAdapter(startSuggestionsAdapter);
        startSuggestionsPopup.setModal(false);
        startSuggestionsPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        startSuggestionsPopup.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < startSuggestionObjects.size()) {
                try {
                    org.json.JSONObject sel = startSuggestionObjects.get(position);
                    org.json.JSONObject props = sel.optJSONObject("properties");
                    String display = null;
                    if (props != null) {
                        String street = props.optString("street", "");
                        String housenumber = props.optString("housenumber", "");
                        String city = props.optString("city", "");
                        if (!street.isEmpty() && !housenumber.isEmpty()) {
                            display = street + " " + housenumber + (city.isEmpty() ? "" : ", " + city);
                        }
                    }
                    if (display == null) {
                        display = sel.optString("display_name", null);
                        if (display == null) {
                            if (props != null) display = props.optString("name", props.optString("label", ""));
                        }
                    }
                    display = toLatin(display);

                    // Store coordinates first
                    org.json.JSONArray geom = sel.optJSONObject("geometry").optJSONArray("coordinates");
                    if (geom != null && geom.length() >= 2) {
                        selectedStartLon = geom.optDouble(0);
                        selectedStartLat = geom.optDouble(1);
                        hasSelectedStart = true;
                        selectedStartDisplayName = display;

                        // Clear suggestions before setting text to prevent retriggering
                        startSuggestionsAdapter.clear();
                        startSuggestionObjects.clear();

                        // Set text (this will trigger TextWatcher but we'll ignore it)
                        if (display != null) {
                            startAddressInput.setText(display);
                        }
                    }
                } catch (Exception ignored) {}
            }
            startSuggestionsPopup.dismiss();
        });

        // Setup end address suggestions
        endSuggestionsAdapter = new SuggestionAdapter(requireContext(), new java.util.ArrayList<>());
        endSuggestionsPopup = new ListPopupWindow(requireContext());
        endSuggestionsPopup.setAdapter(endSuggestionsAdapter);
        endSuggestionsPopup.setModal(false);
        endSuggestionsPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        endSuggestionsPopup.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < endSuggestionObjects.size()) {
                try {
                    org.json.JSONObject sel = endSuggestionObjects.get(position);
                    org.json.JSONObject props = sel.optJSONObject("properties");
                    String display = null;
                    if (props != null) {
                        String street = props.optString("street", "");
                        String housenumber = props.optString("housenumber", "");
                        String city = props.optString("city", "");
                        if (!street.isEmpty() && !housenumber.isEmpty()) {
                            display = street + " " + housenumber + (city.isEmpty() ? "" : ", " + city);
                        }
                    }
                    if (display == null) {
                        display = sel.optString("display_name", null);
                        if (display == null) {
                            if (props != null) display = props.optString("name", props.optString("label", ""));
                        }
                    }
                    display = toLatin(display);

                    // Store coordinates first
                    org.json.JSONArray geom = sel.optJSONObject("geometry").optJSONArray("coordinates");
                    if (geom != null && geom.length() >= 2) {
                        selectedEndLon = geom.optDouble(0);
                        selectedEndLat = geom.optDouble(1);
                        hasSelectedEnd = true;
                        selectedEndDisplayName = display;

                        // Clear suggestions before setting text to prevent retriggering
                        endSuggestionsAdapter.clear();
                        endSuggestionObjects.clear();

                        // Set text (this will trigger TextWatcher but we'll ignore it)
                        if (display != null) {
                            endAddressInput.setText(display);
                        }
                    }
                } catch (Exception ignored) {}
            }
            endSuggestionsPopup.dismiss();
        });
    }

    private void setupAddressInputs() {
        // Start address input
        startAddressInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activeInput = startAddressInput;
                startSuggestionsPopup.setAnchorView(startAddressInput);
                startSuggestionsPopup.setWidth(startAddressInput.getWidth());
                startSuggestionsPopup.setHeight(dpToPx(200));
                if (!startSuggestionsAdapter.isEmpty()) startSuggestionsPopup.show();
            } else {
                suggestionHandler.postDelayed(() -> startSuggestionsPopup.dismiss(), 150);
            }
        });

        startAddressInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                activeInput = startAddressInput;

                // Only fetch new suggestions if user is actually typing (not when we set text programmatically)
                if (!hasSelectedStart || count == 0) {
                    hasSelectedStart = false; // Reset selection when user types
                    if (startPendingRunnable != null) suggestionHandler.removeCallbacks(startPendingRunnable);
                    String q = s.toString();
                    startPendingRunnable = () -> fetchSuggestions(q, true);
                    suggestionHandler.postDelayed(startPendingRunnable, 300);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // End address input
        endAddressInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activeInput = endAddressInput;
                endSuggestionsPopup.setAnchorView(endAddressInput);
                endSuggestionsPopup.setWidth(endAddressInput.getWidth());
                endSuggestionsPopup.setHeight(dpToPx(200));
                if (!endSuggestionsAdapter.isEmpty()) endSuggestionsPopup.show();
            } else {
                suggestionHandler.postDelayed(() -> endSuggestionsPopup.dismiss(), 150);
            }
        });

        endAddressInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                activeInput = endAddressInput;

                // Only fetch new suggestions if user is actually typing (not when we set text programmatically)
                if (!hasSelectedEnd || count == 0) {
                    hasSelectedEnd = false; // Reset selection when user types
                    if (endPendingRunnable != null) suggestionHandler.removeCallbacks(endPendingRunnable);
                    String q = s.toString();
                    endPendingRunnable = () -> fetchSuggestions(q, false);
                    suggestionHandler.postDelayed(endPendingRunnable, 300);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchSuggestions(String query, boolean isStartAddress) {
        if (query == null || query.trim().isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                if (isStartAddress) {
                    startSuggestionsAdapter.clear();
                    startSuggestionObjects.clear();
                    startSuggestionsPopup.dismiss();
                } else {
                    endSuggestionsAdapter.clear();
                    endSuggestionObjects.clear();
                    endSuggestionsPopup.dismiss();
                }
            });
            return;
        }

        new Thread(() -> {
            java.util.List<org.json.JSONObject> objs = new java.util.ArrayList<>();
            try {
                // Novi Sad bbox: minLon,minLat,maxLon,maxLat
                String bbox = "19.70,45.18,19.95,45.35";
                String urlStr = "https://photon.komoot.io/api/?q=" + java.net.URLEncoder.encode(query, "UTF-8") + "&limit=5&bbox=" + bbox;
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    org.json.JSONObject root = new org.json.JSONObject(sb.toString());
                    org.json.JSONArray features = root.optJSONArray("features");
                    if (features != null) {
                        for (int i = 0; i < features.length(); i++) {
                            org.json.JSONObject f = features.getJSONObject(i);
                            objs.add(f);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final java.util.List<org.json.JSONObject> finalObjs = objs;
            requireActivity().runOnUiThread(() -> {
                if (isStartAddress) {
                    lastStartQuery = query;
                    startSuggestionsAdapter.setCurrentQuery(query);
                    startSuggestionsAdapter.clear();
                    startSuggestionsAdapter.addAll(finalObjs);
                    startSuggestionObjects.clear();
                    startSuggestionObjects.addAll(finalObjs);
                    if (finalObjs.isEmpty()) {
                        startSuggestionsPopup.dismiss();
                    } else {
                        if (activeInput == startAddressInput) {
                            startSuggestionsPopup.setAnchorView(startAddressInput);
                            startSuggestionsPopup.show();
                        }
                    }
                } else {
                    lastEndQuery = query;
                    endSuggestionsAdapter.setCurrentQuery(query);
                    endSuggestionsAdapter.clear();
                    endSuggestionsAdapter.addAll(finalObjs);
                    endSuggestionObjects.clear();
                    endSuggestionObjects.addAll(finalObjs);
                    if (finalObjs.isEmpty()) {
                        endSuggestionsPopup.dismiss();
                    } else {
                        if (activeInput == endAddressInput) {
                            endSuggestionsPopup.setAnchorView(endAddressInput);
                            endSuggestionsPopup.show();
                        }
                    }
                }
            });
        }).start();
    }

    private void setupEstimateButton() {
        btnEstimate.setOnClickListener(v -> {
            if (validateInputs()) {
                getRouteEstimate();
            }
        });
    }

    private boolean validateInputs() {
        if (!hasSelectedStart) {
            startAddressInput.setError("Please select a valid start address from suggestions");
            Toast.makeText(getContext(), "Please select a valid start address from suggestions", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!hasSelectedEnd) {
            endAddressInput.setError("Please select a valid end address from suggestions");
            Toast.makeText(getContext(), "Please select a valid end address from suggestions", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedStartLat == selectedEndLat && selectedStartLon == selectedEndLon) {
            Toast.makeText(getContext(), "Start and end addresses must be different", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void getRouteEstimate() {
        resultsLayout.setVisibility(View.GONE);

        btnEstimate.setEnabled(false);
        btnEstimate.setText("Loading...");

        Call<RideEstimateResponseDTO> call = rideService.estimate(
                selectedStartLat,
                selectedStartLon,
                selectedEndLat,
                selectedEndLon
        );

        call.enqueue(new Callback<RideEstimateResponseDTO>() {
            @Override
            public void onResponse(Call<RideEstimateResponseDTO> call,
                                   Response<RideEstimateResponseDTO> response) {
                btnEstimate.setEnabled(true);
                btnEstimate.setText("Estimate Ride");

                if (response.isSuccessful() && response.body() != null) {
                    displayRouteEstimate(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to get route estimate",
                            Toast.LENGTH_SHORT).show();
                    Log.e("HomeFragment", "Failed to get route estimate: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RideEstimateResponseDTO> call, Throwable t) {
                btnEstimate.setEnabled(true);
                btnEstimate.setText("Estimate Ride");
                Toast.makeText(getContext(), "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Error getting route estimate", t);
            }
        });
    }

    private void displayRouteEstimate(RideEstimateResponseDTO estimate) {
        List<PolylinePoint> polylinePoints = new ArrayList<>();
        if (estimate.getRoute() != null) {
            for (RoutePointDTO point : estimate.getRoute()) {
                PolylinePoint polylinePoint = new PolylinePoint();
                polylinePoint.setLatitude(point.getLat());
                polylinePoint.setLongitude(point.getLng());
                polylinePoints.add(polylinePoint);
            }
        }
        com.example.ridenow.dto.model.Location startLocation = new com.example.ridenow.dto.model.Location();
        startLocation.setLatitude(selectedStartLat);
        startLocation.setLongitude(selectedStartLon);
        startLocation.setAddress(selectedStartDisplayName);

        com.example.ridenow.dto.model.Location endLocation = new com.example.ridenow.dto.model.Location();
        endLocation.setLatitude(selectedEndLat);
        endLocation.setLongitude(selectedEndLon);
        endLocation.setAddress(selectedEndDisplayName);

        // Display route on map
        routeMapView.clearMap();
        routeMapView.displayRoute(startLocation, endLocation, null, polylinePoints);

        // Display estimate details
        tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", estimate.getDistanceKm()));
        tvDuration.setText(String.format(Locale.getDefault(), "Duration: %d min", estimate.getEstimatedDurationMin()));

        // Show results
        resultsLayout.setVisibility(View.VISIBLE);

        // Stop vehicle updates while showing route
        stopVehicleUpdates();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            centerMapOnUserLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void centerMapOnUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            Location location = null;

            // Try to get location from GPS first
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            // If GPS is not available, try network provider
            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                // Check if location is reasonable (within Serbia/nearby region)
                // Novi Sad coordinates: 45.2671, 19.8335
                // Only use device location if it's within ~200km of Novi Sad
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                // Simple distance check: is it roughly in Serbia/nearby?
                boolean isNearSerbia = (lat >= 42.0 && lat <= 47.0) && (lon >= 18.0 && lon <= 23.0);

                if (isNearSerbia) {
                    // Location seems valid, use it
                    centerMapOnLocation(lat, lon);

                    // Add driver marker if user is logged in as a driver
                    if (isUserDriver()) {
                        routeMapView.addDriverLocation(lat, lon);
                    }
                } else {
                    // Location is outside Serbia, probably emulator default or invalid
                    Log.w("HomeFragment", "Device location outside expected region: " + lat + ", " + lon);
                    // Map is already centered on Novi Sad from setupMap(), so do nothing
                }
            } else {
                // No location available, map is already centered on Novi Sad
                Log.d("HomeFragment", "No device location available");
            }
        } catch (SecurityException e) {
            Log.e("HomeFragment", "Location permission error", e);
        }
    }

    private void centerMapOnLocation(double latitude, double longitude) {
        routeMapView.centerOnLocation(latitude, longitude);

        // Store current center coordinates
        currentCenterLatitude = latitude;
        currentCenterLongitude = longitude;

        // Load vehicles around this location and start periodic updates
        loadVehiclesAroundLocation(latitude, longitude);
        startVehicleUpdates();
    }

    private void loadVehiclesAroundLocation(double latitude, double longitude) {
        Call<List<VehicleResponse>> call = vehicleService.getAllVehicles(latitude, longitude);
        call.enqueue(new Callback<List<VehicleResponse>>() {
            @Override
            public void onResponse(Call<List<VehicleResponse>> call, Response<List<VehicleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayVehiclesOnMap(response.body());
                } else {
                    Log.e("HomeFragment", "Failed to load vehicles: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<VehicleResponse>> call, Throwable t) {
                Log.e("HomeFragment", "Error loading vehicles", t);
                Toast.makeText(getContext(), "Failed to load vehicles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayVehiclesOnMap(List<VehicleResponse> vehicles) {
        routeMapView.displayVehicles(vehicles);
    }

    // MapEventsReceiver interface methods
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        // When user long presses (moves map), load vehicles for the new center
        GeoPoint mapCenter = routeMapView.getMapCenter();
        if (mapCenter != null) {
            routeMapView.clearMap();
            currentCenterLatitude = mapCenter.getLatitude();
            currentCenterLongitude = mapCenter.getLongitude();

            loadVehiclesAroundLocation(mapCenter.getLatitude(), mapCenter.getLongitude());

            // Restart vehicle updates with new location
            startVehicleUpdates();
            resultsLayout.setVisibility(View.GONE);
        }
        return true;
    }

    private boolean isUserDriver() {
        return tokenUtils != null && tokenUtils.isLoggedIn() && "DRIVER".equals(tokenUtils.getRole());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (routeMapView != null) {
            routeMapView.onResume();
        }
        // Restart vehicle updates if we have a location and not showing a route
        if (currentCenterLatitude != 0 && currentCenterLongitude != 0 &&
                resultsLayout.getVisibility() != View.VISIBLE) {
            startVehicleUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (routeMapView != null) {
            routeMapView.onPause();
        }
        // Stop vehicle updates to save resources
        stopVehicleUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (routeMapView != null) {
            routeMapView.onDestroy();
        }
        // Stop vehicle updates and cleanup handler
        stopVehicleUpdates();
        if (vehicleUpdateHandler != null) {
            vehicleUpdateHandler.removeCallbacksAndMessages(null);
        }
        if (suggestionHandler != null) {
            suggestionHandler.removeCallbacksAndMessages(null);
        }
    }

    private void setupVehicleUpdateRunnable() {
        vehicleUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isVehicleUpdateActive) {
                    // Update vehicles for current map center
                    loadVehiclesAroundLocation(currentCenterLatitude, currentCenterLongitude);

                    // Schedule next update
                    if (vehicleUpdateHandler != null) {
                        vehicleUpdateHandler.postDelayed(this, VEHICLE_UPDATE_INTERVAL);
                    }
                }
            }
        };
    }

    private void startVehicleUpdates() {
        stopVehicleUpdates(); // Stop any existing updates first

        isVehicleUpdateActive = true;
        if (vehicleUpdateHandler != null && vehicleUpdateRunnable != null) {
            vehicleUpdateHandler.postDelayed(vehicleUpdateRunnable, VEHICLE_UPDATE_INTERVAL);
        }
    }

    private void stopVehicleUpdates() {
        isVehicleUpdateActive = false;
        if (vehicleUpdateHandler != null && vehicleUpdateRunnable != null) {
            vehicleUpdateHandler.removeCallbacks(vehicleUpdateRunnable);
        }
    }
}