package com.example.ridenow.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
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
import com.example.ridenow.dto.driver.DriverChangeResponseDTO;
import com.example.ridenow.dto.user.UserResponseDTO;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.util.ClientUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DriverProfileInfoFragment extends Fragment {

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<String> pickImageLauncher;

    public DriverProfileInfoFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Register image picker launcher
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    selectedImageUri = result;
                    ImageView ivAvatarLocal = view.findViewById(R.id.ivAvatar);
                    try {
                        Glide.with(requireContext()).load(selectedImageUri).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatarLocal);
                    } catch (Exception e) {
                        Log.w("DriverProfile", "Failed to load selected image", e);
                    }
                }
            }
        });

        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        ivAvatar.setOnClickListener(v -> {
            // Open system picker for images
            pickImageLauncher.launch("image/*");
        });

        Button btnChange = view.findViewById(R.id.btnChangePasswordDriver);
        btnChange.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.change_password));

        Button btnSave = view.findViewById(R.id.btnSaveChangesDriver);

        Spinner spVehicleType = view.findViewById(R.id.spVehicleType);
        if (spVehicleType != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.vehicle_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spVehicleType.setAdapter(adapter);

            int pos = adapter.getPosition("Standard");
            if (pos >= 0) spVehicleType.setSelection(pos);
        }

        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etAddress = view.findViewById(R.id.etAddress);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etLicensePlate = view.findViewById(R.id.etLicensePlate);
        EditText etVehicleModel = view.findViewById(R.id.etVehicleModel);
        EditText etVehicleSeats = view.findViewById(R.id.etVehicleSeats);
        SwitchCompat cbBabyFriendly = view.findViewById(R.id.cbBabyFriendly);
        SwitchCompat cbPetFriendly = view.findViewById(R.id.cbPetFriendly);

        // TextView for username (will be updated after loading profile)
        TextView tvUserName = view.findViewById(R.id.tvUserName);

        // Load driver profile from backend
        DriverService driverService = ClientUtils.getClient(DriverService.class);
        driverService.getUser().enqueue(new retrofit2.Callback<UserResponseDTO>() {
            @Override
            public void onResponse(retrofit2.Call<UserResponseDTO> call, retrofit2.Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO user = response.body();
                    if (etFirstName != null) etFirstName.setText(nonNull(user.getFirstName()));
                    if (etLastName != null) etLastName.setText(nonNull(user.getLastName()));
                    if (etPhone != null) etPhone.setText(nonNull(user.getPhoneNumber()));
                    if (etAddress != null) etAddress.setText(nonNull(user.getAddress()));
                    if (etEmail != null) etEmail.setText(nonNull(user.getEmail()));
                    if (etLicensePlate != null) etLicensePlate.setText(nonNull(user.getLicensePlate()));
                    if (etVehicleModel != null) etVehicleModel.setText(nonNull(user.getVehicleModel()));
                    if (etVehicleSeats != null) etVehicleSeats.setText(String.valueOf(user.getNumberOfSeats()));
                    if (cbBabyFriendly != null) cbBabyFriendly.setChecked(user.isBabyFriendly());
                    if (cbPetFriendly != null) cbPetFriendly.setChecked(user.isPetFriendly());
                    // vehicle type selection
                    if (spVehicleType != null && user.getVehicleType() != null) {
                        ArrayAdapter adapter = (ArrayAdapter) spVehicleType.getAdapter();
                        if (adapter != null) {
                            int pos = adapter.getPosition(user.getVehicleType());
                            if (pos >= 0) spVehicleType.setSelection(pos);
                        }
                    }
                    // profile image (if available) - try to load directly; if it's not a full URL the developer may need to adapt
                    try {
                        String img = user.getProfileImage();
                        if (img != null && !img.trim().isEmpty()) {
                            String resolved = img;
                            if (img.startsWith("/")) {
                                resolved = ClientUtils.getServerBaseUrl() + img;
                            }
                            Glide.with(requireContext()).load(resolved).placeholder(R.drawable.ic_person).circleCrop().into(ivAvatar);
                        }
                    } catch (Exception e) {
                        Log.w("DriverProfile", "Failed to load profile image", e);
                    }
                     // Update displayed user name
                     if (tvUserName != null) {
                         String first = nonNull(user.getFirstName());
                         String last = nonNull(user.getLastName());
                         String full = (first + " " + last).trim();
                         if (full.isEmpty()) full = getString(R.string.nav_user_profile);
                         tvUserName.setText(full);
                     }
                     Log.i("DriverProfile", "Loaded driver profile successfully");
                 } else {
                     String err = "";
                     try {
                         if (response.errorBody() != null) err = response.errorBody().string();
                     } catch (IOException e) {
                         err = e.getMessage();
                     }
                     Log.e("DriverProfile", "Failed to load driver profile: code=" + response.code() + ", body=" + err);
                     Toast.makeText(requireContext(), "Failed to load driver profile (" + response.code() + "): " + err, Toast.LENGTH_LONG).show();
                 }
             }

             @Override
             public void onFailure(retrofit2.Call<UserResponseDTO> call, Throwable t) {
                 Log.e("DriverProfile", "Error loading profile", t);
                 Toast.makeText(requireContext(), "Error loading profile: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
            if (etLicensePlate != null) partMap.put("licensePlate", createPartFromString(etLicensePlate.getText().toString()));
            if (etVehicleModel != null) partMap.put("vehicleModel", createPartFromString(etVehicleModel.getText().toString()));
            if (etVehicleSeats != null) partMap.put("vehicleSeats", createPartFromString(etVehicleSeats.getText().toString()));
            // Prepare image part if user selected an image
            MultipartBody.Part imagePart = null;
            if (selectedImageUri != null) {
                try {
                    byte[] bytes = readBytesFromUri(selectedImageUri);
                    String fileName = getFileNameFromUri(selectedImageUri);
                    RequestBody req = RequestBody.create(MediaType.parse("image/*"), bytes);
                    imagePart = MultipartBody.Part.createFormData("profileImage", fileName, req);
                } catch (IOException e) {
                    Log.e("DriverProfile", "Failed to read selected image", e);
                    Toast.makeText(requireContext(), "Failed to read selected image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    imagePart = null;
                }
            }

            driverService.requestDriverChange(partMap, imagePart).enqueue(new retrofit2.Callback<DriverChangeResponseDTO>() {
                @Override
                public void onResponse(retrofit2.Call<DriverChangeResponseDTO> call, retrofit2.Response<DriverChangeResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DriverChangeResponseDTO resp = response.body();
                        Toast.makeText(requireContext(), "Change requested: " + resp.getStatus(), Toast.LENGTH_SHORT).show();
                    } else {
                        String err = "";
                        try {
                            if (response.errorBody() != null) err = response.errorBody().string();
                        } catch (IOException e) { err = e.getMessage(); }
                        Log.e("DriverProfile", "Change request failed: code=" + response.code() + ", body=" + err);
                        Toast.makeText(requireContext(), "Failed to request change (" + response.code() + "): " + err, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<DriverChangeResponseDTO> call, Throwable t) {
                    Log.e("DriverProfile", "Error requesting change", t);
                    Toast.makeText(requireContext(), "Error requesting change: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        ImageView ivDropdown = view.findViewById(R.id.ivDropdownArrow);
        if (ivDropdown != null && spVehicleType != null) {
            ivDropdown.setOnClickListener(v -> spVehicleType.performClick());
        }
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
            } finally {
                cursor.close();
            }
        }
        if (result == null) result = "profile.jpg";
        return result;
    }

    private static RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value == null ? "" : value);
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }
}
