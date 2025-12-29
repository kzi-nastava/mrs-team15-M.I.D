package com.example.ridenow.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ridenow.R;

public class DriverProfileInfoFragment extends Fragment {

    public DriverProfileInfoFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        ivAvatar.setOnClickListener(v -> Toast.makeText(getContext(), "Select avatar (not implemented)", Toast.LENGTH_SHORT).show());

        Button btnChange = view.findViewById(R.id.btnChangePasswordDriver);
        btnChange.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.change_password));

        Button btnSave = view.findViewById(R.id.btnSaveChangesDriver);
        btnSave.setOnClickListener(v -> Toast.makeText(getContext(), "Saved (not implemented)", Toast.LENGTH_SHORT).show());

        Spinner spVehicleType = view.findViewById(R.id.spVehicleType);
        if (spVehicleType != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.vehicle_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spVehicleType.setAdapter(adapter);

            int pos = adapter.getPosition("Standard");
            if (pos >= 0) spVehicleType.setSelection(pos);
        }

        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etAddress = view.findViewById(R.id.etAddress);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etLicensePlate = view.findViewById(R.id.etLicensePlate);
        EditText etVehicleModel = view.findViewById(R.id.etVehicleModel);
        EditText etVehicleSeats = view.findViewById(R.id.etVehicleSeats);

        if (etFirstName != null) etFirstName.setText("Ivan");
        if (etLastName != null) etLastName.setText("Ivic");
        if (etPhone != null) etPhone.setText("+381601234567");
        if (etAddress != null) etAddress.setText("Bulevar cara Lazara 1, Novi Sad");
        if (etEmail != null) etEmail.setText("ivan.ivic@example.com");
        if (etLicensePlate != null) etLicensePlate.setText("BG123-AB");
        if (etVehicleModel != null) etVehicleModel.setText("Toyota Prius");
        if (etVehicleSeats != null) etVehicleSeats.setText("4");

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        String first = etFirstName != null ? etFirstName.getText().toString() : "";
        String last = etLastName != null ? etLastName.getText().toString() : "";
        if (tvUserName != null) {
            String full = (first + " " + last).trim();
            if (full.isEmpty()) full = getString(R.string.nav_user_profile);
            tvUserName.setText(full);
        }

        ImageView ivDropdown = view.findViewById(R.id.ivDropdownArrow);
        if (ivDropdown != null && spVehicleType != null) {
            ivDropdown.setOnClickListener(v -> spVehicleType.performClick());
        }
    }
}
