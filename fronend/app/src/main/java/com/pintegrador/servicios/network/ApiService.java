package com.pintegrador.servicios.network;

import com.pintegrador.servicios.network.dto.Category;
import com.pintegrador.servicios.network.dto.LoginResponse;
import com.pintegrador.servicios.network.dto.Professional;
import com.pintegrador.servicios.network.dto.ProfessionalProfile;
import com.pintegrador.servicios.network.dto.Review;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;   // <-- IMPORTANTE

public interface ApiService {

    // -------- Auth ----------
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body Map<String, Object> body);

    @POST("api/auth/register")
    Call<LoginResponse> register(@Body Map<String, Object> body);

    // -------- Catálogo ----------
    @GET("api/professionals/categories")
    Call<List<Category>> getCategories();

    // -------- Listado de profesionales por categoría (dos opciones) ----------
    // Opción A (por query): /api/professionals?category=arquitecto
    @GET("api/professionals")
    Call<List<Professional>> getProfessionals(@Query("category") String slug);

    // Opción B (por path): /api/professionals/by-category/arquitecto
    @GET("api/professionals/by-category/{slug}")
    Call<List<Professional>> getByCategory(@Path("slug") String slug);

    // -------- Perfil Profesional ----------
    @GET("api/professionals/me")
    Call<ProfessionalProfile> getMyProfile();

    @GET("api/professionals/{id}")
    Call<ProfessionalProfile> getProfessional(@Path("id") String id);

    // -------- Reviews ----------
    @POST("api/professionals/{id}/reviews")
    Call<Void> postReview(@Path("id") String id, @Body Review body);
}
