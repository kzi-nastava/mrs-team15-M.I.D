package com.example.ridenow.ui.admin;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.service.AdminService;
import com.example.ridenow.util.ClientUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRegistrationFragment extends Fragment {
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etPhone;
    private EditText etAddress;
    private EditText etEmail;
    private EditText etLicensePlate;
    private EditText etVehicleModel;
    private EditText etSeats;
    private Spinner spinnerVehicleType;
    private Switch switchPetFriendly;
    private Switch switchBabyFriendly;
    private Button btnUploadPhoto;
    private Button btnRegisterDriver;
    private TextView tvImageStatus;
    private ImageView ivPreview;

    private Uri selectedImageUri;
    private ActivityResultLauncher<String> pickImageLauncher;
    private AdminService adminService;

    public DriverRegistrationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminService = ClientUtils.getClient(AdminService.class);

        if (!isAdminUser()) {
            Toast.makeText(requireContext(), "Access denied", Toast.LENGTH_SHORT).show();
            return;
        }

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        etEmail = view.findViewById(R.id.etEmail);
        etLicensePlate = view.findViewById(R.id.etLicensePlate);
        etVehicleModel = view.findViewById(R.id.etVehicleModel);
        etSeats = view.findViewById(R.id.etSeats);
        spinnerVehicleType = view.findViewById(R.id.spinnerVehicleType);
        switchPetFriendly = view.findViewById(R.id.switchPetFriendly);
        switchBabyFriendly = view.findViewById(R.id.switchBabyFriendly);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        btnRegisterDriver = view.findViewById(R.id.btnRegisterDriver);
        tvImageStatus = view.findViewById(R.id.tvImageStatus);
        ivPreview = view.findViewById(R.id.ivProfilePreview);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Standard", "Luksuz", "Kombi"}
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(typeAdapter);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreview.setImageURI(uri);
                        ivPreview.setVisibility(View.VISIBLE);
                        tvImageStatus.setText(getFileNameFromUri(uri));
                    }
                }
        );

        btnUploadPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnRegisterDriver.setOnClickListener(v -> registerDriver());
    }

    private boolean isAdminUser() {
        try {
            String role = ClientUtils.getTokenUtils().getRole();
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    private void registerDriver() {
        String firstName = valueOf(etFirstName);
        String lastName = valueOf(etLastName);
        String phone = valueOf(etPhone);
        String address = valueOf(etAddress);
        String email = valueOf(etEmail);
        String licensePlate = valueOf(etLicensePlate);
        String vehicleModel = valueOf(etVehicleModel);
        String seats = valueOf(etSeats);

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty() ||
                licensePlate.isEmpty() || vehicleModel.isEmpty() || seats.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!firstName.matches("^[A-ZČĆŠĐŽ][a-zčćšđž]+$")) {
            etFirstName.setError("First name must start with a capital letter and contain only letters");
            etFirstName.requestFocus();
            return;
        }

        if (!lastName.matches("^[A-ZČĆŠĐŽ][a-zčćšđž]+$")) {
            etLastName.setError("Last name must start with a capital letter and contain only letters");
            etLastName.requestFocus();
            return;
        }

        if (!phone.matches("^(\\+381|0)[0-9]{9,10}$")) {
            etPhone.setError("Invalid phone number format");
            etPhone.requestFocus();
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (!licensePlate.matches("^[A-Z]{2}[0-9]{3}[A-Z]{2}$")) {
            etLicensePlate.setError("License plate format is invalid (e.g. NS123AB)");
            etLicensePlate.requestFocus();
            return;
        }

        int seatCount;
        try {
            seatCount = Integer.parseInt(seats);
            if (seatCount < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            etSeats.setError("Seats must be at least 1");
            etSeats.requestFocus();
            return;
        }

        btnRegisterDriver.setEnabled(false);

        Map<String, RequestBody> partMap = new HashMap<>();
        partMap.put("firstName", createPart(firstName));
        partMap.put("lastName", createPart(lastName));
        partMap.put("phoneNumber", createPart(phone));
        partMap.put("address", createPart(address));
        partMap.put("email", createPart(email));
        partMap.put("licensePlate", createPart(licensePlate.toUpperCase()));
        partMap.put("vehicleModel", createPart(vehicleModel));
        partMap.put("vehicleType", createPart(mapVehicleType(String.valueOf(spinnerVehicleType.getSelectedItem()))));
        partMap.put("numberOfSeats", createPart(String.valueOf(seatCount)));
        partMap.put("babyFriendly", createPart(String.valueOf(switchBabyFriendly.isChecked())));
        partMap.put("petFriendly", createPart(String.valueOf(switchPetFriendly.isChecked())));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                byte[] bytes = readBytesFromUri(selectedImageUri);
                String fileName = getFileNameFromUri(selectedImageUri);
                RequestBody req = RequestBody.create(MediaType.parse("image/*"), bytes);
                imagePart = MultipartBody.Part.createFormData("profileImage", fileName, req);
            } catch (IOException e) {
                btnRegisterDriver.setEnabled(true);
                Toast.makeText(requireContext(), "Failed to read selected image", Toast.LENGTH_LONG).show();
                return;
            }
        }

        adminService.registerDriver(partMap, imagePart).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                btnRegisterDriver.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Driver registration submitted", Toast.LENGTH_LONG).show();
                    clearForm();
                } else {
                    Toast.makeText(requireContext(), parseErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                btnRegisterDriver.setEnabled(true);
                Toast.makeText(requireContext(), "Driver registration failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearForm() {
        etFirstName.setText("");
        etLastName.setText("");
        etPhone.setText("");
        etAddress.setText("");
        etEmail.setText("");
        etLicensePlate.setText("");
        etVehicleModel.setText("");
        etSeats.setText("");
        spinnerVehicleType.setSelection(0);
        switchPetFriendly.setChecked(false);
        switchBabyFriendly.setChecked(false);
        selectedImageUri = null;
        ivPreview.setImageDrawable(null);
        ivPreview.setVisibility(View.GONE);
        tvImageStatus.setText(R.string.no_image_selected);
    }

    private String mapVehicleType(String label) {
        String normalized = label == null ? "" : label.trim().toLowerCase();
        if (normalized.contains("lux") || normalized.equals("luksuz") || normalized.equals("luxury")) {
            return "LUXURY";
        }
        if (normalized.contains("van") || normalized.equals("kombi")) {
            return "VAN";
        }
        return "STANDARD";
    }

    private String valueOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static RequestBody createPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value == null ? "" : value);
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        ContentResolver resolver = requireContext().getContentResolver();
        try (InputStream inputStream = resolver.openInputStream(uri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Unable to open input stream");
            }
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        ContentResolver resolver = requireContext().getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
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
        return result == null ? "profile.jpg" : result;
    }

    private String parseErrorMessage(Response<?> response) {
        String errorMessage = "Driver registration failed";
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                if (errorBody.contains("\"message\"")) {
                    int start = errorBody.indexOf("\"message\":\"") + 11;
                    int end = errorBody.indexOf('"', start);
                    if (start > 10 && end > start) {
                        errorMessage = errorBody.substring(start, end);
                    }
                } else {
                    errorMessage = errorBody;
                    if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                        errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                    }
                }
            }
        } catch (Exception ignored) {
            errorMessage = "Driver registration failed (code: " + response.code() + ")";
        }
        return errorMessage;
    }
}