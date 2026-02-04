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

public class RideOrderingFragment extends Fragment {
    private com.example.ridenow.ui.components.RouteMapView routeMapView;
    private ListPopupWindow suggestionsPopup;
    private SuggestionAdapter suggestionsAdapter;
    private java.util.List<org.json.JSONObject> suggestionObjects = new java.util.ArrayList<>();
    private String lastQuery = "";
    private android.os.Handler handler = new android.os.Handler();
    private Runnable pendingRunnable;
    private EditText activeInput;

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

        EditText pickup = view.findViewById(R.id.pickupAddress);
        EditText destination = view.findViewById(R.id.destinationAddress);
        LinearLayout stopsContainer = view.findViewById(R.id.stopsContainer);
        ImageButton addStopBtn = view.findViewById(R.id.addStopBtn);
        LinearLayout resultsLayout = view.findViewById(R.id.resultsLayout);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        TextView tvCost = view.findViewById(R.id.tvCost);
        Button showRouteBtn = view.findViewById(R.id.showRouteBtn);
        Button chooseRouteBtn = view.findViewById(R.id.chooseRouteBtn);

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
                        if (routeMapView != null) routeMapView.addTemporaryMarker(lat, lon, display);
                    }
                } catch (Exception ignored) {}
            }
            suggestionsPopup.dismiss();
        });

        // add initial stop input
        addStopInput(stopsContainer);

        addStopBtn.setOnClickListener(v -> addStopInput(stopsContainer));

        // attach suggestion handlers
        attachSuggestionHandlers(pickup);
        attachSuggestionHandlers(destination);

        showRouteBtn.setOnClickListener(v -> {
            // Dummy estimate values to mirror HomeFragment results layout
            String distance = "Distance: 5 km";
            String duration = "Estimated time: 12 minutes";
            String cost = "250 DIN";

            tvDistance.setText(distance);
            tvDuration.setText(duration);
            tvCost.setText(cost);
            resultsLayout.setVisibility(View.VISIBLE);
        });

        chooseRouteBtn.setOnClickListener(v -> {
            if (pickup.getText().toString().trim().isEmpty() || destination.getText().toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter pickup and destination addresses", Toast.LENGTH_SHORT).show();
                return;
            }
            // Navigate to ride preferences via NavController
            try {
                Navigation.findNavController(requireActivity(), com.example.ridenow.R.id.nav_host_fragment)
                        .navigate(com.example.ridenow.R.id.ride_preference);
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
        removeBtn.setOnClickListener(v -> container.removeView(row));

        LinearLayout.LayoutParams removeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        removeLp.setMargins(8, 0, 0, 0);
        removeBtn.setLayoutParams(removeLp);

        row.addView(stopInput);
        row.addView(removeBtn);

        container.addView(row);
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
                    if (activeInput != null) {
                        suggestionsPopup.setAnchorView(activeInput);
                        suggestionsPopup.show();
                    }
                }
            });
        }).start();
    }
}
