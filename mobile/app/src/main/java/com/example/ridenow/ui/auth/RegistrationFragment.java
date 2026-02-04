package com.example.ridenow.ui.auth;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridenow.R;
import com.example.ridenow.dto.auth.RegisterResponseDTO;
import com.example.ridenow.service.AuthService;
import com.example.ridenow.ui.auth.util.PasswordToggleUtil;
import com.example.ridenow.util.ClientUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RegistrationFragment extends Fragment {
    private TextView tvFirstName;
    private TextView tvLastName;
    private TextView tvPhoneNumber;
    private TextView tvAddress;
    private TextView etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnUploadPhoto;
    private Button btnSignUp;
    private TextView tvLogin;
    private Uri selectedImageUri = null;
    private ActivityResultLauncher<String> pickImageLauncher;
    private AuthService authService;

    public RegistrationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authService = ClientUtils.getClient(AuthService.class);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        selectedImageUri = result;
                        String fileName = getFileNameFromUri(selectedImageUri);
                        btnUploadPhoto.setText("Photo: " + fileName);
                        Toast.makeText(requireContext(), "Photo selected: " + fileName, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        tvFirstName = view.findViewById(R.id.tvFirstName);
        tvLastName = view.findViewById(R.id.tvLastName);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvAddress = view.findViewById(R.id.tvAddress);
        etEmail = view.findViewById(R.id.etEmail);

        etPassword = view.findViewById(R.id.etPassword);
        PasswordToggleUtil.addPasswordToggle(etPassword);

        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        PasswordToggleUtil.addPasswordToggle(etConfirmPassword);

        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        btnUploadPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSignUp = view.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> register());

        tvLogin = view.findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.login)
        );
    }

    private void register() {
        String firstName = tvFirstName.getText().toString().trim();
        String lastName = tvLastName.getText().toString().trim();
        String phone = tvPhoneNumber.getText().toString().trim();
        String address = tvAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if(!validate(firstName, lastName, phone, address, email, password, confirmPassword)){
            return;
        }

        btnSignUp.setEnabled(false);

        Map<String, RequestBody> partMap = new HashMap<>();
        partMap.put("firstName", createPartFromString(firstName));
        partMap.put("lastName", createPartFromString(lastName));
        partMap.put("phoneNumber", createPartFromString(phone));
        partMap.put("address", createPartFromString(address));
        partMap.put("email", createPartFromString(email));
        partMap.put("password", createPartFromString(password));
        partMap.put("confirmPassword", createPartFromString(confirmPassword));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                byte[] bytes = readBytesFromUri(selectedImageUri);
                String fileName = getFileNameFromUri(selectedImageUri);
                RequestBody req = RequestBody.create(MediaType.parse("image/*"), bytes);
                imagePart = MultipartBody.Part.createFormData("profileImage", fileName, req);
            } catch (IOException e) {
                Log.e("Registration", "Failed to read selected image", e);
                Toast.makeText(requireContext(), "Failed to read selected image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                btnSignUp.setEnabled(true);
                return;
            }
        }

        authService.register(partMap, imagePart).enqueue(new retrofit2.Callback<RegisterResponseDTO>() {
            @Override
            public void onResponse(retrofit2.Call<RegisterResponseDTO> call, retrofit2.Response<RegisterResponseDTO> response) {
                btnSignUp.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Registration successful! Please check your email to activate your account.", Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("email", email);
                    NavHostFragment.findNavController(RegistrationFragment.this).navigate(R.id.registration_to_activate_account, bundle);
                } else {
                    String errorMessage = "Error during registration";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("\"message\"")) {
                                int start = errorBody.indexOf("\"message\":\"") + 11;
                                int end = errorBody.indexOf("\"", start);
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
                    } catch (Exception e) {
                        Log.e("Registration", "Error parsing error response", e);
                        errorMessage = "Error during registration (code: " + response.code() + ")";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("Registration", "Registration failed: code=" + response.code() + ", message=" + errorMessage);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RegisterResponseDTO> call, Throwable t) {
                btnSignUp.setEnabled(true);
                Log.e("Registration", "Error during registration", t);

                String errorMessage = "Network error: " + t.getMessage();
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMessage = "Cannot connect to server. Please check your internet connection.";
                } else if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    errorMessage = "Connection timeout. Please try again.";
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validate(String firstName, String lastName, String phone, String address, String email, String password, String confirmPassword) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        String firstNamePattern = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$";
        if (!firstName.matches(firstNamePattern)) {
            tvFirstName.setError("First name must start with a capital letter and contain only letters");
            tvFirstName.requestFocus();
            return false;
        }

        if (firstName.length() < 2 || firstName.length() > 30) {
            tvFirstName.setError("First name must be between 2 and 30 characters");
            tvFirstName.requestFocus();
            return false;
        }

        String lastNamePattern = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$";
        if (!lastName.matches(lastNamePattern)) {
            tvLastName.setError("Last name must start with a capital letter and contain only letters");
            tvLastName.requestFocus();
            return false;
        }

        if (lastName.length() < 2 || lastName.length() > 30) {
            tvLastName.setError("Last name must be between 2 and 30 characters");
            tvLastName.requestFocus();
            return false;
        }

        String phonePattern = "^(\\+381|0)[0-9]{9,10}$";
        if (!phone.matches(phonePattern)) {
            tvPhoneNumber.setError("Invalid phone number format. Use +381XXXXXXXXX or 0XXXXXXXXX");
            tvPhoneNumber.requestFocus();
            return false;
        }

        String addressPattern = "^[A-Za-zČĆŠĐŽčćšđž0-9\\s.,/\\-]{5,}$";
        if (!address.matches(addressPattern)) {
            tvAddress.setError("Address must be at least 5 characters and contain only valid characters");
            tvAddress.requestFocus();
            return false;
        }

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!email.matches(emailPattern)) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords must match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        ContentResolver cr = requireContext().getContentResolver();
        try (InputStream is = cr.openInputStream(uri);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (is == null) throw new IOException("Unable to open input stream for uri");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
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
}