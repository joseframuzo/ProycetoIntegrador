package com.pintegrador.servicios.network.dto;

import java.util.List;

public class ProfessionalProfile {
    public String id;
    public String full_name;
    public String headline;
    public String about;
    public String city;
    public String phone;
    public String main_category; // "arquitecto", etc.
    public float rating_avg;
    public int rating_count;
    public List<ProfessionalPhoto> photos;
    public List<Review> reviews;
}
