package com.example.ridenow.ui.admin;

import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.admin.AdminUserResponseDTO;
import com.example.ridenow.dto.admin.BlockUserRequestDTO;
import com.example.ridenow.dto.admin.PagedResponseDTO;
import com.example.ridenow.service.AdminService;
import com.example.ridenow.util.ClientUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersFragment extends Fragment {

    private static final String TAG = "UsersFragment";
    private static final String BACKEND_URL = "https://your-backend-url.example.com"; // TODO: wire to environment config

    private ProgressBar progressBar;
    private TextView tvNoUsers;
    private TextView tvPageInfo;
    private LinearLayout usersContainer;
    private TextInputEditText etSearch;
    private Spinner spinnerPageSize;
    private Button btnPrevPage;
    private Button btnNextPage;

    private TextView sortRole, sortEmail, sortFirstName, sortLastName, sortBlocked;

    private AdminService adminService;

    private String searchQuery = "";
    private int pageSize = 10;
    private int currentPage = 0; // 0-based, mirrors web pageIndex
    private long totalUsers = 0;
    private int totalPages = 1;
    private String sortBy = null;
    private String sortDir = "asc";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminService = ClientUtils.getClient(AdminService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupPageSizeSpinner();
        setupSortHandlers();
        setupSearch();
        setupPagination();
        fetchUsers();
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        tvNoUsers = view.findViewById(R.id.tvNoUsers);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);
        usersContainer = view.findViewById(R.id.usersContainer);
        etSearch = view.findViewById(R.id.etSearch);
        spinnerPageSize = view.findViewById(R.id.spinnerPageSize);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);

        sortRole = view.findViewById(R.id.sortRole);
        sortEmail = view.findViewById(R.id.sortEmail);
        sortFirstName = view.findViewById(R.id.sortFirstName);
        sortLastName = view.findViewById(R.id.sortLastName);
        sortBlocked = view.findViewById(R.id.sortBlocked);
    }

    private void setupPageSizeSpinner() {
        Integer[] sizes = {5, 10, 25, 50};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sizes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPageSize.setAdapter(adapter);
        spinnerPageSize.setSelection(1); // default 10

        spinnerPageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                int selected = sizes[position];
                if (selected != pageSize) {
                    pageSize = selected;
                    currentPage = 0;
                    fetchUsers();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSortHandlers() {
        sortRole.setOnClickListener(v -> setSort("role"));
        sortEmail.setOnClickListener(v -> setSort("email"));
        sortFirstName.setOnClickListener(v -> setSort("firstName"));
        sortLastName.setOnClickListener(v -> setSort("lastName"));
        sortBlocked.setOnClickListener(v -> setSort("blocked"));
    }

    private void setSort(String column) {
        if (column.equals(sortBy)) {
            sortDir = "asc".equals(sortDir) ? "desc" : "asc";
        } else {
            sortBy = column;
            sortDir = "asc";
        }
        updateSortLabels();
        fetchUsers();
    }

    private void updateSortLabels() {
        sortRole.setText("Role " + sortArrow("role"));
        sortEmail.setText("Email " + sortArrow("email"));
        sortFirstName.setText("Name " + sortArrow("firstName"));
        sortLastName.setText("Surname " + sortArrow("lastName"));
        sortBlocked.setText("Blocked " + sortArrow("blocked"));
    }

    private String sortArrow(String column) {
        if (!column.equals(sortBy)) return "⇅";
        return "asc".equals(sortDir) ? "↑" : "↓";
    }

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            searchQuery = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
            currentPage = 0;
            fetchUsers();
            return true;
        });
    }

    private void setupPagination() {
        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                fetchUsers();
            }
        });
        btnNextPage.setOnClickListener(v -> {
            if (currentPage + 1 < totalPages) {
                currentPage++;
                fetchUsers();
            }
        });
    }

    private void fetchUsers() {
        showLoading(true);

        Call<PagedResponseDTO<AdminUserResponseDTO>> call = adminService.getAllUsers(
                searchQuery.isEmpty() ? null : searchQuery,
                sortBy,
                sortDir,
                currentPage,
                pageSize
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponseDTO<AdminUserResponseDTO>> call, @NonNull Response<PagedResponseDTO<AdminUserResponseDTO>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    PagedResponseDTO<AdminUserResponseDTO> page = response.body();
                    List<AdminUserResponseDTO> users = page.getContent();
                    totalUsers = page.getTotalElements();
                    totalPages = Math.max(1, page.getTotalPages());
                    updatePageInfo();
                    displayUsers(users);
                } else {
                    Log.e(TAG, "Failed to load users: " + response.code());
                    showError("Failed to load users");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponseDTO<AdminUserResponseDTO>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error loading users", t);
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void updatePageInfo() {
        int displayPage = currentPage + 1;
        tvPageInfo.setText("page " + displayPage + " of " + totalPages + " (" + totalUsers + " total users)");
        btnPrevPage.setEnabled(currentPage > 0);
        btnNextPage.setEnabled(currentPage + 1 < totalPages);
    }

    private void displayUsers(List<AdminUserResponseDTO> users) {
        usersContainer.removeAllViews();

        if (users == null || users.isEmpty()) {
            showNoUsers(true);
            return;
        }

        showNoUsers(false);

        for (AdminUserResponseDTO user : users) {
            View card = createUserCard(user);
            usersContainer.addView(card);
        }
    }

    private View createUserCard(AdminUserResponseDTO user) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View cardView = inflater.inflate(R.layout.item_admin_user, usersContainer, false);

        ImageView ivAvatar = cardView.findViewById(R.id.ivAvatar);
        TextView tvInitials = cardView.findViewById(R.id.tvAvatarInitials);
        TextView tvName = cardView.findViewById(R.id.tvName);
        TextView tvEmail = cardView.findViewById(R.id.tvEmail);
        TextView tvRole = cardView.findViewById(R.id.tvRole);
        TextView tvPhone = cardView.findViewById(R.id.tvPhone);
        Button btnBlockToggle = cardView.findViewById(R.id.btnBlockToggle);

        String firstName = user.getFirstName() == null ? "" : user.getFirstName();
        String lastName = user.getLastName() == null ? "" : user.getLastName();

        tvName.setText((firstName + " " + lastName).trim());
        tvEmail.setText(user.getEmail());
        tvRole.setText(user.getRole());
        tvPhone.setText(user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()
                ? user.getPhoneNumber() : "-");

        String initials = (firstName.isEmpty() ? "" : firstName.substring(0, 1))
                + (lastName.isEmpty() ? "" : lastName.substring(0, 1));
        tvInitials.setText(initials.toUpperCase());

        if (user.getProfileImage() != null && !user.getProfileImage().trim().isEmpty()) {
            loadAvatarImage(BACKEND_URL + user.getProfileImage(), ivAvatar, tvInitials);
        } else {
            ivAvatar.setVisibility(View.GONE);
            tvInitials.setVisibility(View.VISIBLE);
        }

        if (user.isBlocked()) {
            btnBlockToggle.setText("Unblock");
            btnBlockToggle.setOnClickListener(v -> openUnblockDialog(user));
        } else {
            btnBlockToggle.setText("Block");
            btnBlockToggle.setOnClickListener(v -> openBlockDialog(user));
        }

        return cardView;
    }

    // Simple background image load; swap for Glide/Picasso if already a project dependency.
    private void loadAvatarImage(String url, ImageView imageView, TextView fallbackInitials) {
        new AsyncTask<Void, Void, android.graphics.Bitmap>() {
            @Override
            protected android.graphics.Bitmap doInBackground(Void... voids) {
                try (InputStream in = new URL(url).openStream()) {
                    return BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to load avatar: " + url, e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(android.graphics.Bitmap bitmap) {
                if (!isAdded()) return;
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    fallbackInitials.setVisibility(View.GONE);
                } else {
                    imageView.setVisibility(View.GONE);
                    fallbackInitials.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void openBlockDialog(AdminUserResponseDTO user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_block_user, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvInfo = dialogView.findViewById(R.id.tvBlockUserInfo);
        TextInputEditText etReason = dialogView.findViewById(R.id.etBlockReason);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancelBlock);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirmBlock);

        tvInfo.setText("Are you sure you want to block " + user.getFirstName() + " " + user.getLastName()
                + " (" + user.getEmail() + ")?");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String reason = etReason.getText() == null ? "" : etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                etReason.setError("Reason is required");
                Toast.makeText(requireContext(), "Please provide a reason", Toast.LENGTH_LONG).show();
                return;
            }
            dialog.dismiss();
            performBlock(user.getId(), reason);
        });

        dialog.show();
    }

    private void openUnblockDialog(AdminUserResponseDTO user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_unblock_user, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvInfo = dialogView.findViewById(R.id.tvUnblockUserInfo);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancelUnblock);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirmUnblock);

        tvInfo.setText("Are you sure you want to unblock " + user.getFirstName() + " " + user.getLastName()
                + " (" + user.getEmail() + ")?");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performUnblock(user.getId());
        });

        dialog.show();
    }

    private void performBlock(Long id, String reason) {
        showLoading(true);
        BlockUserRequestDTO dto = new BlockUserRequestDTO(reason);
        adminService.blockUser(id, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "User blocked.", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                } else {
                    showRequestError(response, "Failed to block user.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performUnblock(Long id) {
        showLoading(true);
        adminService.unblockUser(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "User unblocked.", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                } else {
                    showRequestError(response, "Failed to unblock user.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRequestError(Response<Void> response, String fallbackMessage) {
        String errorMessage = fallbackMessage;
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                errorMessage = errorBody.string();
                if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                    errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                }
            }
        } catch (Exception e) {
            errorMessage = fallbackMessage + " (code: " + response.code() + ")";
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showNoUsers(boolean show) {
        tvNoUsers.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        showNoUsers(true);
    }
}