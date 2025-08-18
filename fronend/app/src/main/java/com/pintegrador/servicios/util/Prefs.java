package com.pintegrador.servicios.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Prefs {
    private static final String FILE = "servicios_prefs";
    private static final String K_TOKEN = "token";
    private static final String K_ROLE  = "role";
    private static final String K_NAME  = "name";
    // NUEVO: Base URL opcional del servidor (para celular real sin adb reverse)
    private static final String K_BASE_URL = "server_base";

    private static SharedPreferences sp(Context c){
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static void saveAuth(Context c, String token, String role, String name){
        sp(c).edit()
                .putString(K_TOKEN, token)
                .putString(K_ROLE, role)
                .putString(K_NAME, name)
                .apply();
    }

    public static String getToken(Context c){ return sp(c).getString(K_TOKEN, null); }
    public static String getRole(Context c){ return sp(c).getString(K_ROLE, null); }
    public static String getName(Context c){ return sp(c).getString(K_NAME, ""); }

    public static void clear(Context c){
        sp(c).edit().clear().apply();
    }

    // ===== Helpers NUEVOS para configurar la URL del servidor =====

    /**
     * Guarda una URL base personalizada del backend (por ejemplo "http://192.168.1.50:4000/").
     * Si pasas null o vacío, se elimina y ApiClient volverá a su lógica por defecto
     * (10.0.2.2 en emulador, 127.0.0.1 con adb reverse).
     */
    public static void setServerBaseUrl(Context c, String url){
        if (url != null) url = url.trim();
        if (TextUtils.isEmpty(url)) {
            sp(c).edit().remove(K_BASE_URL).apply();
        } else {
            sp(c).edit().putString(K_BASE_URL, url).apply();
        }
    }

    /** Devuelve la URL base personalizada si existe, si no, null. */
    public static String getServerBaseUrl(Context c){
        return sp(c).getString(K_BASE_URL, null);
    }
}
