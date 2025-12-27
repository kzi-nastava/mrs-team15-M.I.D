package com.example.ridenow.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ridenow.R;

public class ProfileInfoFragment extends Fragment {

    public ProfileInfoFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        ivAvatar.setOnClickListener(v -> Toast.makeText(getContext(), "Select avatar (not implemented)", Toast.LENGTH_SHORT).show());

        Button btnChange = view.findViewById(R.id.btnChangePassword);
        btnChange.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.change_password));

        Button btnSave = view.findViewById(R.id.btnSaveChanges);
        btnSave.setOnClickListener(v -> Toast.makeText(getContext(), "Saved (not implemented)", Toast.LENGTH_SHORT).show());

        EditText etFirstName = view.findViewById(R.id.tvFirstName);
        EditText etLastName = view.findViewById(R.id.tvLastName);
        EditText etPhone = view.findViewById(R.id.tvPhoneNumber);
        EditText etAddress = view.findViewById(R.id.tvAddress);
        EditText etEmail = view.findViewById(R.id.etEmail);

        if (etFirstName != null) etFirstName.setText("Marko");
        if (etLastName != null) etLastName.setText("Markovic");
        if (etPhone != null) etPhone.setText("+381641112233");
        if (etAddress != null) etAddress.setText("Dunavska 10, Novi Sad");
        if (etEmail != null) etEmail.setText("marko.markovic@example.com");

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        String first = etFirstName != null ? etFirstName.getText().toString() : "";
        String last = etLastName != null ? etLastName.getText().toString() : "";
        if (tvUserName != null) {
            String full = (first + " " + last).trim();
            if (full.isEmpty()) full = getString(R.string.nav_user_profile);
            tvUserName.setText(full);
        }
    }
}
