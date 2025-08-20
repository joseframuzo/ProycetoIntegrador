package com.example.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.servicios.R;
import com.example.servicios.api.ApiClient;
import com.example.servicios.api.ApiService;
import com.example.servicios.data.SessionManager;
import com.example.servicios.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Inputs
    private TextInputEditText edtFullName, edtEmail, edtPhone, edtPassword;
    private MaterialAutoCompleteTextView edtRole;

    // Layouts (errores)
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilPassword, tilRole;

    private MaterialButton btnCreate;
    private View progress;

    private ApiService api;
    private SessionManager session;

    // Reglas
    private static final Pattern NAME_RX  = Pattern.compile("^[\\p{L} ]{2,60}$");
    private static final Pattern PHONE_RX = Pattern.compile("^[0-9]{7,15}$");
    // 1ª mayúscula + al menos una minúscula, un dígito y un símbolo; total >=8
    private static final Pattern PASS_RX  =
            Pattern.compile("^(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])[A-Z][A-Za-z0-9!@#\\$%\\^&\\*]{7,}$");

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);

        api = ApiClient.get().create(ApiService.class);
        session = new SessionManager(this);

        // Refs inputs
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail    = findViewById(R.id.edtEmailReg);
        edtPhone    = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPasswordReg);
        edtRole     = findViewById(R.id.edtRole);
        btnCreate   = findViewById(R.id.btnCreate);
        progress    = findViewById(R.id.progressReg);

        // Refs TIL (IDs deben existir en el XML)
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail    = findViewById(R.id.tilEmail);
        tilPhone    = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilRole     = findViewById(R.id.tilRole);

        // --- Filtros de entrada ---
        // Nombre: solo letras/espacio (teclado, pegado y defensive fix)
        edtFullName.setKeyListener(new android.text.method.DigitsKeyListener(false, false) {
            @Override public int getInputType() {
                return android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS;
            }
            @Override protected char[] getAcceptedChars() {
                String allowed = " áéíóúüñÁÉÍÓÚÜÑ" +
                        "abcdefghijklmnopqrstuvwxyz" +
                        "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                return allowed.toCharArray();
            }
        });
        InputFilter lettersOnly = (src, start, end, dst, dstart, dend) -> {
            StringBuilder out = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = src.charAt(i);
                if (Character.isLetter(c) || Character.isSpaceChar(c)) out.append(c);
            }
            return (out.length() == end - start) ? null : out.toString();
        };
        edtFullName.setFilters(new InputFilter[]{ lettersOnly, new InputFilter.LengthFilter(60) });
        edtFullName.addTextChangedListener(new TextWatcher() {
            private boolean selfFix;
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (selfFix) return;
                String fixed = s.toString().replaceAll("[^\\p{L} ]+", "");
                if (!fixed.equals(s.toString())) { selfFix = true; s.replace(0, s.length(), fixed); selfFix = false; }
            }
        });

        // Teléfono: solo dígitos y máx 15
        edtPhone.setKeyListener(android.text.method.DigitsKeyListener.getInstance("0123456789"));
        edtPhone.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(15) });

        // Rol (dropdown)
        ArrayAdapter<CharSequence> roles = ArrayAdapter.createFromResource(
                this, R.array.roles_array, android.R.layout.simple_list_item_1);
        edtRole.setAdapter(roles);
        edtRole.setOnTouchListener((v, e) -> { if (e.getAction()== MotionEvent.ACTION_UP) edtRole.showDropDown(); return false; });

        // Limpia errores y valida para habilitar botón
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                clearAllErrors();
                btnCreate.setEnabled(validate(false)); // silenciosa para habilitar/deshabilitar
            }
            @Override public void afterTextChanged(Editable s){}
        };
        edtFullName.addTextChangedListener(watcher);
        edtEmail.addTextChangedListener(watcher);
        edtPhone.addTextChangedListener(watcher);
        edtPassword.addTextChangedListener(watcher);
        edtRole.addTextChangedListener(watcher);

        // Estado inicial: botón deshabilitado
        btnCreate.setEnabled(false);

        // Click (NO pongas android:onClick en XML)
        btnCreate.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        Log.d(TAG, "doRegister() clicked");
        // 🚧 GATE FINAL: si falla, NO llama al backend
        if (!validate(true)) {
            Log.d(TAG, "doRegister() blocked by validation");
            return;
        }

        String fullName = txt(edtFullName);
        String email    = txt(edtEmail).toLowerCase(Locale.ROOT).trim();
        String phone    = txt(edtPhone);
        String password = txt(edtPassword);
        String role     = txt(edtRole).toUpperCase(Locale.ROOT).trim();

        progress.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("password", password);
        body.put("role", role);

        api.register(body).enqueue(new Callback<User>() {
            @Override public void onResponse(Call<User> call, Response<User> r) {
                progress.setVisibility(View.GONE);
                btnCreate.setEnabled(true);

                if (r.isSuccessful() && r.body()!=null) {
                    session.save(r.body());
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
                    // pinta error asociado al email (o general)
                    tilEmail.setErrorEnabled(true);
                    tilEmail.setError("No se pudo registrar. Verifica tu correo.");
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** true = todo válido. Modo: silencioso(false) / mostrarErrores(true) */
    private boolean validate(boolean showErrors) {
        boolean ok = true;
        View firstInvalid = null;

        String name = txt(edtFullName);
        if (name.isEmpty()) { setErr(tilFullName, "El nombre es obligatorio", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtFullName); }
        else if (!NAME_RX.matcher(name).matches()) { setErr(tilFullName, "Solo letras y espacios (2–60)", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtFullName); }
        else clr(tilFullName, showErrors);

        String email = txt(edtEmail);
        if (email.isEmpty()) { setErr(tilEmail, "El email es obligatorio", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtEmail); }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { setErr(tilEmail, "Email no válido", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtEmail); }
        else clr(tilEmail, showErrors);

        String phone = txt(edtPhone);
        if (phone.isEmpty()) { setErr(tilPhone, "El teléfono es obligatorio", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtPhone); }
        else if (!PHONE_RX.matcher(phone).matches()) { setErr(tilPhone, "Solo dígitos (7–15)", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtPhone); }
        else clr(tilPhone, showErrors);

        String pass = txt(edtPassword);
        if (pass.isEmpty()) { setErr(tilPassword, "La contraseña es obligatoria", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtPassword); }
        else if (!PASS_RX.matcher(pass).matches()) { setErr(tilPassword, "Debe iniciar con mayúscula e incluir minúscula, número y símbolo (mín. 8)", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtPassword); }
        else clr(tilPassword, showErrors);

        String role = txt(edtRole).toUpperCase(Locale.ROOT).trim();
        boolean roleValid = role.equals("USER") || role.equals("PROFESSIONAL");
        if (role.isEmpty()) { setErr(tilRole, "Selecciona un rol", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtRole); }
        else if (!roleValid) { setErr(tilRole, "Rol inválido (USER o PROFESSIONAL)", showErrors); ok=false; firstInvalid = firstIfNull(firstInvalid, edtRole); }
        else clr(tilRole, showErrors);

        if (!ok && showErrors && firstInvalid != null) firstInvalid.requestFocus();
        Log.d(TAG, "validate(showErrors=" + showErrors + ") -> " + ok);
        return ok;
    }

    private void clearAllErrors() {
        clr(tilFullName, true); clr(tilEmail, true); clr(tilPhone, true); clr(tilPassword, true); clr(tilRole, true);
    }
    private void setErr(TextInputLayout til, String msg, boolean show){
        if (!show || til==null) return;
        til.setErrorEnabled(true); til.setError(msg);
    }
    private void clr(TextInputLayout til, boolean show){
        if (!show || til==null) return;
        til.setError(null); til.setErrorEnabled(false);
    }
    private View firstIfNull(View first, View candidate){ return first==null? candidate: first; }

    // Helpers
    private String txt(TextInputEditText et){ return et.getText()==null? "" : et.getText().toString().trim(); }
    private String txt(MaterialAutoCompleteTextView et){ return et.getText()==null? "" : et.getText().toString().trim(); }
}
