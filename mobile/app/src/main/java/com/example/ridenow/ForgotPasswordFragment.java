package com.example.ridenow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForgotPasswordFragment extends Fragment {

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        TextView tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        tvBackToLogin.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.login)
        );
        return view;
    }
}
