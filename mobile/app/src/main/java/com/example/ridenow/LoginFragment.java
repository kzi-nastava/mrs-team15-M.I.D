package com.example.ridenow;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        EditText etPassword = view.findViewById(R.id.etPassword);

        // Add an event listener to the password input field
        etPassword.setOnTouchListener((v, event) -> {
            // If the user taps on the screen, check the bounds
            if(event.getAction() == MotionEvent.ACTION_UP){
                int iconPosition = etPassword.getRight() - etPassword.getCompoundDrawables()[2].getBounds().width();
                if(event.getRawX() >= iconPosition){
                    // The user clicked on the visibility icon
                    togglePasswordVisibility(etPassword);
                    return true;
                }
            }
            return false;
        });

         TextView tvForgotPasswordLink = view.findViewById(R.id.tvForgotPasswordLink);
        tvForgotPasswordLink.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.forgot_password)
        );
        return view;
    }

    private void togglePasswordVisibility(EditText etPassword) {
        if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod){
            // Password is hidden - show it
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etPassword.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.visibility_off_24px, 0);
        }
        else{
            // Password is visible - hide it
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etPassword.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.visibility_24px, 0);
        }
    }
}
