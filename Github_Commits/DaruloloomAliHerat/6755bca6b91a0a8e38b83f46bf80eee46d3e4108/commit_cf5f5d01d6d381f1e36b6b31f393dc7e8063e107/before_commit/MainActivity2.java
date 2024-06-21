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
//    int WHICH_FRAGMENT = 0;

    Boolean onBackPressedOnce;
//    Fragment[] fragment;

    HomeFragment homeFragment;
    FavoriteFragment favoriteFragment;
    SettingsFragment settingsFragment;

//    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

//        fab = findViewById(R.id.fab);

        //first run of app applicationMode will get light mode
        /*try {
            if (Global.getStat(MainActivity2.this, "applicationMode").equals(null)) {
            }
        } catch (Exception e) {
            Global.saveState(MainActivity2.this, "applicationMode", "light");
        }*/

//        darkLightMode();

        onBackPressedOnce = false;


        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Global.getStat(MainActivity2.this, "applicationMode").equals("light")){
                    Global.saveState(MainActivity2.this, "applicationMode", "dark");
                }else if (Global.getStat(MainActivity2.this, "applicationMode").equals("dark")){
                    Global.saveState(MainActivity2.this, "applicationMode", "light");
                }
                Toast.makeText(MainActivity2.this, "it is just for test Doctor", Toast.LENGTH_SHORT).show();

                darkLightMode();

                *//*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*//*
               // fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
//                fab.setImageResource(R.drawable.ic_baseline_wb_sunny_24);
               // fab.setRippleColor(11846103);
                //view.setBackgroundColor(11846103);
            }
        });*/



        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            favoriteFragment = new FavoriteFragment();
            settingsFragment = new SettingsFragment();


//            fragment = new Fragment[]{new HomeFragment(), new FavoriteFragment(), new SettingsFragment()};
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();
        }else {
//            WHICH_FRAGMENT = savedInstanceState.getInt("fragmentNumber");
//            if (WHICH_FRAGMENT == 2){
//                favoriteFragment = new FavoriteFragment();
//                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, favoriteFragment).commit();
//            }
//            if (WHICH_FRAGMENT == 3){
//                settingsFragment = new SettingsFragment();
//                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, settingsFragment).commit();
//            }
        }


        bottomBar = findViewById(R.id.bottomBar);
        bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                String activeFragment = Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getClass().getSimpleName();

                if (item.getTitle().equals("خانه")) {

                    checkFavoriteButtons();
                    

//                    WHICH_FRAGMENT = 1;
                    try {
                        getSupportFragmentManager().beginTransaction().remove(favoriteFragment).commit();
                    } catch (Exception e) {

                    }
                    try {
                        getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();
                    } catch (Exception e) {

                    }

                    favoriteFragment.closeHomeFragment();



//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment[0]).commit();




                    /*String activeFragment1 = Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getClass().getSimpleName();
                    if (!activeFragment1.equals(HomeFragment.class.getSimpleName())){

                        try {
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                        }catch (Exception e){

                        }

                    }*/






                    //getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment[0]).commit();
                } else if (item.getTitle().equals("دلخواه")) {
//                    WHICH_FRAGMENT = 2;

                    try {
                        /*HomeFragment homeFragment= (HomeFragment) fragment[0];
                        homeFragment.closeHomeFragment();*/
//                        homeFragment.closeHomeFragment();
                    } catch (Exception e) {
                    }

                    try {
                        getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();
                    } catch (Exception e) {

                    }
                    if (!activeFragment.equals(FavoriteFragment.class.getSimpleName())){
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, favoriteFragment).commit();
                    }

                } else if (item.getTitle().equals("در باره ما")) {
//                    WHICH_FRAGMENT = 3;

                    try {
                        /*HomeFragment homeFragment= (HomeFragment) fragment[0];
                        homeFragment.closeHomeFragment();*/
//                        homeFragment.closeHomeFragment();
                    } catch (Exception e) {

                    }

                    try {
                        getSupportFragmentManager().beginTransaction().remove(favoriteFragment).commit();
                    }catch (Exception e){

                    }
                    if (!activeFragment.equals(SettingsFragment.class.getSimpleName())){
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, settingsFragment).commit();
                    }


                    favoriteFragment.closeHomeFragment();
                }
                return true;
            }
        });
    }

    private void checkFavoriteButtons() {
        if (Global.getStat(MainActivity2.this,"favorite_state1").equals("border")){
            homeFragment.favoriteButton1.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state2").equals("border")){
            homeFragment.favoriteButton2.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state3").equals("border")){
            homeFragment.favoriteButton3.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state4").equals("border")){
            homeFragment.favoriteButton4.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state5").equals("border")){
            homeFragment.favoriteButton5.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state6").equals("border")){
            homeFragment.favoriteButton6.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state7").equals("border")){
            homeFragment.favoriteButton7.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state8").equals("border")){
            homeFragment.favoriteButton8.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state9").equals("border")){
            homeFragment.favoriteButton9.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        if (Global.getStat(MainActivity2.this,"favorite_state10").equals("border")){
            homeFragment.favoriteButton10.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

       /* if (WHICH_FRAGMENT == 2){
            getSupportFragmentManager().beginTransaction().remove(favoriteFragment).commit();
        }
        if (WHICH_FRAGMENT == 3){
            getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();
        }
        outState.putInt("fragmentNumber", WHICH_FRAGMENT);*/

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
    


    /*void darkLightMode(){
        if (Global.getStat(MainActivity2.this, "applicationMode").equals("light")){
            //fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00BCD4")));
            fab.setImageResource(R.drawable.ic_baseline_wb_sunny_24);
        }else if (Global.getStat(MainActivity2.this, "applicationMode").equals("dark")){
            //fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5D7677")));
            fab.setImageResource(R.drawable.ic_baseline_bedtime_24);
        }
    }*/
}