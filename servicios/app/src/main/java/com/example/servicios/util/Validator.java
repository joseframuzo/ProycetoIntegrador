package com.example.servicios.util;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Validator {

    private Validator() {}

    // Reglas (Unicode: \p{L} = letra en cualquier idioma)
    private static final Pattern NAME_RX  = Pattern.compile("^[\\p{L} ]{2,60}$");
    private static final Pattern PHONE_RX = Pattern.compile("^[0-9]{7,15}$");
    // 1ª mayúscula + al menos una minúscula, un dígito y un símbolo; longitud total >=8
    private static final Pattern PASS_RX  =
            Pattern.compile("^(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])[A-Z][A-Za-z0-9!@#\\$%\\^&\\*]{7,}$");

    /** Devuelve true si todos los campos son válidos. Pinta errores en rojo y enfoca el 1º inválido. */
    public static boolean validateRegisterForm(
            TextInputLayout tilFullName, TextInputEditText edtFullName,
            TextInputLayout tilEmail,    TextInputEditText edtEmail,
            TextInputLayout tilPhone,    TextInputEditText edtPhone,
            TextInputLayout tilPassword, TextInputEditText edtPassword,
            TextInputLayout tilRole,     MaterialAutoCompleteTextView edtRole) {

        boolean ok = true;
        View firstInvalid = null;

        // NOMBRE
        String name = text(edtFullName);
        if (TextUtils.isEmpty(name)) {
            setError(tilFullName, "El nombre es obligatorio");
            ok = false; if (firstInvalid == null) firstInvalid = edtFullName;
        } else if (!NAME_RX.matcher(name).matches()) {
            setError(tilFullName, "Solo letras y espacios (2–60)");
            ok = false; if (firstInvalid == null) firstInvalid = edtFullName;
        } else clearError(tilFullName);

        // EMAIL
        String email = text(edtEmail);
        if (TextUtils.isEmpty(email)) {
            setError(tilEmail, "El email es obligatorio");
            ok = false; if (firstInvalid == null) firstInvalid = edtEmail;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(tilEmail, "Email no válido");
            ok = false; if (firstInvalid == null) firstInvalid = edtEmail;
        } else clearError(tilEmail);

        // TELÉFONO
        String phone = text(edtPhone);
        if (TextUtils.isEmpty(phone)) {
            setError(tilPhone, "El teléfono es obligatorio");
            ok = false; if (firstInvalid == null) firstInvalid = edtPhone;
        } else if (!PHONE_RX.matcher(phone).matches()) {
            setError(tilPhone, "Solo dígitos (7–15)");
            ok = false; if (firstInvalid == null) firstInvalid = edtPhone;
        } else clearError(tilPhone);

        // CONTRASEÑA
        String pass = text(edtPassword);
        if (TextUtils.isEmpty(pass)) {
            setError(tilPassword, "La contraseña es obligatoria");
            ok = false; if (firstInvalid == null) firstInvalid = edtPassword;
        } else if (!PASS_RX.matcher(pass).matches()) {
            setError(tilPassword, "Debe iniciar con mayúscula e incluir minúscula, número y símbolo (mín. 8)");
            ok = false; if (firstInvalid == null) firstInvalid = edtPassword;
        } else clearError(tilPassword);

        // ROL
        String role = text(edtRole).toUpperCase(Locale.ROOT).trim();
        boolean roleValid = role.equals("USER") || role.equals("PROFESSIONAL");
        if (TextUtils.isEmpty(role)) {
            setError(tilRole, "Selecciona un rol");
            ok = false; if (firstInvalid == null) firstInvalid = edtRole;
        } else if (!roleValid) {
            setError(tilRole, "Rol inválido (USER o PROFESSIONAL)");
            ok = false; if (firstInvalid == null) firstInvalid = edtRole;
        } else clearError(tilRole);

        if (!ok && firstInvalid != null) firstInvalid.requestFocus();
        return ok;
    }

    // Helpers visibles públicamente si quieres validar individuales
    public static boolean isValidName(String s){ return !TextUtils.isEmpty(s) && NAME_RX.matcher(s).matches(); }
    public static boolean isValidEmail(String s){ return !TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches(); }
    public static boolean isValidPhone(String s){ return !TextUtils.isEmpty(s) && PHONE_RX.matcher(s).matches(); }
    public static boolean isValidPassword(String s){ return !TextUtils.isEmpty(s) && PASS_RX.matcher(s).matches(); }
    public static boolean isValidRole(String s){
        if (TextUtils.isEmpty(s)) return false;
        String r = s.toUpperCase(Locale.ROOT).trim();
        return r.equals("USER") || r.equals("PROFESSIONAL");
    }

    // Internos
    private static void setError(TextInputLayout til, String msg){
        if (til == null) return;
        til.setErrorEnabled(true);
        til.setError(msg);
    }
    private static void clearError(TextInputLayout til){
        if (til == null) return;
        til.setError(null);
        til.setErrorEnabled(false);
    }
    private static String text(TextInputEditText et){ return et.getText()==null? "": et.getText().toString().trim(); }
    private static String text(MaterialAutoCompleteTextView et){ return et.getText()==null? "": et.getText().toString().trim(); }
}
