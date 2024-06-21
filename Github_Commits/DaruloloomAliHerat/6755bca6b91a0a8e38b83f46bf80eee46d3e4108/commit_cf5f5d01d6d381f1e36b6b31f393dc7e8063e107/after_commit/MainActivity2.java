package com.ehsanhaidary.arefmorady;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class MainActivity2 extends AppCompatActivity {

    BottomNavigationView bottomBar;


    Boolean onBackPressedOnce;


    HomeFragment homeFragment;
    FavoriteFragment favoriteFragment;
    SettingsFragment settingsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        onBackPressedOnce = false;


        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            favoriteFragment = new FavoriteFragment();
            settingsFragment = new SettingsFragment();


            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();
        } else {
//
        }


        bottomBar = findViewById(R.id.bottomBar);
        bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                String activeFragment = Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getClass().getSimpleName();

                if (item.getTitle().equals("خانه")) {

                    checkFavoriteButtons();
                    try {
                        getSupportFragmentManager().beginTransaction().remove(favoriteFragment).commit();
                    } catch (Exception e) {

                    }
                    try {
                        getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();
                    } catch (Exception e) {

                    }

                    favoriteFragment.closeHomeFragment();

                } else if (item.getTitle().equals("دلخواه")) {


                    try {
                        getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();
                    } catch (Exception e) {

                    }
                    if (!activeFragment.equals(FavoriteFragment.class.getSimpleName())) {
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, favoriteFragment).commit();
                    }

                } else if (item.getTitle().equals("در باره ما")) {


                    try {
                        getSupportFragmentManager().beginTransaction().remove(favoriteFragment).commit();
                    } catch (Exception e) {

                    }
                    if (!activeFragment.equals(SettingsFragment.class.getSimpleName())) {
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, settingsFragment).commit();
                    }


                    favoriteFragment.closeHomeFragment();
                }
                return true;
            }
        });
    }

    private void checkFavoriteButtons() {
        if (Global.getStat(MainActivity2.this, "favorite_state1").equals("border")) {
            homeFragment.favoriteButton1.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state2").equals("border")) {
            homeFragment.favoriteButton2.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state3").equals("border")) {
            homeFragment.favoriteButton3.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state4").equals("border")) {
            homeFragment.favoriteButton4.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state5").equals("border")) {
            homeFragment.favoriteButton5.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state6").equals("border")) {
            homeFragment.favoriteButton6.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state7").equals("border")) {
            homeFragment.favoriteButton7.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state8").equals("border")) {
            homeFragment.favoriteButton8.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state9").equals("border")) {
            homeFragment.favoriteButton9.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this, "favorite_state10").equals("border")) {
            homeFragment.favoriteButton10.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {


        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBackPressed() {

        String activeFragment = Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getClass().getSimpleName();

        if (activeFragment.equals(FavoriteFragment.class.getSimpleName())){
            try {
                getSupportFragmentManager().beginTransaction().remove(favoriteFragment).commit();
                bottomBar.setSelectedItemId(R.id.home);
                return;
            } catch (Exception e) {

            }

        }

        if (activeFragment.equals(SettingsFragment.class.getSimpleName())){
            try {
                getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();
                bottomBar.setSelectedItemId(R.id.home);
                return;
            } catch (Exception e) {

            }
        }


        if (onBackPressedOnce) {
            homeFragment.removeNotification();
            super.onBackPressed();
        }

        onBackPressedOnce = true;
        Toast.makeText(this, "جهت خروج از برنامه، دکمه برگشت را دوباره فشار دهید.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onBackPressedOnce = false;
            }
        }, 2000);

    }

    @Override
    protected void onDestroy() {
        homeFragment.removeNotification();
        super.onDestroy();
    }

}