package com.example.ridenow.ui.driver.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridenow.R;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestsFragment extends Fragment {

    public DriverRequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_requests, container, false);
        setupRecycler(view);
        return view;
    }

    private void setupRecycler(View view) {
        RecyclerView recycler = view.findViewById(R.id.requestsRecycler);
        Spinner statusFilter = view.findViewById(R.id.statusFilter);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        DriverRequestsAdapter adapter = new DriverRequestsAdapter(new ArrayList<>(), item -> {
            Bundle args = new Bundle();
            args.putInt("requestId", item.id);
            args.putString("avatarUrlCur", item.curAvatarUrl);
            args.putString("avatarUrlProp", item.avatarUrl);
            args.putString("firstName", item.firstName);
            args.putString("lastName", item.lastName);
            args.putString("email", item.email);
            args.putString("phone", item.phoneNumber);
            args.putString("address", item.address);
            args.putString("license", item.licensePlate);
            args.putString("model", item.vehicleModel);
            args.putString("vehicleType", item.vehicleType);
            args.putInt("seats", item.numberOfSeats);
            args.putBoolean("pet", item.petFriendly);
            args.putBoolean("baby", item.babyFriendly);
            args.putString("status", item.status);
            args.putLong("driverId", item.driverId);
            try {
                NavHostFragment.findNavController(this).navigate(R.id.action_driverRequests_to_requestDetail, args);
            } catch (Exception e) {
                // swallow navigation errors silently
            }
        });

        recycler.setAdapter(adapter);

        // fetch from backend
        com.example.ridenow.service.AdminService adminService = com.example.ridenow.util.ClientUtils.getClient(com.example.ridenow.service.AdminService.class);
        adminService.getDriverRequests().enqueue(new retrofit2.Callback<java.util.List<com.example.ridenow.dto.admin.DriverChangeRequestDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.ridenow.dto.admin.DriverChangeRequestDTO>> call, retrofit2.Response<java.util.List<com.example.ridenow.dto.admin.DriverChangeRequestDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RequestItem> items = new ArrayList<>();
                    for (com.example.ridenow.dto.admin.DriverChangeRequestDTO d : response.body()) {
                        int id = (int) d.getRequestId();
                        String name = (d.getFirstName() == null ? "" : d.getFirstName()) + " " + (d.getLastName() == null ? "" : d.getLastName());
                        RequestItem it = new RequestItem(id, name, d.getSubmitDate(), d.getStatus() == null ? "pending" : d.getStatus().toLowerCase(), d.getProfileImage());
                        it.firstName = d.getFirstName();
                        it.lastName = d.getLastName();
                        it.email = d.getEmail();
                        it.phoneNumber = d.getPhoneNumber();
                        it.address = d.getAddress();
                        it.licensePlate = d.getLicensePlate();
                        it.vehicleModel = d.getVehicleModel();
                        it.vehicleType = d.getVehicleType();
                        it.numberOfSeats = d.getNumberOfSeats();
                        it.petFriendly = d.isPetFriendly();
                        it.babyFriendly = d.isBabyFriendly();
                        it.driverId = d.getDriverId() != null ? d.getDriverId() : -1L;
                        items.add(it);
                    }
                    adapter.setItems(items);

                    // enrich with current user info for each item (fetch user by driverId)
                    for (RequestItem r : items) {
                        if (r.driverId != -1L) {
                            final RequestItem copy = r;
                            adminService.getUserById(r.driverId).enqueue(new retrofit2.Callback<com.example.ridenow.dto.user.UserResponseDTO>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.example.ridenow.dto.user.UserResponseDTO> call, retrofit2.Response<com.example.ridenow.dto.user.UserResponseDTO> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        com.example.ridenow.dto.user.UserResponseDTO cur = response.body();
                                        String curName = (cur.getFirstName() == null ? "" : cur.getFirstName()) + " " + (cur.getLastName() == null ? "" : cur.getLastName());
                                        copy.curName = curName.trim().isEmpty() ? null : curName.trim();
                                        copy.curEmail = cur.getEmail();
                                        copy.curAvatarUrl = cur.getProfileImage();
                                        // refresh adapter data
                                        adapter.setItems(items);
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<com.example.ridenow.dto.user.UserResponseDTO> call, Throwable t) {
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.ridenow.dto.admin.DriverChangeRequestDTO>> call, Throwable t) {
                // keep sample empty; optionally show error
            }
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"All", "pending", "approved", "rejected"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilter.setAdapter(spinnerAdapter);
        statusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                String val = (String) parent.getItemAtPosition(position);
                if ("All".equalsIgnoreCase(val)) adapter.setFilter(null);
                else adapter.setFilter(val.toLowerCase());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    static class RequestItem {
        int id;
        String name;
        String submittedAt;
        String status;
        String avatarUrl;
        String curName;
        String curEmail;
        String curAvatarUrl;
        String firstName;
        String lastName;
        String email;
        String phoneNumber;
        String address;
        String licensePlate;
        String vehicleModel;
        String vehicleType;
        int numberOfSeats;
        boolean petFriendly;
        boolean babyFriendly;
        long driverId; // Added field for driverId

        RequestItem(int id, String name, String submittedAt, String status, String avatarUrl) {
            this.id = id;
            this.name = name;
            this.submittedAt = submittedAt;
            this.status = status;
            this.avatarUrl = avatarUrl;
        }
    }
}
