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

        List<RequestItem> items = new ArrayList<>();
        items.add(new RequestItem(101, "Ivan Horvat", "2026-02-01", "pending", "https://i.pravatar.cc/150?img=3"));
        items.add(new RequestItem(102, "Ana Nikolic", "2026-02-02", "approved", "https://i.pravatar.cc/150?img=5"));
        items.add(new RequestItem(103, "Marko Petrovic", "2026-02-03", "rejected", "https://i.pravatar.cc/150?img=7"));
        items.add(new RequestItem(104, "Jana Kostic", "2026-02-04", "pending", null));

        DriverRequestsAdapter adapter = new DriverRequestsAdapter(items, item -> {
            Bundle args = new Bundle();
            args.putInt("requestId", item.id);
            args.putString("avatarUrlCur", null);
            args.putString("avatarUrlProp", item.avatarUrl);
            try {
                NavHostFragment.findNavController(this).navigate(R.id.action_driverRequests_to_requestDetail, args);
            } catch (Exception e) {
                // swallow navigation errors silently
            }
        });

        recycler.setAdapter(adapter);

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

        RequestItem(int id, String name, String submittedAt, String status, String avatarUrl) {
            this.id = id;
            this.name = name;
            this.submittedAt = submittedAt;
            this.status = status;
            this.avatarUrl = avatarUrl;
        }
    }
}
