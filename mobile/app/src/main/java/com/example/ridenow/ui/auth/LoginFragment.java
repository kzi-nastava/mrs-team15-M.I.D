package com.example.ridenow.ui.auth;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ridenow.R;
import com.example.ridenow.ui.auth.util.PasswordToggleUtil;

public class LoginFragment extends Fragment {

    public LoginFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText etPassword = view.findViewById(R.id.etPassword);
        PasswordToggleUtil.addPasswordToggle(etPassword);

        TextView tvSignUp = view.findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.registration);
        });

        TextView tvForgotPasswordLink = view.findViewById(R.id.tvForgotPasswordLink);
        tvForgotPasswordLink.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.forgot_password)
        );

        return view;
    }
}
