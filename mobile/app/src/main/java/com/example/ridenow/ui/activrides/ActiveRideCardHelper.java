package com.example.ridenow.ui.activrides;

import android.view.View;
import android.widget.TextView;

import com.example.ridenow.R;
import com.example.ridenow.dto.ride.ActiveRideDTO;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.DateUtils;

public class ActiveRideCardHelper {

    public static void setupCard(View cardView, ActiveRideDTO ride) {
        TextView rideIdTextView = cardView.findViewById(R.id.rideIdTextView);
        TextView routeTextView = cardView.findViewById(R.id.routeTextView);
        TextView driverNameTextView = cardView.findViewById(R.id.driverNameTextView);
        TextView passengersTextView = cardView.findViewById(R.id.passengersTextView);
        TextView startTimeTextView = cardView.findViewById(R.id.startTimeTextView);
        TextView panicStatusTextView = cardView.findViewById(R.id.panicStatusTextView);

        // Set ride ID
        rideIdTextView.setText(String.valueOf(ride.getRideId()));

        // Set route information
        if (ride.getRoute() != null && ride.getRoute().getStartLocation() != null
            && ride.getRoute().getEndLocation() != null) {
            String startAddress = AddressUtils.formatAddress(ride.getRoute().getStartLocation().getAddress());
            String endAddress = AddressUtils.formatAddress(ride.getRoute().getEndLocation().getAddress());
            routeTextView.setText(cardView.getContext().getString(R.string.route_format, startAddress, endAddress));
        } else {
            routeTextView.setText(R.string.route_unavailable);
        }

        // Set driver name
        driverNameTextView.setText(ride.getDriverName() != null ? ride.getDriverName() : "Unknown Driver");

        // Set passengers
        if (ride.getPassengerNames() != null && !ride.getPassengerNames().isEmpty()) {
            passengersTextView.setText(ride.getPassengerNames());
        } else {
            passengersTextView.setText(R.string.no_passengers);
        }

        // Set start time
        if (ride.getStartTime() != null && !ride.getStartTime().isEmpty()) {
            // Try to format the ISO string date to a readable format
            try {
                String formattedDate = DateUtils.formatDateFromISO(ride.getStartTime());
                String formattedTime = DateUtils.formatTimeFromISO(ride.getStartTime());
                if (!"N/A".equals(formattedDate) && !"N/A".equals(formattedTime)) {
                    startTimeTextView.setText(cardView.getContext().getString(R.string.date_time_format, formattedDate, formattedTime));
                } else {
                    startTimeTextView.setText(ride.getStartTime()); // Fallback to original string
                }
            } catch (Exception e) {
                startTimeTextView.setText(ride.getStartTime()); // Fallback to original string
            }
        } else {
            startTimeTextView.setText(R.string.start_time_unavailable);
        }

        // Set panic status
        if (ride.getPanic() != null && ride.getPanic()) {
            panicStatusTextView.setTextColor(cardView.getContext().getColor(R.color.danger));
            panicStatusTextView.setVisibility(View.VISIBLE);

            if (ride.getPanicBy() != null) {
                panicStatusTextView.setText(cardView.getContext().getString(R.string.panic_alert_by, ride.getPanicBy()));
            } else {
                panicStatusTextView.setText(R.string.panic_alert);
            }
        } else {
            panicStatusTextView.setVisibility(View.GONE);
        }
    }
}
