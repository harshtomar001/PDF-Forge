package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.MainPagerAdapter;
import com.harsh.pdfstudio.fragments.HomeFragment;
import com.harsh.pdfstudio.notifications.NotificationActivity;

public class MainActivity extends FontBaseActivity implements HomeFragment.homeActionListener {

    ViewPager2 viewPager_nav_bottom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.home_main_page);

        viewPager_nav_bottom=findViewById(R.id.viewPager_nav_bottom);

        viewPager_nav_bottom.setAdapter(new MainPagerAdapter(this,this));

        viewPager_nav_bottom.setCurrentItem(0,false);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        viewPager_nav_bottom.setUserInputEnabled(false);

        viewPager_nav_bottom.setOffscreenPageLimit(3);

        viewPager_nav_bottom.setPageTransformer(null);

        bottomNav.setItemActiveIndicatorEnabled(false);

        ViewCompat.setOnApplyWindowInsetsListener(
                bottomNav,
                (v, insets) -> {

            v.setPadding(0, 0, 0, 5);
            return insets;

        });

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                viewPager_nav_bottom.setCurrentItem(0, false);
                return true;
            }

            if (id == R.id.nav_tools) {

                viewPager_nav_bottom.setCurrentItem(1, false);

                return true;
            }

            if (id == R.id.nav_history) {

                viewPager_nav_bottom.setCurrentItem(2, false);
                return true;
            }

            if (id == R.id.nav_settings) {

                viewPager_nav_bottom.setCurrentItem(3 , false);
                return true;

            }

            return false;
        });


        handleIntent(getIntent());


    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        setIntent(intent);

        handleIntent(intent);
    }


    @Override
    public void  onLoginClicked(){

        Intent intent = new Intent(this ,  AuthActivity.class);
        startActivity(intent);

    };

    @Override
    public void onProfileClicked() {

        Intent intent = new Intent(
                MainActivity.this,
                ProfileActivity.class
        );

        startActivity(intent);
    }


    @Override
    public void  onNotificationClicked(){

        Intent intent = new Intent(this , NotificationActivity.class);
        startActivity(intent);


    };

    @Override
    public void  onSearchClicked(){

    };


    private void handleIntent(Intent intent) {

        if (intent.getBooleanExtra("open_settings", false)) {

            viewPager_nav_bottom.setCurrentItem(3, false);

            BottomNavigationView bottomNav =
                    findViewById(R.id.bottomNavigation);

            bottomNav.setSelectedItemId(R.id.nav_settings);

            intent.removeExtra("open_settings");
        }
    }


}