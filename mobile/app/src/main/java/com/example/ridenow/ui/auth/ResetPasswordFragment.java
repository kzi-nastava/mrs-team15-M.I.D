package com.example.ridenow.ui.auth;

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

import com.example.ridenow.R;

public class ResetPasswordFragment extends Fragment {

    public ResetPasswordFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_reset_password, container, false);
        TextView tvBackToLogin = view.findViewById(R.id.tvBackToLogin);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        addPasswordToggle(etNewPassword);
        addPasswordToggle(etConfirmPassword);

        tvBackToLogin.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.login)
        );
        return view;
    }

    private void addPasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) ->{
            if(event.getAction() == MotionEvent.ACTION_UP){
                int iconPosition = editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width();
                if(event.getRawX() >= iconPosition){
                    togglePasswordVisibility(editText);
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText editText) {
        if(editText.getTransformationMethod() instanceof PasswordTransformationMethod){
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            editText.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.visibility_off_24px, 0);
        }
        else{
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.visibility_24px, 0);
        }
    }
}