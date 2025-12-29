package com.example.ridenow.ui.auth.util;

import android.annotation.SuppressLint;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.EditText;

import com.example.ridenow.R;

public class PasswordToggleUtil {
    @SuppressLint("ClickableViewAccessibility")
    public static void  addPasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int iconPosition = editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width() - editText.getPaddingEnd();
                if (event.getRawX() >= iconPosition) {
                    togglePasswordVisibility(editText);
                    v.performClick();
                    return true;
                }
            }
            return false;
        });
    }
    private static void  togglePasswordVisibility(EditText editText) {
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
