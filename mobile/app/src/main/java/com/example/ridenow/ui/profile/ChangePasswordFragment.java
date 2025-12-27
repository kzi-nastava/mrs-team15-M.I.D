package com.example.ridenow.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ridenow.R;
import com.example.ridenow.ui.auth.util.PasswordToggleUtil;

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
        tvBackToLogin.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.login)
        );

        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        PasswordToggleUtil.addPasswordToggle(etCurrentPassword);

        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        PasswordToggleUtil.addPasswordToggle(etNewPassword);

        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        PasswordToggleUtil.addPasswordToggle(etConfirmPassword);
    }
}