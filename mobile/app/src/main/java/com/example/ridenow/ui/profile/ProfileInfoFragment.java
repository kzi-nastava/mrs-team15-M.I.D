package com.example.ridenow.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
    }
}
