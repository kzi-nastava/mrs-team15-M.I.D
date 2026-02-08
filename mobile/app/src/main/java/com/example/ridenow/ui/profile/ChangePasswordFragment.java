package com.example.ridenow.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.user.ChangePasswordRequestDTO;
import com.example.ridenow.service.ProfileService;
import com.example.ridenow.util.ClientUtils;

public class ChangePasswordFragment extends Fragment {
    public ChangePasswordFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvBackToLogin = view.findViewById(R.id.tvBackToLogin);
        // Use navigateUp() so this returns to whichever fragment navigated here (profile or driver profile)
        tvBackToLogin.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigateUp()
        );

        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        com.example.ridenow.ui.auth.util.PasswordToggleUtil.addPasswordToggle(etCurrentPassword);

        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        com.example.ridenow.ui.auth.util.PasswordToggleUtil.addPasswordToggle(etNewPassword);

        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        com.example.ridenow.ui.auth.util.PasswordToggleUtil.addPasswordToggle(etConfirmPassword);

        Button btnSavePassword = view.findViewById(R.id.btnSavePassword);

        ProfileService profileService = ClientUtils.getClient(ProfileService.class);

        btnSavePassword.setOnClickListener(v -> {
            // validate
            String current = etCurrentPassword.getText() == null ? "" : etCurrentPassword.getText().toString().trim();
            String nw = etNewPassword.getText() == null ? "" : etNewPassword.getText().toString().trim();
            String conf = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString().trim();

            if (current.isEmpty()) {
                etCurrentPassword.setError("Current password required");
                etCurrentPassword.requestFocus();
                return;
            }
            if (nw.isEmpty()) {
                etNewPassword.setError("New password required");
                etNewPassword.requestFocus();
                return;
            }
            if (nw.length() < 6) {
                etNewPassword.setError("Password must be at least 6 characters");
                etNewPassword.requestFocus();
                return;
            }
            if (!nw.equals(conf)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            // disable button to prevent double submit
            btnSavePassword.setEnabled(false);

            ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
            dto.setCurrentPassword(current);
            dto.setNewPassword(nw);
            dto.setConfirmNewPassword(conf);

            profileService.changePassword(dto).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    btnSavePassword.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Password changed", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(ChangePasswordFragment.this).navigateUp();
                    } else if (response.code() == 401 || response.code() == 403) {
                        Toast.makeText(requireContext(), "Current password incorrect or unauthorized", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to change password: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    btnSavePassword.setEnabled(true);
                    Toast.makeText(requireContext(), "Error changing password: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}