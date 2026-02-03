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

        int requestId = requireArguments() != null ? requireArguments().getInt("requestId", -1) : -1;
        String avatarUrlCur = requireArguments() != null ? requireArguments().getString("avatarUrlCur", null) : null;
        String avatarUrlProp = requireArguments() != null ? requireArguments().getString("avatarUrlProp", null) : null;

        requestIdTv.setText(requestId == -1 ? "-" : String.valueOf(requestId));

        ImageView ivAvatarCur = view.findViewById(R.id.ivAvatarCur);
        ImageView ivAvatarProp = view.findViewById(R.id.ivAvatarProp);

        // load avatars if provided
        try {
            if (avatarUrlCur != null && !avatarUrlCur.isEmpty()) {
                Glide.with(requireContext()).load(avatarUrlCur).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatarCur);
            }
            if (avatarUrlProp != null && !avatarUrlProp.isEmpty()) {
                Glide.with(requireContext()).load(avatarUrlProp).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatarProp);
            }
        } catch (Exception ignored) { }

        // Example placeholder data for original and changed driver (replace with backend model)
        String curName = "Ana Marković";
        String propName = "Ana Marković"; // same
        String curPhone = "0601234567";
        String propPhone = "0601234567";
        String curEmail = "ana@example.com";
        String propEmail = "ana.new@example.com"; // changed example
        String curLicense = "NS123AB";
        String propLicense = "NS999ZZ"; // changed example
        String curModel = "VW Golf";
        String propModel = "Opel Astra"; // changed example
        String curVehicleType = "Hatchback";
        String propVehicleType = "Sedan";
        String curSeats = "5";
        String propSeats = "4";

        tvNameCur.setText(curName);
        tvNameProp.setText(propName);
        tvPhoneCur.setText(curPhone);
        tvPhoneProp.setText(propPhone);
        tvEmailCur.setText(curEmail);
        tvEmailProp.setText(propEmail);
        tvLicenseCur.setText(curLicense);
        tvLicenseProp.setText(propLicense);
        tvModelCur.setText(curModel);
        tvModelProp.setText(propModel);
        tvVehicleTypeCur.setText(curVehicleType);
        tvVehicleTypeProp.setText(propVehicleType);
        tvSeatsCur.setText(curSeats);
        tvSeatsProp.setText(propSeats);

        // placeholder address / pet / baby values
        String curAddress = "Current address 1";
        String propAddress = "Proposed address 1";
        String curPet = "No";
        String propPet = "Yes";
        String curBaby = "Yes";
        String propBaby = "Yes";

        tvAddressCur.setText(curAddress);
        tvAddressProp.setText(propAddress);
        tvPetCur.setText(curPet);
        tvPetProp.setText(propPet);
        tvBabyCur.setText(curBaby);
        tvBabyProp.setText(propBaby);

        // highlight proposed fields when they differ (light-blue)
        int changedBg = 0xFFE6EEF8; // #E6EEF8
        if (!equalsNullSafe(curName, propName)) tvNameProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curPhone, propPhone)) tvPhoneProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curEmail, propEmail)) tvEmailProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curLicense, propLicense)) tvLicenseProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curModel, propModel)) tvModelProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curVehicleType, propVehicleType)) tvVehicleTypeProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curSeats, propSeats)) tvSeatsProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curAddress, propAddress)) tvAddressProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curPet, propPet)) tvPetProp.setBackgroundColor(changedBg);
        if (!equalsNullSafe(curBaby, propBaby)) tvBabyProp.setBackgroundColor(changedBg);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnApprove.setOnClickListener(v -> {
            String notes = adminNotes.getText().toString();
            Toast.makeText(requireContext(), "Approved request " + requestId + (notes.isEmpty() ? "" : (": " + notes)), Toast.LENGTH_SHORT).show();
        });

        btnReject.setOnClickListener(v -> {
            String notes = adminNotes.getText().toString();
            Toast.makeText(requireContext(), "Rejected request " + requestId + (notes.isEmpty() ? "" : (": " + notes)), Toast.LENGTH_SHORT).show();
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

