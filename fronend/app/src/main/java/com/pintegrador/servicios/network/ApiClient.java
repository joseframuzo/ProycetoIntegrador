package com.pintegrador.servicios.network;

import android.content.Context;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pintegrador.servicios.util.Prefs;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    private static OkHttpClient client;
    private static String lastBaseUrl = null;

    public static Retrofit get(Context ctx) {
        String baseUrl = computeBaseUrl(ctx);

        // Si la base cambió o aún no existe, reconstruimos
        if (retrofit == null || client == null || !baseUrl.equals(lastBaseUrl)) {
            lastBaseUrl = baseUrl;

            Gson gson = new GsonBuilder().create();

            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor de Authorization: Bearer <token>
            Interceptor auth = chain -> {
                Request req = chain.request();
                String token = Prefs.getToken(ctx);
                if (token != null && !token.isEmpty()) {
                    req = req.newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .addHeader("Accept", "application/json")
                            .build();
                }
                return chain.proceed(req);
            };

            client = new OkHttpClient.Builder()
                    .addInterceptor(log)   // Déjalo, es útil mientras desarrollas
                    .addInterceptor(auth)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl) // ¡importante que termine con /
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    /** Llama a esto si cambias la URL del servidor en Prefs para forzar reconstrucción. */
    public static void invalidate() {
        retrofit = null;
        client = null;
        lastBaseUrl = null;
    }

    // ---------- Helpers ----------

    private static String computeBaseUrl(Context ctx) {
        // 1) Si el usuario configuró una URL personalizada en Prefs, úsala.
        String custom = safe(Prefs.getServerBaseUrl(ctx)); // opcional
        if (custom != null && custom.startsWith("http")) return ensureSlash(custom);

        // 2) Por defecto: emulador usa 10.0.2.2, dispositivo usa 127.0.0.1 (si hiciste adb reverse)
        if (isEmulator()) return "http://10.0.2.2:4000/";
        return "http://127.0.0.1:4000/";
    }

    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    private static String ensureSlash(String url) {
        return url.endsWith("/") ? url : url + "/";
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
