package com.example.servicios.models;


import java.util.List;

public class ProfessionalDetail {
    public String id;
    public String user_id;
    public String cedula;
    public String main_category;
    public String headline;
    public String about;
    public String city;
    public boolean verified;
    public Integer experience_years;
    public String notes;
    public String created_at;

    // Embebidos
    public EmbeddedUser AppUser; // del include
    public List<Photo> ProfessionalPhotos;

    // Cuando viene de /public/professionals/:id usamos estas claves:
    public java.util.List<Photo> photos;

    public static class EmbeddedUser {
        public String full_name;
        public String email;
        public String phone;
    }
}
