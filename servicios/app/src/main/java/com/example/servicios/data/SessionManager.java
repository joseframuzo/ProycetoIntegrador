package com.example.servicios.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.servicios.models.User;

public class SessionManager {
    private static final String PREF = "app_session";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void save(User u) {
        sp.edit()
                .putString(KEY_ID, u.id)
                .putString(KEY_NAME, u.full_name)
                .putString(KEY_EMAIL, u.email)
                .putString(KEY_ROLE, u.role)
                .apply();
    }

    public boolean isLogged() { return sp.getString(KEY_ID, null) != null; }
    public String userId() { return sp.getString(KEY_ID, null); }
    public String role() { return sp.getString(KEY_ROLE, "USER"); }
    public void logout() { sp.edit().clear().apply(); }
}
