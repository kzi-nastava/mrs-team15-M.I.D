package com.example.ridenow.ui.driver.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ridenow.R;

public class DriverRequestDetailFragment extends Fragment {

    public DriverRequestDetailFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_request_detail, container, false);

        TextView requestIdTv = view.findViewById(R.id.tvRequestId);
        Button btnApprove = view.findViewById(R.id.btnApprove);
        Button btnReject = view.findViewById(R.id.btnReject);
        Button btnBack = view.findViewById(R.id.btnBack);
        TextView resultTv = view.findViewById(R.id.tvResultMessage);
        EditText adminNotes = view.findViewById(R.id.etAdminNotes);

        TextView tvNameCur = view.findViewById(R.id.tvNameCur);
        TextView tvNameProp = view.findViewById(R.id.tvNameProp);
        TextView tvPhoneCur = view.findViewById(R.id.tvPhoneCur);
        TextView tvPhoneProp = view.findViewById(R.id.tvPhoneProp);
        TextView tvEmailCur = view.findViewById(R.id.tvEmailCur);
        TextView tvEmailProp = view.findViewById(R.id.tvEmailProp);
        TextView tvLicenseCur = view.findViewById(R.id.tvLicenseCur);
        TextView tvLicenseProp = view.findViewById(R.id.tvLicenseProp);
        TextView tvModelCur = view.findViewById(R.id.tvModelCur);
        TextView tvModelProp = view.findViewById(R.id.tvModelProp);
        TextView tvVehicleTypeCur = view.findViewById(R.id.tvVehicleTypeCur);
        TextView tvVehicleTypeProp = view.findViewById(R.id.tvVehicleTypeProp);
        TextView tvSeatsCur = view.findViewById(R.id.tvSeatsCur);
        TextView tvSeatsProp = view.findViewById(R.id.tvSeatsProp);
        TextView tvAddressCur = view.findViewById(R.id.tvAddressCur);
        TextView tvAddressProp = view.findViewById(R.id.tvAddressProp);
        TextView tvPetCur = view.findViewById(R.id.tvPetCur);
        TextView tvPetProp = view.findViewById(R.id.tvPetProp);
        TextView tvBabyCur = view.findViewById(R.id.tvBabyCur);
        TextView tvBabyProp = view.findViewById(R.id.tvBabyProp);

        long requestId = requireArguments() != null ? requireArguments().getLong("requestId", -1L) : -1L;
        String avatarUrlCur = requireArguments() != null ? requireArguments().getString("avatarUrlCur", null) : null;
        String avatarUrlProp = requireArguments() != null ? requireArguments().getString("avatarUrlProp", null) : null;

        requestIdTv.setText(requestId == -1L ? "-" : String.valueOf(requestId));

        ImageView ivAvatarCur = view.findViewById(R.id.ivAvatarCur);
        ImageView ivAvatarProp = view.findViewById(R.id.ivAvatarProp);

        // load avatars if provided
        try {
            if (avatarUrlCur != null && !avatarUrlCur.isEmpty()) {
                String u = avatarUrlCur;
                if (!u.startsWith("http://") && !u.startsWith("https://")) {
                    u = com.example.ridenow.util.ClientUtils.getServerBaseUrl() + (u.startsWith("/") ? "" : "/") + u;
                }
                Glide.with(requireContext()).load(u).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatarCur);
            }
            if (avatarUrlProp != null && !avatarUrlProp.isEmpty()) {
                String u2 = avatarUrlProp;
                if (!u2.startsWith("http://") && !u2.startsWith("https://")) {
                    u2 = com.example.ridenow.util.ClientUtils.getServerBaseUrl() + (u2.startsWith("/") ? "" : "/") + u2;
                }
                Glide.with(requireContext()).load(u2).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatarProp);
            }
        } catch (Exception ignored) { }

        // read data passed from list (backend DTO mapped in DriverRequestsFragment)
        String firstName = requireArguments() != null ? requireArguments().getString("firstName", null) : null;
        String lastName = requireArguments() != null ? requireArguments().getString("lastName", null) : null;
        String email = requireArguments() != null ? requireArguments().getString("email", null) : null;
        String phone = requireArguments() != null ? requireArguments().getString("phone", null) : null;
        String address = requireArguments() != null ? requireArguments().getString("address", null) : null;
        String license = requireArguments() != null ? requireArguments().getString("license", null) : null;
        String model = requireArguments() != null ? requireArguments().getString("model", null) : null;
        String vehicleType = requireArguments() != null ? requireArguments().getString("vehicleType", null) : null;
        int seats = requireArguments() != null ? requireArguments().getInt("seats", -1) : -1;
        boolean pet = requireArguments() != null && requireArguments().getBoolean("pet", false);
        boolean baby = requireArguments() != null && requireArguments().getBoolean("baby", false);
        String status = requireArguments() != null ? requireArguments().getString("status", null) : null;

        String propName = (firstName == null && lastName == null) ? "-" : ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        String propPhone = phone == null ? "-" : phone;
        String propEmail = email == null ? "-" : email;
        String propLicense = license == null ? "-" : license;
        String propModel = model == null ? "-" : model;
        String propVehicleType = vehicleType == null ? "-" : vehicleType;
        String propSeats = seats == -1 ? "-" : String.valueOf(seats);

        // set proposed values immediately
        tvNameProp.setText(propName);
        tvPhoneProp.setText(propPhone);
        tvEmailProp.setText(propEmail);
        tvLicenseProp.setText(propLicense);
        tvModelProp.setText(propModel);
        tvVehicleTypeProp.setText(propVehicleType);
        tvSeatsProp.setText(propSeats);

        String propAddress = address == null ? "-" : address;
        String propPet = pet ? "Yes" : "No";
        String propBaby = baby ? "Yes" : "No";
        tvAddressProp.setText(propAddress);
        tvPetProp.setText(propPet);
        tvBabyProp.setText(propBaby);

        // backend-provided admin message and response date (may be null)
        String backendMessage = requireArguments() != null ? requireArguments().getString("message", null) : null;
        String backendResponseDate = requireArguments() != null ? requireArguments().getString("adminResponseDate", null) : null;

        // If request already processed, hide action buttons and show backend message (if any) in admin notes
        if (status != null && (status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("rejected"))) {
            // hide buttons
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            // show backend message if present, otherwise show simple status
            String baseMsg = backendMessage != null ? backendMessage : ("Request " + status);
            String dateStr = backendResponseDate != null ? backendResponseDate : "";
            String finalSuffix = status.equalsIgnoreCase("approved") ? ("\n\nRequest approved: " + requestId + (dateStr.isEmpty() ? "" : (" on " + dateStr))) : ("\n\nRequest rejected: " + requestId + (dateStr.isEmpty() ? "" : (" on " + dateStr)));
            adminNotes.setText(baseMsg + finalSuffix);
            adminNotes.setEnabled(false);
            resultTv.setVisibility(View.GONE);
        }

        // fetch current (existing) user info from backend when driverId is available
        long driverId = requireArguments() != null ? requireArguments().getLong("driverId", -1L) : -1L;
        if (driverId != -1L) {
            com.example.ridenow.service.AdminService adminService = com.example.ridenow.util.ClientUtils.getClient(com.example.ridenow.service.AdminService.class);
            adminService.getUserById(driverId).enqueue(new retrofit2.Callback<com.example.ridenow.dto.user.UserResponseDTO>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.ridenow.dto.user.UserResponseDTO> call, retrofit2.Response<com.example.ridenow.dto.user.UserResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.ridenow.dto.user.UserResponseDTO cur = response.body();
                        String curName = (cur.getFirstName() == null ? "" : cur.getFirstName()) + " " + (cur.getLastName() == null ? "" : cur.getLastName());
                        String curPhone = cur.getPhoneNumber() == null ? "-" : cur.getPhoneNumber();
                        String curEmail = cur.getEmail() == null ? "-" : cur.getEmail();
                        String curLicense = cur.getLicensePlate() == null ? "-" : cur.getLicensePlate();
                        String curModel = cur.getVehicleModel() == null ? "-" : cur.getVehicleModel();
                        String curVehicleType = cur.getVehicleType() == null ? "-" : cur.getVehicleType();
                        String curSeats = cur.getNumberOfSeats() <= 0 ? "-" : String.valueOf(cur.getNumberOfSeats());
                        String curAddress = cur.getAddress() == null ? "-" : cur.getAddress();
                        String curPet = cur.isPetFriendly() ? "Yes" : "No";
                        String curBaby = cur.isBabyFriendly() ? "Yes" : "No";

                        tvNameCur.setText(curName.trim().isEmpty() ? "-" : curName.trim());
                        tvPhoneCur.setText(curPhone);
                        tvEmailCur.setText(curEmail);
                        tvLicenseCur.setText(curLicense);
                        tvModelCur.setText(curModel);
                        tvVehicleTypeCur.setText(curVehicleType);
                        tvSeatsCur.setText(curSeats);
                        tvAddressCur.setText(curAddress);
                        tvPetCur.setText(curPet);
                        tvBabyCur.setText(curBaby);

                        // load current avatar if available
                        try {
                            String curImg = cur.getProfileImage();
                            if (curImg != null && !curImg.isEmpty()) {
                                String u = curImg;
                                if (!u.startsWith("http://") && !u.startsWith("https://")) {
                                    u = com.example.ridenow.util.ClientUtils.getServerBaseUrl() + (u.startsWith("/") ? "" : "/") + u;
                                }
                                Glide.with(requireContext()).load(u).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatarCur);
                            }
                        } catch (Exception ignored) {}

                        // apply highlighting comparing current vs proposed
                        int changedBg = 0xFFE6EEF8; // #E6EEF8
                        if (!equalsNullSafe(tvNameCur.getText().toString(), tvNameProp.getText().toString())) tvNameProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvPhoneCur.getText().toString(), tvPhoneProp.getText().toString())) tvPhoneProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvEmailCur.getText().toString(), tvEmailProp.getText().toString())) tvEmailProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvLicenseCur.getText().toString(), tvLicenseProp.getText().toString())) tvLicenseProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvModelCur.getText().toString(), tvModelProp.getText().toString())) tvModelProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvVehicleTypeCur.getText().toString(), tvVehicleTypeProp.getText().toString())) tvVehicleTypeProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvSeatsCur.getText().toString(), tvSeatsProp.getText().toString())) tvSeatsProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvAddressCur.getText().toString(), tvAddressProp.getText().toString())) tvAddressProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvPetCur.getText().toString(), tvPetProp.getText().toString())) tvPetProp.setBackgroundColor(changedBg);
                        if (!equalsNullSafe(tvBabyCur.getText().toString(), tvBabyProp.getText().toString())) tvBabyProp.setBackgroundColor(changedBg);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.ridenow.dto.user.UserResponseDTO> call, Throwable t) {
                    // leave current values as dashes
                }
            });
        } else {
            // No driverId available: set current values to dashes and highlight if proposal differs from dash
            tvNameCur.setText("-");
            tvPhoneCur.setText("-");
            tvEmailCur.setText("-");
            tvLicenseCur.setText("-");
            tvModelCur.setText("-");
            tvVehicleTypeCur.setText("-");
            tvSeatsCur.setText("-");
            tvAddressCur.setText("-");
            tvPetCur.setText("-");
            tvBabyCur.setText("-");

            int changedBg = 0xFFE6EEF8; // #E6EEF8
            if (!equalsNullSafe("-", tvNameProp.getText().toString())) tvNameProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvPhoneProp.getText().toString())) tvPhoneProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvEmailProp.getText().toString())) tvEmailProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvLicenseProp.getText().toString())) tvLicenseProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvModelProp.getText().toString())) tvModelProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvVehicleTypeProp.getText().toString())) tvVehicleTypeProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvSeatsProp.getText().toString())) tvSeatsProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvAddressProp.getText().toString())) tvAddressProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvPetProp.getText().toString())) tvPetProp.setBackgroundColor(changedBg);
            if (!equalsNullSafe("-", tvBabyProp.getText().toString())) tvBabyProp.setBackgroundColor(changedBg);
        }

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnApprove.setOnClickListener(v -> {
            String notes = adminNotes.getText().toString();
            com.example.ridenow.dto.admin.AdminChangesReviewRequestDTO dto = new com.example.ridenow.dto.admin.AdminChangesReviewRequestDTO();
            dto.setApproved(true);
            dto.setMessage(notes);
            com.example.ridenow.service.AdminService adminService = com.example.ridenow.util.ClientUtils.getClient(com.example.ridenow.service.AdminService.class);
            adminService.reviewDriverRequest(requestId, dto).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    View parent = requireActivity().findViewById(android.R.id.content);
                    Snackbar.make(parent, "Request approved: " + requestId, Snackbar.LENGTH_LONG).show();
                    // append current timestamp to admin notes like the post-action message
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String now = sdf.format(new Date());
                    adminNotes.setText(notes + "\n\nRequest approved: " + requestId + " on " + now);
                    adminNotes.setEnabled(false);
                    resultTv.setVisibility(View.GONE);
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    View parent = requireActivity().findViewById(android.R.id.content);
                    Snackbar.make(parent, "Failed to approve request: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    resultTv.setVisibility(View.GONE);
                }
            });
        });

        btnReject.setOnClickListener(v -> {
            String notes = adminNotes.getText().toString();
            com.example.ridenow.dto.admin.AdminChangesReviewRequestDTO dto = new com.example.ridenow.dto.admin.AdminChangesReviewRequestDTO();
            dto.setApproved(false);
            dto.setMessage(notes);
            com.example.ridenow.service.AdminService adminService = com.example.ridenow.util.ClientUtils.getClient(com.example.ridenow.service.AdminService.class);
            adminService.reviewDriverRequest(requestId, dto).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    View parent = requireActivity().findViewById(android.R.id.content);
                    Snackbar.make(parent, "Request rejected: " + requestId, Snackbar.LENGTH_LONG).show();
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String now2 = sdf2.format(new Date());
                    adminNotes.setText(notes + "\n\nRequest rejected: " + requestId + " on " + now2);
                    adminNotes.setEnabled(false);
                    resultTv.setVisibility(View.GONE);
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    View parent = requireActivity().findViewById(android.R.id.content);
                    Snackbar.make(parent, "Failed to reject request: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    resultTv.setVisibility(View.GONE);
                }
            });
        });

        // apply approve button green style programmatically as fallback
        try {
            btnApprove.setTextColor(getResources().getColor(android.R.color.white));
        } catch (Exception ignored) {}

        return view;
    }

    private boolean equalsNullSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

}

