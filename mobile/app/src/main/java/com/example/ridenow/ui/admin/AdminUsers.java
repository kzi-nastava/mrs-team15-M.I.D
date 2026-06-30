package com.example.ridenow.ui.admin;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridenow.R;
import com.example.ridenow.dto.user.UserItemDTO;
import com.example.ridenow.dto.util.PageResponse;
import com.example.ridenow.service.AdminService;
import com.example.ridenow.util.ClientUtils;
import android.widget.Filter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsers extends Fragment {

    private AutoCompleteTextView spinnerSortBy;
    private AutoCompleteTextView spinnerOrder;
    private Button btnApply;
    private Button btnClear;
    private LinearLayout usersContainer;
    private AdminService adminService;

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private String currentSortBy = "email";
    private String currentSortDir = "desc";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        spinnerSortBy = view.findViewById(R.id.spinnerSortBy);
        spinnerOrder = view.findViewById(R.id.spinnerOrder);
        btnApply = view.findViewById(R.id.btnApply);
        btnClear = view.findViewById(R.id.btnClear);
        usersContainer = view.findViewById(R.id.usersContainer);

        spinnerSortBy.setDropDownBackgroundResource(android.R.color.white);
        spinnerOrder.setDropDownBackgroundResource(android.R.color.white);

        adminService = ClientUtils.getClient(AdminService.class);

        setupDropdowns();
        setupButtons();
        loadUsers();

        return view;
    }

    private void setupDropdowns() {
        String[] sortOptions = {"Name", "Surname", "Role", "Email"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(requireContext(), R.layout.dropdown_item, sortOptions) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = sortOptions;
                        results.count = sortOptions.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        spinnerSortBy.setAdapter(sortAdapter);
        spinnerSortBy.setTextColor(Color.BLACK);
        spinnerSortBy.setBackgroundColor(Color.WHITE);
        spinnerSortBy.setText("Name", false);

        String[] orderOptions = {"Asc", "Desc"};
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<String>(requireContext(), R.layout.dropdown_item, orderOptions) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = orderOptions;
                        results.count = orderOptions.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        spinnerOrder.setAdapter(orderAdapter);
        spinnerOrder.setTextColor(Color.BLACK);
        spinnerOrder.setBackgroundColor(Color.WHITE);
        spinnerOrder.setText("Desc", false);

        spinnerSortBy.setOnItemClickListener((parent, v, position, id) -> {
            String[] apiFields = {"firstName", "surname", "role", "email"};
            currentSortBy = apiFields[position];
        });

        spinnerOrder.setOnItemClickListener((parent, v, position, id) ->
                currentSortDir = position == 0 ? "asc" : "desc"
        );
    }
    private void setupButtons() {
        btnApply.setOnClickListener(v -> {
            currentPage = 0;
            loadUsers();
        });

        btnClear.setOnClickListener(v -> {
            spinnerSortBy.setText("Email", false);
            spinnerOrder.setText("Desc", false);
            currentSortBy = "email";
            currentSortDir = "desc";
            currentPage = 0;
            loadUsers();
        });
    }

    private void loadUsers() {
        if (isLoading) {
            return;
        }

        isLoading = true;

        Call<PageResponse<UserItemDTO>> call = adminService.getAllUsers(currentPage, 10, currentSortBy, currentSortDir);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<UserItemDTO>> call, @NonNull Response<PageResponse<UserItemDTO>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<UserItemDTO> data = response.body();

                    if (currentPage == 0) { usersContainer.removeAllViews(); }

                    else { removeLoadMoreButton();}

                    hasMoreData = !data.isLast();

                    List<UserItemDTO> users = data.getContent();

                    for (UserItemDTO user : users) { createUserCard(user);}

                    if (hasMoreData) { addLoadMoreButton();}

                } else {
                    String errorMsg = "Failed to load users";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    android.widget.Toast.makeText(getContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<UserItemDTO>> call, @NonNull Throwable t) {
                isLoading = false;
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserCard(UserItemDTO user) {
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_user_card, usersContainer, false);

        TextView tvFullName = cardView.findViewById(R.id.tvFullName);
        TextView tvEmail    = cardView.findViewById(R.id.tvEmail);
        TextView tvRole     = cardView.findViewById(R.id.tvRole);

        tvFullName.setText(user.getName() + " " + user.getSurname());
        tvEmail.setText(user.getEmail());
        tvRole.setText(user.getRole());

        cardView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("userId", user.getId());
            bundle.putString("userName", user.getName() + " " + user.getSurname());
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.action_adminUsers_to_adminUserHistory, bundle);
        });
        cardView.setClickable(true);
        cardView.setFocusable(true);
        usersContainer.addView(cardView);
    }

    private void addLoadMoreButton() {
        Button btn = new Button(getContext());
        btn.setText("Load More");
        btn.setBackgroundColor(Color.BLACK);
        btn.setTextColor(Color.WHITE);
        spinnerSortBy.setTextColor(Color.BLACK);
        btn.setTag("load_more_button");
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 16, 0, 16);
        btn.setLayoutParams(p);
        btn.setOnClickListener(v -> {
            removeLoadMoreButton();
            currentPage++;
            loadUsers();
        });
        usersContainer.addView(btn);
    }

    private void removeLoadMoreButton() {
        for (int i = usersContainer.getChildCount() - 1; i >= 0; i--) {
            View child = usersContainer.getChildAt(i);
            if (child instanceof Button && "load_more_button".equals(child.getTag())) {
                usersContainer.removeViewAt(i);
                break;
            }
        }
    }
}