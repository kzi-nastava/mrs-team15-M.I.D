package com.example.ridenow.ui.currentride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.ridenow.R;
import com.example.ridenow.dto.ride.StopRideResponseDTO;
import com.example.ridenow.service.RideService;
import com.example.ridenow.util.ClientUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StopRideDialog extends BottomSheetDialogFragment {

    public interface OnRideStoppedListener {
        void onRideStopped(StopRideResponseDTO result);
    }

    private OnRideStoppedListener listener;
    private RideService rideService;

    private  Button btnCancel;
    private  Button btnConfirmStop;

    public static StopRideDialog newInstance() {
        return new StopRideDialog();
    }

    public void setOnRideStoppedListener(OnRideStoppedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stop_ride_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rideService = ClientUtils.getClient(RideService.class);

        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirmStop = view.findViewById(R.id.btnConfirmStop);

        btnCancel.setOnClickListener(v -> dismiss());
        btnConfirmStop.setOnClickListener(v -> handleStopRide());
    }

    private void handleStopRide() {
        btnConfirmStop.setEnabled(false);
        btnConfirmStop.setText("Stopping...");

        rideService.stopRide().enqueue(new Callback<StopRideResponseDTO>() {
            @Override
            public void onResponse(Call<StopRideResponseDTO> call, Response<StopRideResponseDTO> response) {
                if(response.isSuccessful() && response.body() != null){
                    if(listener != null){
                        listener.onRideStopped(response.body());
                    }
                    dismiss();
                }else{
                    btnConfirmStop.setEnabled(true);
                    btnConfirmStop.setText(R.string.stop_ride_confirm);
                    android.widget.Toast.makeText(getContext(), "Failed to stop ride", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StopRideResponseDTO> call, Throwable t) {
                btnConfirmStop.setEnabled(true);
                btnConfirmStop.setText(R.string.stop_ride_confirm);
                android.widget.Toast.makeText(getContext(), "Network error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });

    }
}