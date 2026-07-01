package com.example.ridenow.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.admin.PriceConfigRequestDTO;
import com.example.ridenow.dto.admin.PriceConfigResponseDTO;
import com.example.ridenow.dto.enums.VehicleType;
import com.example.ridenow.dto.model.PriceConfigDTO;
import com.example.ridenow.service.AdminService;
import com.example.ridenow.util.ClientUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceConfigFragment extends Fragment {

    private AdminService adminService;

    // UI Components
    private LinearLayout loadingLayout;
    private LinearLayout contentLayout;
    private LinearLayout priceConfigCardsContainer;
    private TextView tvError;
    private TextView tvSuccess;

    // Buttons
    private Button btnSaveChanges;

    // Data storage
    private final Map<VehicleType, PriceConfigDTO> originalConfigs = new HashMap<>();
    private final Map<VehicleType, PriceConfigDTO> currentConfigs = new HashMap<>();
    private final Map<VehicleType, PriceConfigCardHolder> cardHolders = new HashMap<>();

    // State tracking
    private boolean isLoading = false;
    private boolean isSaving = false;

    public PriceConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_price_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeServices();
        initializeViews(view);
        setupButtons();
        loadPriceConfigurations();
    }

    private void initializeServices() {
        adminService = ClientUtils.getClient(AdminService.class);
    }

    private void initializeViews(View view) {
        loadingLayout = view.findViewById(R.id.loadingLayout);
        contentLayout = view.findViewById(R.id.contentLayout);
        priceConfigCardsContainer = view.findViewById(R.id.priceConfigCardsContainer);
        tvError = view.findViewById(R.id.tvError);
        tvSuccess = view.findViewById(R.id.tvSuccess);

        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
    }

    private void setupButtons() {
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadPriceConfigurations() {
        if (isLoading) return;

        setLoadingState(true);
        hideMessages();

        adminService.getPriceConfig().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PriceConfigResponseDTO> call, @NonNull Response<PriceConfigResponseDTO> response) {
                setLoadingState(false);

                // Check if response is successful and has body
                if (response.isSuccessful() && response.body() != null) {
                    populateConfigData(response.body());
                    createDynamicCards();
                    showContent();
                } else {
                    showError(getString(R.string.price_config_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PriceConfigResponseDTO> call, @NonNull Throwable t) {
                setLoadingState(false);
                showError(getString(R.string.price_config_error));
            }
        });
    }

    private void populateConfigData(PriceConfigResponseDTO response) {
        originalConfigs.clear();
        currentConfigs.clear();

        // Populate both original and current configs from response
        if (response.getPrices() != null) {
            for (PriceConfigDTO config : response.getPrices()) {
                originalConfigs.put(config.getVehicleType(), copyConfig(config));
                currentConfigs.put(config.getVehicleType(), copyConfig(config));
            }
        }

        updateButtonStates();
    }

    private void createDynamicCards() {
        priceConfigCardsContainer.removeAllViews();
        cardHolders.clear();

        LayoutInflater inflater = getLayoutInflater();

        for (Map.Entry<VehicleType, PriceConfigDTO> entry : currentConfigs.entrySet()) {
            VehicleType vehicleType = entry.getKey();
            PriceConfigDTO config = entry.getValue();

            // Inflate the card layout
            View cardView = inflater.inflate(R.layout.item_price_config_card, priceConfigCardsContainer, false);

            // Create card holder
            PriceConfigCardHolder cardHolder = new PriceConfigCardHolder(cardView, vehicleType);
            cardHolders.put(vehicleType, cardHolder);

            // Set vehicle type name
            cardHolder.tvVehicleTypeName.setText(getVehicleTypeDisplayName(vehicleType));

            // Set initial values
            cardHolder.etBasePrice.setText(formatPrice(config.getBasePrice()));
            cardHolder.etPricePerKm.setText(formatPrice(config.getPricePerKm()));

            // Setup text watchers
            setupTextWatcher(cardHolder.etBasePrice);
            setupTextWatcher(cardHolder.etPricePerKm);

            // Add card to container
            priceConfigCardsContainer.addView(cardView);
        }
    }

    private void setupTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateCurrentConfigsFromCards();
                updateButtonStates();
                hideMessages();
            }
        });
    }

    private void updateCurrentConfigsFromCards() {
        // Update currentConfigs based on the values in the EditTexts
        for (Map.Entry<VehicleType, PriceConfigCardHolder> entry : cardHolders.entrySet()) {
            VehicleType vehicleType = entry.getKey();
            PriceConfigCardHolder cardHolder = entry.getValue();
            PriceConfigDTO config = currentConfigs.get(vehicleType);

            if (config != null) {
                config.setBasePrice(parsePrice(cardHolder.etBasePrice.getText()));
                config.setPricePerKm(parsePrice(cardHolder.etPricePerKm.getText()));
            }
        }
    }

    private void updateCardsFromConfigs() {
        for (Map.Entry<VehicleType, PriceConfigCardHolder> entry : cardHolders.entrySet()) {
            VehicleType vehicleType = entry.getKey();
            PriceConfigCardHolder cardHolder = entry.getValue();
            PriceConfigDTO config = currentConfigs.get(vehicleType);

            if (config != null) {
                cardHolder.etBasePrice.setText(formatPrice(config.getBasePrice()));
                cardHolder.etPricePerKm.setText(formatPrice(config.getPricePerKm()));
            }
        }
    }

    private void saveChanges() {
        if (isSaving) return;

        if (!validateInputs()) {
            showError(getString(R.string.price_config_validation_error));
            return;
        }

        if (!hasChanges()) {
            showError(getString(R.string.price_config_no_changes));
            return;
        }

        setSavingState(true);
        hideMessages();

        PriceConfigRequestDTO request = new PriceConfigRequestDTO();
        List<PriceConfigDTO> priceList = new ArrayList<>(currentConfigs.values());
        request.setPrices(priceList);

        adminService.updatePriceConfig(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                setSavingState(false);

                if (response.isSuccessful()) {
                    // Update original configs to match current configs
                    for (Map.Entry<VehicleType, PriceConfigDTO> entry : currentConfigs.entrySet()) {
                        originalConfigs.put(entry.getKey(), copyConfig(entry.getValue()));
                    }
                    updateButtonStates();
                    showSuccess(getString(R.string.price_config_success));
                } else {
                    showError(getString(R.string.price_config_save_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setSavingState(false);
                showError(getString(R.string.price_config_save_error));
            }
        });
    }

    private boolean validateInputs() {
        for (PriceConfigDTO config : currentConfigs.values()) {
            if (config.getBasePrice() < 0 || config.getBasePrice() > 10000 ||
                    config.getPricePerKm() < 0 || config.getPricePerKm() > 10000) {
                return false;
            }
        }
        return true;
    }

    private boolean hasChanges() {
        for (Map.Entry<VehicleType, PriceConfigDTO> entry : currentConfigs.entrySet()) {
            PriceConfigDTO current = entry.getValue();
            PriceConfigDTO original = originalConfigs.get(entry.getKey());

            if (original == null ||
                    Math.abs(current.getBasePrice() - original.getBasePrice()) > 0.01 ||
                    Math.abs(current.getPricePerKm() - original.getPricePerKm()) > 0.01) {
                return true;
            }
        }
        return false;
    }

    private void updateButtonStates() {
        boolean hasChanges = hasChanges();
        btnSaveChanges.setEnabled(hasChanges && !isSaving);

        if (isSaving) {
            btnSaveChanges.setText(getString(R.string.price_config_saving));
        } else {
            btnSaveChanges.setText(getString(R.string.price_config_save_changes));
        }
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        loadingLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (!loading) {
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setSavingState(boolean saving) {
        isSaving = saving;
        updateButtonStates();
    }

    private void showContent() {
        contentLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
    }

    private void showError(String message) {
        hideMessages();
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String message) {
        hideMessages();
        tvSuccess.setText(message);
        tvSuccess.setVisibility(View.VISIBLE);
    }

    private void hideMessages() {
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
    }

    private PriceConfigDTO copyConfig(PriceConfigDTO original) {
        return new PriceConfigDTO(original.getVehicleType(), original.getBasePrice(), original.getPricePerKm());
    }

    private String formatPrice(double price) {
        if (price == (long) price) {
            return String.valueOf((long) price);
        } else {
            return String.valueOf(price);
        }
    }

    private double parsePrice(Editable editable) {
        if (editable == null) return 0.0;
        String text = editable.toString().trim();
        if (TextUtils.isEmpty(text)) return 0.0;

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getVehicleTypeDisplayName(VehicleType vehicleType) {
        switch (vehicleType) {
            case STANDARD:
                return getString(R.string.vehicle_type_standard);
            case LUXURY:
                return getString(R.string.vehicle_type_luxury);
            case VAN:
                return getString(R.string.vehicle_type_van);
            default:
                return vehicleType.name();
        }
    }

    // Helper class to hold card views
    private static class PriceConfigCardHolder {
        final TextView tvVehicleTypeName;
        final EditText etBasePrice;
        final EditText etPricePerKm;
        final VehicleType vehicleType;

        public PriceConfigCardHolder(View cardView, VehicleType vehicleType) {
            this.vehicleType = vehicleType;
            this.tvVehicleTypeName = cardView.findViewById(R.id.tvVehicleTypeName);
            this.etBasePrice = cardView.findViewById(R.id.etBasePrice);
            this.etPricePerKm = cardView.findViewById(R.id.etPricePerKm);
        }
    }
}
