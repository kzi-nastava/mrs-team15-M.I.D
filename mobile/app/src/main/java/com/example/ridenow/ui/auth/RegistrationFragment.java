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

public class RegistrationFragment extends Fragment {

    public RegistrationFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_registration, container, false);

       TextView tvLogin = view.findViewById(R.id.tvLogin);
       tvLogin.setOnClickListener( v ->
                NavHostFragment.findNavController(this).navigate(R.id.login));

       EditText etPassword = view.findViewById(R.id.etPassword);
       PasswordToggleUtil.addPasswordToggle(etPassword);

       EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        PasswordToggleUtil.addPasswordToggle(etConfirmPassword);

       return view;
    }
}