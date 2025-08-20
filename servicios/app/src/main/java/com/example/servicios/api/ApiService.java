package com.example.servicios.api;

import com.example.servicios.models.*;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // Auth
    @POST("auth/login")
    Call<User> login(@Body Map<String, String> body);

    @POST("auth/register")
    Call<User> register(@Body Map<String, String> body);

    // Público
    @GET("public/professionals")
    Call<List<ProfessionalCard>> listProfessionals(
            @Query("city") String city,
            @Query("category") String category,
            @Query("q") String q,
            @Query("limit") Integer limit,
            @Query("offset") Integer offset
    );

    @GET("public/professionals/{id}")
    Call<ProfessionalDetail> professionalDetail(@Path("id") String professionalId);

    // Profesional (perfil)
    @POST("professionals/me/notes")
    Call<ProfessionalDetail> saveNotes(@Body Map<String, Object> body);

    @POST("professionals/me/photos")
    Call<Photo> addPhoto(@Body Map<String, String> body);

    @DELETE("professionals/me/photos")
    Call<SimpleResponse> deletePhoto(@Body Map<String, String> body); // requiere OkHttp 4. (Soporta body en DELETE)

    // Conversaciones
    @POST("conversations/open")
    Call<OpenConversationResponse> openConversation(@Body Map<String, String> body);

    @POST("conversations/send")
    Call<Message> sendMessage(@Body Map<String, String> body);

    @GET("conversations/{id}/messages")
    Call<List<Message>> listMessages(@Path("id") String conversationId, @Query("limit") Integer limit);
}
