package com.example.ridenow.ui.ride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import com.example.ridenow.ui.components.RouteMapView;
import androidx.navigation.Navigation;
import androidx.core.content.ContextCompat;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;

public class RideOrderingFragment extends Fragment {

    public RideOrderingFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_ordering, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RouteMapView lifecycle is managed from the fragment lifecycle methods below
        RouteMapView routeMapView = view.findViewById(R.id.routeMapView);

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

        // add initial stop input
        addStopInput(stopsContainer);

        addStopBtn.setOnClickListener(v -> addStopInput(stopsContainer));

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
    }
}
