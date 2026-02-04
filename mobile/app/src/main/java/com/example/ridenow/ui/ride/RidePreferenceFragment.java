package com.example.ridenow.ui.ride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;

public class RidePreferenceFragment extends Fragment {

    private com.example.ridenow.ui.components.RouteMapView routeMapView;

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

        // initialize map view (follow HomeFragment pattern)
        routeMapView = view.findViewById(R.id.routeMapView);
        if (routeMapView != null) {
            // center on Novi Sad by default
            routeMapView.centerOnLocation(45.2671, 19.8335);
        }

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
        // Match other inputs: dark background with white border/text
        vehicleSpinner.setBackgroundResource(R.drawable.edittext_with_bg);
        vehicleSpinner.setSelection(0);

        // add initial guest input
        addGuestInput(guestsContainer);

        // show Date and Time pickers when scheduledTime is clicked
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
}
