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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;

import com.example.ridenow.R;
import com.example.ridenow.dto.user.UserResponseDTO;
import com.example.ridenow.util.ClientUtils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileInfoFragment extends Fragment {

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<String> pickImageLauncher;

    public ProfileInfoFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // register image picker
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    selectedImageUri = result;
                    ImageView ivAvatarLocal = view.findViewById(R.id.ivAvatar);
                    try { Glide.with(requireContext()).load(selectedImageUri).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatarLocal); } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });

        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        ivAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        Button btnChange = view.findViewById(R.id.btnChangePassword);
        btnChange.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.change_password));

        Button btnSave = view.findViewById(R.id.btnSaveChanges);

        EditText etFirstName = view.findViewById(R.id.tvFirstName);
        EditText etLastName = view.findViewById(R.id.tvLastName);
        EditText etPhone = view.findViewById(R.id.tvPhoneNumber);
        EditText etAddress = view.findViewById(R.id.tvAddress);
        EditText etEmail = view.findViewById(R.id.etEmail);

        TextView tvUserName = view.findViewById(R.id.tvUserName);

        // Load user from backend
        com.example.ridenow.service.ProfileService profileService = ClientUtils.getClient(com.example.ridenow.service.ProfileService.class);
        profileService.getUser().enqueue(new retrofit2.Callback<UserResponseDTO>() {
            @Override
            public void onResponse(retrofit2.Call<UserResponseDTO> call, retrofit2.Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO user = response.body();
                    if (etFirstName != null) etFirstName.setText(nonNull(user.getFirstName()));
                    if (etLastName != null) etLastName.setText(nonNull(user.getLastName()));
                    if (etPhone != null) etPhone.setText(nonNull(user.getPhoneNumber()));
                    if (etAddress != null) etAddress.setText(nonNull(user.getAddress()));
                    if (etEmail != null) etEmail.setText(nonNull(user.getEmail()));
                    // load profile image if present
                    try {
                        String img = user.getProfileImage();
                        ImageView iv = view.findViewById(R.id.ivAvatar);
                        if (img != null && !img.trim().isEmpty()) {
                            String resolved = img;
                            if (img.startsWith("/")) resolved = ClientUtils.getServerBaseUrl() + img;
                            Glide.with(requireContext()).load(resolved).placeholder(R.drawable.ic_person).circleCrop().into(iv);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    String first = etFirstName != null ? etFirstName.getText().toString() : "";
                    String last = etLastName != null ? etLastName.getText().toString() : "";
                    if (tvUserName != null) {
                        String full = (first + " " + last).trim();
                        if (full.isEmpty()) full = getString(R.string.nav_user_profile);
                        tvUserName.setText(full);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserResponseDTO> call, Throwable t) {
                Toast.makeText(requireContext(), "Error loading profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            // Build multipart form fields from the edit texts
            Map<String, RequestBody> partMap = new HashMap<>();
            if (etFirstName != null) partMap.put("firstName", createPartFromString(etFirstName.getText().toString()));
            if (etLastName != null) partMap.put("lastName", createPartFromString(etLastName.getText().toString()));
            if (etPhone != null) partMap.put("phoneNumber", createPartFromString(etPhone.getText().toString()));
            if (etAddress != null) partMap.put("address", createPartFromString(etAddress.getText().toString()));
            if (etEmail != null) partMap.put("email", createPartFromString(etEmail.getText().toString()));

            // profile image (if selected) -> build multipart part
            MultipartBody.Part imagePart = null;
            if (selectedImageUri != null) {
                try {
                    byte[] bytes = readBytesFromUri(selectedImageUri);
                    String fileName = getFileNameFromUri(selectedImageUri);
                    RequestBody req = RequestBody.create(MediaType.parse("image/*"), bytes);
                    imagePart = MultipartBody.Part.createFormData("profileImage", fileName, req);
                } catch (IOException e) { e.printStackTrace(); Toast.makeText(requireContext(), "Failed to read selected image: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
            }

            profileService.updateUser(partMap, imagePart).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error saving profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private static RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value == null ? "" : value);
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        ContentResolver cr = requireContext().getContentResolver();
        try (InputStream is = cr.openInputStream(uri); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (is == null) throw new IOException("Unable to open input stream for uri");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) baos.write(buffer, 0, len);
            return baos.toByteArray();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        ContentResolver cr = requireContext().getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    result = cursor.getString(nameIndex);
                }
            } finally { cursor.close(); }
        }
        if (result == null) result = "profile.jpg";
        return result;
    }
}
