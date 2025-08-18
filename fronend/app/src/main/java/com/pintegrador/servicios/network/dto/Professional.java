package com.pintegrador.servicios.network.dto;

public class Professional {
    public String professional_id;
    public String full_name;
    public String main_category;
    public String headline;
    public String city;
    public boolean verified;   // si la vista lo trae; si no, GSON lo ignora
    public String photo_url;   // si no existe en la respuesta, GSON lo ignora
}
