package com.example.ridenow.service;

import retrofit2.Call;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PanicAlertService {
    @PUT("/api/panic-alerts/{id}/resolve")
    Call<Void> resolvePanicAlert(@Path("id") Long id);
}