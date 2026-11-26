package com.harsh.pdfstudio.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.harsh.pdfstudio.fragments.HistoryFragment;
import com.harsh.pdfstudio.fragments.HomeFragment;
import com.harsh.pdfstudio.fragments.SettingsFragment;
import com.harsh.pdfstudio.fragments.ToolsFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    private final HomeFragment.homeActionListener listener;

    public MainPagerAdapter(
            FragmentActivity activity,
            HomeFragment.homeActionListener listener
    ) {
        super(activity);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:

                HomeFragment homeFragment = new HomeFragment();

                homeFragment.setOnHomeActionListener(listener);

                return homeFragment;

            case 1:
                return new ToolsFragment();

            case 2:
                return new HistoryFragment();

            case 3:
                return new SettingsFragment();

            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}