package com.example.servicios.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.servicios.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explore) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ExploreFragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ProfileFragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_chat) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ConversationsFragment())
                        .commit();
                return true;
            }
            return false;
        });

        nav.setSelectedItemId(R.id.nav_explore);
    }
}
