package com.example.ridenow.ui.ride;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ridenow.ui.components.RouteMapView;
import androidx.navigation.Navigation;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.service.RideService;
import com.example.ridenow.service.PassengerService;
import com.example.ridenow.dto.ride.FavoriteRouteResponseDTO;
import com.example.ridenow.dto.ride.RouteResponseDTO;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.dto.ride.RideEstimateResponseDTO;
import com.example.ridenow.dto.model.PolylinePointDTO;
import com.example.ridenow.dto.model.LocationDTO;
import com.example.ridenow.dto.ride.RoutePointDTO;
import com.example.ridenow.dto.ride.EstimateRouteRequestDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RideOrderingFragment extends Fragment {
    private com.example.ridenow.ui.components.RouteMapView routeMapView;
    private ListPopupWindow suggestionsPopup;
    private SuggestionAdapter suggestionsAdapter;
    private java.util.List<org.json.JSONObject> suggestionObjects = new java.util.ArrayList<>();
    private String lastQuery = "";
    private android.os.Handler handler = new android.os.Handler();
    private Runnable pendingRunnable;
    private EditText activeInput;

    // --- new fields to track selected addresses and backend client ---
    private RideService rideService;
    private PassengerService passengerService;

    private EditText pickupInput;
    private EditText destinationInput;

    private boolean hasSelectedStart = false;
    private boolean hasSelectedEnd = false;
    private double selectedStartLat, selectedStartLon;
    private double selectedEndLat, selectedEndLon;
    private String selectedStartDisplayName;
    private String selectedEndDisplayName;

    // UI fields promoted so they can be used by callbacks
    private LinearLayout resultsLayout;
    private TextView tvDistance;
    private TextView tvDuration;
    private TextView tvCost;
    private Button showRouteBtn;
    private Button chooseRouteBtn;
    // favorites UI
    private android.widget.TextView favoriteSelected;
    private android.widget.ImageButton favoriteToggle;
    private ListPopupWindow favoritesPopup;
    private java.util.List<FavoriteRouteResponseDTO> favoriteObjects = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<String> favoritesAdapter;
    private String currentSelectedFavoriteName = null;
    private Long currentSelectedFavoriteId = null;
    // per-stop selection state and coordinates
    private java.util.List<Boolean> hasSelectedStop = new java.util.ArrayList<>();
    private java.util.List<Double> stopLatitudesSelected = new java.util.ArrayList<>();
    private java.util.List<Double> stopLongitudesSelected = new java.util.ArrayList<>();
    private java.util.List<String> stopDisplayNames = new java.util.ArrayList<>();

    public RideOrderingFragment() {
        // Required empty constructor
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

        public SuggestionAdapter(@NonNull android.content.Context ctx, java.util.List<org.json.JSONObject> items) {
            super(ctx, android.R.layout.simple_list_item_2);
            this.items = items;
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
            if (lastQuery != null && !lastQuery.isEmpty()) {
                String lower = primary.toLowerCase();
                String qlower = lastQuery.toLowerCase();
                int idx = lower.indexOf(qlower);
                if (idx >= 0) {
                    android.text.SpannableString ss = new android.text.SpannableString(primary);
                    ss.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), idx, idx + lastQuery.length(), 0);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_ordering, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize map view (follow HomeFragment pattern)
        routeMapView = view.findViewById(R.id.routeMapView);
        if (routeMapView != null) {
            // center on Novi Sad by default
            routeMapView.centerOnLocation(45.2671, 19.8335);
        }

        // initialize ride service
        rideService = ClientUtils.getClient(RideService.class);
        passengerService = ClientUtils.getClient(PassengerService.class);

        // UI fields
        pickupInput = view.findViewById(R.id.pickupAddress);
        destinationInput = view.findViewById(R.id.destinationAddress);
        favoriteSelected = view.findViewById(R.id.favoriteSelected);
        favoriteToggle = view.findViewById(R.id.favoriteToggle);
        LinearLayout stopsContainer = view.findViewById(R.id.stopsContainer);
        ImageButton addStopBtn = view.findViewById(R.id.addStopBtn);
        resultsLayout = view.findViewById(R.id.resultsLayout);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvCost = view.findViewById(R.id.tvCost);
        showRouteBtn = view.findViewById(R.id.showRouteBtn);
        chooseRouteBtn = view.findViewById(R.id.chooseRouteBtn);

        // enable Show Route only for users with role USER (server requires hasRole('USER'))
        try {
            String role = ClientUtils.getTokenUtils().getRole();
            boolean isUser = "USER".equals(role);
            if (showRouteBtn != null) showRouteBtn.setEnabled(isUser);
        } catch (IllegalStateException ignored) {
            // ClientUtils not initialized; default to enabled (will fail server-side if not authenticated)
        }

        // setup suggestions popup (anchored to inputs)
        suggestionsAdapter = new SuggestionAdapter(requireContext(), new java.util.ArrayList<>());
        suggestionsPopup = new ListPopupWindow(requireContext());
        suggestionsPopup.setAdapter(suggestionsAdapter);
        // non-modal so user can keep typing while suggestions are visible
        suggestionsPopup.setModal(false);
        suggestionsPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        suggestionsPopup.setOnItemClickListener((parent, itemView, position, id) -> {
            if (activeInput != null && position >= 0 && position < suggestionObjects.size()) {
                try {
                    org.json.JSONObject sel = suggestionObjects.get(position);
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
                    if (display != null) activeInput.setText(display);

                    // also add temporary marker on map if coordinates available
                    org.json.JSONArray geom = sel.optJSONObject("geometry").optJSONArray("coordinates");
                    if (geom != null && geom.length() >= 2) {
                        double lon = geom.optDouble(0);
                        double lat = geom.optDouble(1);

                        // remember selection depending on which input was active
                        if (activeInput == pickupInput) {
                            hasSelectedStart = true;
                            selectedStartLat = lat;
                            selectedStartLon = lon;
                            selectedStartDisplayName = display;
                        } else if (activeInput == destinationInput) {
                            hasSelectedEnd = true;
                            selectedEndLat = lat;
                            selectedEndLon = lon;
                            selectedEndDisplayName = display;
                        } else {
                            // it's probably one of the stop inputs - find its index in stopsContainer
                            View root = getView();
                            if (root != null) {
                                LinearLayout sc = root.findViewById(R.id.stopsContainer);
                                if (sc != null) {
                                    for (int i = 0; i < sc.getChildCount(); i++) {
                                        View row = sc.getChildAt(i);
                                        if (row instanceof LinearLayout) {
                                            // check if activeInput is a descendant of this row
                                            if (((LinearLayout) row).indexOfChild(activeInput) >= 0) {
                                                // ensure lists have capacity
                                                while (hasSelectedStop.size() <= i) {
                                                    hasSelectedStop.add(false);
                                                    stopLatitudesSelected.add(0.0);
                                                    stopLongitudesSelected.add(0.0);
                                                    stopDisplayNames.add("");
                                                }
                                                hasSelectedStop.set(i, true);
                                                stopLatitudesSelected.set(i, lat);
                                                stopLongitudesSelected.set(i, lon);
                                                stopDisplayNames.set(i, display);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                            if (routeMapView != null) routeMapView.addTemporaryMarker(lat, lon, display);
                    }
                } catch (Exception ignored) {}
            }
                // prevent pending fetch from re-showing the popup and clear focus
                try { if (pendingRunnable != null) handler.removeCallbacks(pendingRunnable); } catch (Exception ignored) {}
                pendingRunnable = null;
                try { if (activeInput != null) { activeInput.clearFocus(); } } catch (Exception ignored) {}
                activeInput = null;
                suggestionsPopup.dismiss();
        });

        // setup favorites popup
        favoritesAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new java.util.ArrayList<>());
        favoritesPopup = new ListPopupWindow(requireContext());
        favoritesPopup.setAdapter(favoritesAdapter);
        favoritesPopup.setModal(true);
        favoritesPopup.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < favoriteObjects.size()) {
                FavoriteRouteResponseDTO fav = favoriteObjects.get(position);
                applyFavoriteRoute(fav);
            }
            favoritesPopup.dismiss();
        });

        android.view.View.OnClickListener favToggle = v -> {
            if (favoritesPopup.isShowing()) {
                favoritesPopup.dismiss();
            } else {
                favoritesPopup.setAnchorView(favoriteSelected);
                favoritesPopup.setWidth(favoriteSelected.getWidth());
                favoritesPopup.setHeight(dpToPx(200));
                favoritesPopup.show();
            }
        };
        favoriteToggle.setOnClickListener(favToggle);
        favoriteSelected.setOnClickListener(favToggle);

        fetchFavoriteRoutes();

        // add initial stop input
        addStopInput(stopsContainer);

        addStopBtn.setOnClickListener(v -> addStopInput(stopsContainer));

        // attach suggestion handlers
        attachSuggestionHandlers(pickupInput);
        attachSuggestionHandlers(destinationInput);

        showRouteBtn.setOnClickListener(v -> {
            // validate selected points
            if (!hasSelectedStart) {
                pickupInput.setError("Please select a valid pickup address from suggestions");
                Toast.makeText(getContext(), "Please select a valid pickup address from suggestions", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!hasSelectedEnd) {
                destinationInput.setError("Please select a valid destination address from suggestions");
                Toast.makeText(getContext(), "Please select a valid destination address from suggestions", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStartLat == selectedEndLat && selectedStartLon == selectedEndLon) {
                Toast.makeText(getContext(), "Start and end addresses must be different", Toast.LENGTH_SHORT).show();
                return;
            }

            showRouteBtn.setEnabled(false);
            showRouteBtn.setText("Loading...");

            // Build POST body for /rides/estimate-route
            EstimateRouteRequestDTO req = new EstimateRouteRequestDTO();
            req.setStartLatitude(selectedStartLat);
            req.setStartLongitude(selectedStartLon);
            req.setEndLatitude(selectedEndLat);
            req.setEndLongitude(selectedEndLon);
            // attach stops from UI (if any)
            try {
                View root = getView();
                if (root != null) {
                    LinearLayout sc = root.findViewById(R.id.stopsContainer);
                    if (sc != null) {
                        java.util.List<LocationDTO> stops = new java.util.ArrayList<>();
                        java.util.List<String> stopAddrs = new java.util.ArrayList<>();
                        java.util.List<Double> stopLats = new java.util.ArrayList<>();
                        java.util.List<Double> stopLons = new java.util.ArrayList<>();
                        for (int i = 0; i < sc.getChildCount(); i++) {
                            View row = sc.getChildAt(i);
                            if (row instanceof LinearLayout) {
                                // find first EditText child in the stop row
                                EditText stopField = null;
                                for (int ci = 0; ci < ((LinearLayout) row).getChildCount(); ci++) {
                                    View c = ((LinearLayout) row).getChildAt(ci);
                                    if (c instanceof EditText) { stopField = (EditText) c; break; }
                                }
                                if (stopField != null) {
                                    String text = stopField.getText().toString().trim();
                                    if (!text.isEmpty()) {
                                        LocationDTO l = new LocationDTO();
                                        l.setAddress(text);
                                        stops.add(l);
                                        stopAddrs.add(text);
                                    }
                                }
                            }
                        }
                        if (!stops.isEmpty()) {
                            Log.d("RideOrdering", "Collected stop addresses: " + stopAddrs);
                            req.setStops(stops);
                            req.setStopAddresses(stopAddrs);
                            // populate lat/lon lists from per-stop selected coordinates
                            java.util.List<Double> latList = new java.util.ArrayList<>();
                            java.util.List<Double> lonList = new java.util.ArrayList<>();
                            boolean haveCoords = false;
                            for (int si = 0; si < stopAddrs.size(); si++) {
                                if (si < hasSelectedStop.size() && hasSelectedStop.get(si) != null && hasSelectedStop.get(si)) {
                                    double lat = stopLatitudesSelected.get(si);
                                    double lon = stopLongitudesSelected.get(si);
                                    latList.add(lat);
                                    lonList.add(lon);
                                    haveCoords = true;
                                }
                            }
                            if (haveCoords) {
                                req.setStopLatitudes(latList);
                                req.setStopLongitudes(lonList);
                            }
                        } else {
                            Log.d("RideOrdering", "No stops collected from UI");
                        }
                    }
                }
            } catch (Exception ignored) {}
            // set address fields expected by backend (use input text as fallback)
            try {
                String startAddr = (selectedStartDisplayName != null && !selectedStartDisplayName.trim().isEmpty()) ? selectedStartDisplayName : (pickupInput != null ? pickupInput.getText().toString() : "");
                String endAddr = (selectedEndDisplayName != null && !selectedEndDisplayName.trim().isEmpty()) ? selectedEndDisplayName : (destinationInput != null ? destinationInput.getText().toString() : "");
                req.setStartAddress(startAddr);
                req.setEndAddress(endAddr);
            } catch (Exception ignored) {}

            Call<RideEstimateResponseDTO> call = rideService.estimateRoute(req);

            call.enqueue(new Callback<RideEstimateResponseDTO>() {
                @Override
                public void onResponse(Call<RideEstimateResponseDTO> call, Response<RideEstimateResponseDTO> response) {
                    showRouteBtn.setEnabled(true);
                    showRouteBtn.setText("Show Route");

                    if (response.isSuccessful() && response.body() != null) {
                        RideEstimateResponseDTO estimate = response.body();

                        // convert route points
                        List<PolylinePointDTO> polylinePoints = new ArrayList<>();
                        if (estimate.getRoute() != null) {
                            for (RoutePointDTO point : estimate.getRoute()) {
                                PolylinePointDTO polylinePoint = new PolylinePointDTO();
                                polylinePoint.setLatitude(point.getLat());
                                polylinePoint.setLongitude(point.getLng());
                                polylinePoints.add(polylinePoint);
                            }
                        }

                        LocationDTO startLocation = new LocationDTO();
                        startLocation.setLatitude(selectedStartLat);
                        startLocation.setLongitude(selectedStartLon);
                        String startAddr = (selectedStartDisplayName != null && !selectedStartDisplayName.trim().isEmpty()) ? selectedStartDisplayName : (pickupInput != null ? pickupInput.getText().toString() : "");
                        startLocation.setAddress(startAddr);

                        LocationDTO endLocation = new LocationDTO();
                        endLocation.setLatitude(selectedEndLat);
                        endLocation.setLongitude(selectedEndLon);
                        String endAddr = (selectedEndDisplayName != null && !selectedEndDisplayName.trim().isEmpty()) ? selectedEndDisplayName : (destinationInput != null ? destinationInput.getText().toString() : "");
                        endLocation.setAddress(endAddr);

                        // Display route on map
                        routeMapView.clearMap();
                        routeMapView.displayRoute(startLocation, endLocation, null, polylinePoints);

                        // Display estimate details
                        tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", estimate.getDistanceKm()));
                        tvDuration.setText(String.format(Locale.getDefault(), "Duration: %d min", estimate.getEstimatedDurationMin()));
                        tvCost.setText("");

                        // Show results
                        resultsLayout.setVisibility(View.VISIBLE);
                    } else {
                        if (response.code() == 403) {
                            Toast.makeText(getContext(), "You need to be logged in as a USER to estimate routes.", Toast.LENGTH_LONG).show();
                        } else {
                            String msg = "Failed to get route estimate";
                            try {
                                if (response.errorBody() != null) msg = response.errorBody().string();
                            } catch (Exception ignored) {}
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<RideEstimateResponseDTO> call, Throwable t) {
                    showRouteBtn.setEnabled(true);
                    showRouteBtn.setText("Show Route");
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        chooseRouteBtn.setOnClickListener(v -> {
            if (!hasSelectedStart || !hasSelectedEnd) {
                Toast.makeText(requireContext(), "Please select pickup and destination from suggestions", Toast.LENGTH_SHORT).show();
                return;
            }
            // build bundle with primitive route info
            Bundle bundle = new Bundle();
            bundle.putDouble("startLat", selectedStartLat);
            bundle.putDouble("startLon", selectedStartLon);
            bundle.putDouble("endLat", selectedEndLat);
            bundle.putDouble("endLon", selectedEndLon);
            bundle.putString("startAddress", selectedStartDisplayName);
            bundle.putString("endAddress", selectedEndDisplayName);

            try {
                Navigation.findNavController(requireActivity(), com.example.ridenow.R.id.nav_host_fragment)
                        .navigate(com.example.ridenow.R.id.ride_preference, bundle);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void addStopInput(LinearLayout container) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int topMargin = (int) (8 * requireContext().getResources().getDisplayMetrics().density);
        rowParams.setMargins(0, topMargin, 0, 0);
        row.setLayoutParams(rowParams);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(0, 0, 8, 0);
        EditText stopInput = new EditText(requireContext());
        stopInput.setHint("Enter stop address");
        stopInput.setLayoutParams(lp);
        stopInput.setBackgroundResource(R.drawable.edittext_with_bg);
        stopInput.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        stopInput.setHintTextColor(0x80FFFFFF);

        ImageButton removeBtn = new ImageButton(requireContext());
        removeBtn.setImageResource(R.drawable.ic_trash);
        removeBtn.setBackgroundResource(android.R.color.transparent);
        removeBtn.setContentDescription("Remove stop");
        removeBtn.setOnClickListener(v -> {
            int idx = container.indexOfChild(row);
            container.removeView(row);
            if (idx >= 0) {
                if (idx < hasSelectedStop.size()) {
                    hasSelectedStop.remove(idx);
                }
                if (idx < stopLatitudesSelected.size()) stopLatitudesSelected.remove(idx);
                if (idx < stopLongitudesSelected.size()) stopLongitudesSelected.remove(idx);
                if (idx < stopDisplayNames.size()) stopDisplayNames.remove(idx);
            }
        });

        LinearLayout.LayoutParams removeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        removeLp.setMargins(8, 0, 0, 0);
        removeBtn.setLayoutParams(removeLp);

        row.addView(stopInput);
        row.addView(removeBtn);

        container.addView(row);
        // ensure per-stop lists keep in sync
        hasSelectedStop.add(false);
        stopLatitudesSelected.add(0.0);
        stopLongitudesSelected.add(0.0);
        stopDisplayNames.add("");
        attachSuggestionHandlers(stopInput);
    }

    private void attachSuggestionHandlers(EditText input) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activeInput = input;
                suggestionsPopup.setAnchorView(input);
                suggestionsPopup.setWidth(input.getWidth());
                suggestionsPopup.setHeight(dpToPx(200));
                if (!suggestionsAdapter.isEmpty()) suggestionsPopup.show();
            } else {
                handler.postDelayed(() -> suggestionsPopup.dismiss(), 150);
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                activeInput = input;
                if (pendingRunnable != null) handler.removeCallbacks(pendingRunnable);
                String q = s.toString();
                pendingRunnable = () -> fetchSuggestions(q);
                handler.postDelayed(pendingRunnable, 300);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                suggestionsAdapter.clear();
                suggestionObjects.clear();
                suggestionsPopup.dismiss();
            });
            return;
        }

        new Thread(() -> {
            java.util.List<String> display = new java.util.ArrayList<>();
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
                            org.json.JSONObject props = f.optJSONObject("properties");
                            String name = null;
                            if (props != null) name = props.optString("name", props.optString("label", null));
                            if (name == null) name = f.optString("display_name", "");
                            display.add(name);
                            objs.add(f);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final java.util.List<String> finalDisplay = display;
            final java.util.List<org.json.JSONObject> finalObjs = objs;
            requireActivity().runOnUiThread(() -> {
                lastQuery = query;
                suggestionsAdapter.clear();
                suggestionsAdapter.addAll(finalObjs);
                suggestionObjects.clear();
                suggestionObjects.addAll(finalObjs);
                if (finalDisplay.isEmpty()) {
                    suggestionsPopup.dismiss();
                } else {
                    // only show if the input still has focus (prevents re-show after click)
                    if (activeInput != null && activeInput.isFocused()) {
                        suggestionsPopup.setAnchorView(activeInput);
                        suggestionsPopup.show();
                    }
                }
            });
        }).start();
    }

    // Fetch list of favorite routes from backend
    private void fetchFavoriteRoutes() {
        if (passengerService == null) return;
        Call<java.util.List<FavoriteRouteResponseDTO>> call = passengerService.getFavoriteRoutes();
        call.enqueue(new Callback<java.util.List<FavoriteRouteResponseDTO>>() {
            @Override
            public void onResponse(Call<java.util.List<FavoriteRouteResponseDTO>> call, Response<java.util.List<FavoriteRouteResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteObjects.clear();
                    favoriteObjects.addAll(response.body());
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (FavoriteRouteResponseDTO f : favoriteObjects) {
                        String n = f.getStartAddress() + " → " + f.getEndAddress();
                        names.add(n);
                    }
                    favoritesAdapter.clear();
                    favoritesAdapter.addAll(names);
                    favoritesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<java.util.List<FavoriteRouteResponseDTO>> call, Throwable t) {
                // ignore silently
            }
        });
    }

    // Apply a favorite route selection: fetch details if available and show on map
    private void applyFavoriteRoute(FavoriteRouteResponseDTO fav) {
        if (fav == null) return;
        currentSelectedFavoriteName = fav.getStartAddress() + " → " + fav.getEndAddress();
        favoriteSelected.setText(currentSelectedFavoriteName);
        currentSelectedFavoriteId = fav.getRouteId();

        if (currentSelectedFavoriteId != null && passengerService != null) {
            passengerService.getFavoriteRoute(currentSelectedFavoriteId).enqueue(new Callback<RouteResponseDTO>() {
                @Override
                public void onResponse(Call<RouteResponseDTO> call, Response<RouteResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RouteResponseDTO r = response.body();
                        try {
                            // set inputs
                            if (r.getStartAddress() != null) pickupInput.setText(r.getStartAddress());
                            if (r.getEndAddress() != null) destinationInput.setText(r.getEndAddress());
                            java.util.List<String> stops = r.getStopAddresses();
                            // clear existing stops container and re-add
                            View root = getView();
                                        if (root != null) {
                                            LinearLayout sc = root.findViewById(R.id.stopsContainer);
                                            if (sc != null) {
                                                sc.removeAllViews();
                                                if (stops != null) {
                                                    for (String s : stops) {
                                                        addStopInput(sc);
                                                        // set last added input text
                                                        int cnt = sc.getChildCount();
                                                        if (cnt > 0) {
                                                            View row = sc.getChildAt(cnt - 1);
                                                            if (row instanceof LinearLayout) {
                                                                // find first EditText in row and set text
                                                                for (int ci = 0; ci < ((LinearLayout) row).getChildCount(); ci++) {
                                                                    View c = ((LinearLayout) row).getChildAt(ci);
                                                                    if (c instanceof EditText) { ((EditText) c).setText(s); break; }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                // if backend provided stop coordinates, populate per-stop coordinate lists
                                                try {
                                                    if (stops != null && r.getStopLatitudes() != null && r.getStopLongitudes() != null
                                                            && r.getStopLatitudes().size() == r.getStopLongitudes().size()) {
                                                        int expected = stops.size();
                                                        while (hasSelectedStop.size() < expected) {
                                                            hasSelectedStop.add(false);
                                                            stopLatitudesSelected.add(0.0);
                                                            stopLongitudesSelected.add(0.0);
                                                            stopDisplayNames.add("");
                                                        }
                                                        for (int si = 0; si < expected; si++) {
                                                            hasSelectedStop.set(si, true);
                                                            stopLatitudesSelected.set(si, r.getStopLatitudes().get(si));
                                                            stopLongitudesSelected.set(si, r.getStopLongitudes().get(si));
                                                            stopDisplayNames.set(si, stops.get(si));
                                                        }
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        }

                            // set selected coordinates if provided
                            if (r.getStartLatitude() != null && r.getStartLongitude() != null) {
                                hasSelectedStart = true;
                                selectedStartLat = r.getStartLatitude();
                                selectedStartLon = r.getStartLongitude();
                                selectedStartDisplayName = r.getStartAddress();
                            }
                            if (r.getEndLatitude() != null && r.getEndLongitude() != null) {
                                hasSelectedEnd = true;
                                selectedEndLat = r.getEndLatitude();
                                selectedEndLon = r.getEndLongitude();
                                selectedEndDisplayName = r.getEndAddress();
                            }

                            // draw polyline if provided
                            java.util.List<PolylinePointDTO> polylinePoints = new java.util.ArrayList<>();
                            if (r.getRoute() != null) {
                                for (RoutePointDTO p : r.getRoute()) {
                                    PolylinePointDTO pp = new PolylinePointDTO();
                                    pp.setLatitude(p.getLat());
                                    pp.setLongitude(p.getLng());
                                    polylinePoints.add(pp);
                                }
                            } else if (r.getStopLatitudes() != null && r.getStopLongitudes() != null && r.getStopLatitudes().size() == r.getStopLongitudes().size()) {
                                // build simple polyline from start, stops, end
                                if (r.getStartLatitude() != null && r.getStartLongitude() != null) {
                                    PolylinePointDTO spt = new PolylinePointDTO(); spt.setLatitude(r.getStartLatitude()); spt.setLongitude(r.getStartLongitude()); polylinePoints.add(spt);
                                }
                                for (int i = 0; i < r.getStopLatitudes().size(); i++) {
                                    PolylinePointDTO sp = new PolylinePointDTO(); sp.setLatitude(r.getStopLatitudes().get(i)); sp.setLongitude(r.getStopLongitudes().get(i)); polylinePoints.add(sp);
                                }
                                if (r.getEndLatitude() != null && r.getEndLongitude() != null) {
                                    PolylinePointDTO ept = new PolylinePointDTO(); ept.setLatitude(r.getEndLatitude()); ept.setLongitude(r.getEndLongitude()); polylinePoints.add(ept);
                                }
                            }

                            LocationDTO startLoc = new LocationDTO();
                            startLoc.setLatitude(selectedStartLat);
                            startLoc.setLongitude(selectedStartLon);
                            startLoc.setAddress(selectedStartDisplayName);

                            LocationDTO endLoc = new LocationDTO();
                            endLoc.setLatitude(selectedEndLat);
                            endLoc.setLongitude(selectedEndLon);
                            endLoc.setAddress(selectedEndDisplayName);

                            if (routeMapView != null) {
                                routeMapView.clearMap();
                                routeMapView.displayRoute(startLoc, endLoc, null, polylinePoints);
                            }

                            // show estimates if available
                            try {
                                tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", r.getDistanceKm()));
                                tvDuration.setText(String.format(Locale.getDefault(), "Duration: %d min", r.getEstimatedTimeMinutes()));
                                resultsLayout.setVisibility(View.VISIBLE);
                            } catch (Exception ignored) {}
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<RouteResponseDTO> call, Throwable t) {
                    // ignore
                }
            });
        } else {
            // no id — apply client-side favorite if present
            if (fav.getStartAddress() != null) pickupInput.setText(fav.getStartAddress());
            if (fav.getEndAddress() != null) destinationInput.setText(fav.getEndAddress());
            java.util.List<String> stops = fav.getStopAddresses();
            View root = getView();
            if (root != null) {
                LinearLayout sc = root.findViewById(R.id.stopsContainer);
                if (sc != null) {
                    sc.removeAllViews();
                    if (stops != null) {
                        for (String s : stops) {
                            addStopInput(sc);
                            int cnt = sc.getChildCount();
                            if (cnt > 0) {
                                View row = sc.getChildAt(cnt - 1);
                                if (row instanceof LinearLayout) {
                                    // find first EditText in row and set text
                                    for (int ci = 0; ci < ((LinearLayout) row).getChildCount(); ci++) {
                                        View c = ((LinearLayout) row).getChildAt(ci);
                                        if (c instanceof EditText) { ((EditText) c).setText(s); break; }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
