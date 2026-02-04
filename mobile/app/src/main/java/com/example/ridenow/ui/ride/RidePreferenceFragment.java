package com.example.ridenow.ui.ride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;
import com.example.ridenow.ui.components.RouteMapView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;

public class RidePreferenceFragment extends Fragment {

    public RidePreferenceFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_preference, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RouteMapView lifecycle will be forwarded from fragment lifecycle methods below
        RouteMapView routeMapView = view.findViewById(R.id.routeMapView);

        Spinner vehicleSpinner = view.findViewById(R.id.vehicleSpinner);
        Switch switchPet = view.findViewById(R.id.switchPet);
        Switch switchBaby = view.findViewById(R.id.switchBaby);
        EditText scheduledTime = view.findViewById(R.id.scheduledTime);
        LinearLayout guestsContainer = view.findViewById(R.id.guestsContainer);
        ImageButton addGuestBtn = view.findViewById(R.id.addGuestBtn);
        TextView finalPrice = view.findViewById(R.id.finalPrice);
        Button backBtn = view.findViewById(R.id.backBtn);
        Button orderRideBtn = view.findViewById(R.id.orderRideBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"", "Standard", "Luxury", "Van"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleSpinner.setAdapter(adapter);

        // add initial guest input
        addGuestInput(guestsContainer);

        addGuestBtn.setOnClickListener(v -> addGuestInput(guestsContainer));

        backBtn.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        orderRideBtn.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Ride ordered", Toast.LENGTH_SHORT).show();
        });

        // placeholder price
        finalPrice.setText("");
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

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
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

        row.addView(guestInput);
        row.addView(removeBtn);

        container.addView(row);
    }
}
