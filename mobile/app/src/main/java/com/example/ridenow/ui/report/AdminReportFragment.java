package com.example.ridenow.ui.report;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.admin.AdminUserResponseDTO;
import com.example.ridenow.dto.admin.PagedResponseDTO;
import com.example.ridenow.dto.report.ReportResponseDTO;
import com.example.ridenow.service.AdminService;
import com.example.ridenow.util.ClientUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReportFragment extends Fragment {

    private static final String TAG = "AdminReportFragment";
    private static final SimpleDateFormat DISPLAY_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final long SEARCH_DEBOUNCE_MS = 300;

    private static final String[] SCOPES = {"ALL_DRIVERS", "ALL_USERS", "SINGLE_USER"};
    private static final String[] SCOPE_LABELS = {"All drivers", "All users", "Specific user"};

    private EditText etStartDate, etEndDate;
    private Spinner spinnerScope;
    private View userSearchContainer;
    private AutoCompleteTextView etUserSearch;
    private TextView tvUserSearchLoading;
    private Button btnLoadReport;
    private TextView tvMessage;
    private ReportTabsView reportTabsView;

    private AdminService adminService;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    private Long startDateMillis = null;
    private Long endDateMillis = null;
    private String scope = "ALL_DRIVERS";
    private String selectedUserId = null;

    private List<AdminUserResponseDTO> lastSearchResults = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminService = ClientUtils.getClient(AdminService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        spinnerScope = view.findViewById(R.id.spinnerScope);
        userSearchContainer = view.findViewById(R.id.userSearchContainer);
        etUserSearch = view.findViewById(R.id.etUserSearch);
        tvUserSearchLoading = view.findViewById(R.id.tvUserSearchLoading);
        btnLoadReport = view.findViewById(R.id.btnLoadReport);
        tvMessage = view.findViewById(R.id.tvMessage);
        reportTabsView = view.findViewById(R.id.reportTabsView);

        reportTabsView.setRole(false, true);

        etStartDate.setOnClickListener(v -> pickDate(true));
        etEndDate.setOnClickListener(v -> pickDate(false));
        btnLoadReport.setOnClickListener(v -> loadReport());

        setupScopeSpinner();
        setupUserSearch();
    }

    private void setupScopeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, SCOPE_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScope.setAdapter(adapter);

        spinnerScope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                scope = SCOPES[position];
                boolean isSingleUser = "SINGLE_USER".equals(scope);
                userSearchContainer.setVisibility(isSingleUser ? View.VISIBLE : View.GONE);
                if (!isSingleUser) {
                    selectedUserId = null;
                    etUserSearch.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupUserSearch() {
        etUserSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedUserId = null;
                if (pendingSearch != null) debounceHandler.removeCallbacks(pendingSearch);
                String term = s.toString();
                if (term.length() < 2) return;
                pendingSearch = () -> searchUsers(term);
                debounceHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etUserSearch.setOnItemClickListener((parent, view, position, id) -> {
            if (position < lastSearchResults.size()) {
                AdminUserResponseDTO user = lastSearchResults.get(position);
                selectedUserId = String.valueOf(user.getId());
            }
        });
    }

    private void searchUsers(String term) {
        tvUserSearchLoading.setVisibility(View.VISIBLE);
        adminService.getAllUsers(term, null, null, 0, 10).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponseDTO<AdminUserResponseDTO>> call, @NonNull Response<PagedResponseDTO<AdminUserResponseDTO>> response) {
                tvUserSearchLoading.setVisibility(View.GONE);
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    lastSearchResults = response.body().getContent() != null ? response.body().getContent() : new ArrayList<>();
                    List<String> display = new ArrayList<>();
                    for (AdminUserResponseDTO u : lastSearchResults) {
                        String name = (u.getFirstName() == null ? "" : u.getFirstName()) + " " + (u.getLastName() == null ? "" : u.getLastName());
                        display.add(name.trim() + " (" + u.getEmail() + ")");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, display);
                    etUserSearch.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    etUserSearch.showDropDown();
                } else {
                    lastSearchResults = new ArrayList<>();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponseDTO<AdminUserResponseDTO>> call, @NonNull Throwable t) {
                tvUserSearchLoading.setVisibility(View.GONE);
                Log.e(TAG, "User search failed", t);
                lastSearchResults = new ArrayList<>();
            }
        });
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            long millis = selected.getTimeInMillis();
            if (isStart) {
                startDateMillis = millis;
                etStartDate.setText(DISPLAY_FMT.format(selected.getTime()));
            } else {
                endDateMillis = millis;
                etEndDate.setText(DISPLAY_FMT.format(selected.getTime()));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadReport() {
        hideMessage();
        setLoading(true);

        boolean drivers = "ALL_DRIVERS".equals(scope);
        boolean users = !"ALL_DRIVERS".equals(scope); // ALL_USERS or SINGLE_USER
        String personId = "SINGLE_USER".equals(scope) ? selectedUserId : null;

        Call<ReportResponseDTO> call = adminService.getReport(startDateMillis, endDateMillis, drivers, users, personId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReportResponseDTO> call, @NonNull Response<ReportResponseDTO> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    renderReport(response.body());
                } else {
                    Log.e(TAG, "Failed to load admin report: " + response.code());
                    showMessage("Failed to load report.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportResponseDTO> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Network error loading admin report", t);
                showMessage("Failed to request report.");
            }
        });
    }

    private void renderReport(ReportResponseDTO resp) {
        ReportDataProcessor.Result result = ReportDataProcessor.process(resp, startDateMillis, endDateMillis);

        if (!result.hasRange) {
            reportTabsView.clearCharts();
            reportTabsView.setSummaries(0d, 0d, 0d, 0d, 0d, 0d);
            showMessage("No report data for the selected range.");
            return;
        }

        if (result.allZero) {
            reportTabsView.clearCharts();
            reportTabsView.setSummaries(null, null, null, null, null, null);
            showMessage("No data available for the chosen parameters.");
            return;
        }

        hideMessage();
        reportTabsView.setSummaries(result.ridesSum, result.ridesAvg, result.kmSum, result.kmAvg, result.moneySum, result.moneyAvg);
        reportTabsView.renderCharts(result.labels, result.rides, result.km, result.money);
    }

    private void setLoading(boolean isLoading) {
        btnLoadReport.setEnabled(!isLoading);
        btnLoadReport.setText(isLoading ? "Loading..." : "Load report");
    }

    private void showMessage(String msg) {
        tvMessage.setText(msg);
        tvMessage.setVisibility(View.VISIBLE);
    }

    private void hideMessage() {
        tvMessage.setVisibility(View.GONE);
    }
}
