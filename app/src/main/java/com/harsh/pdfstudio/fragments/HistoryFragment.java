package com.harsh.pdfstudio.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.HistoryAdapter;
import com.harsh.pdfstudio.model.RecentFileItem;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    View noRecentFilesImg;
    LinearLayout recentLayout;
    ArrayList<RecentFileItem> list;


    public HistoryFragment(){
        super(R.layout.history_page);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {

        super.onViewCreated(view, bundle);



        noRecentFilesImg =
                view.findViewById(R.id.no_recentFileImg);

        recentLayout =
                view.findViewById(R.id.history_layout);

        RecyclerView recyclerView =
                view.findViewById(R.id.recycler_view_recent_files);

        // IMPORTANT: no new local variable
        list = getRecentFiles(requireContext());

        HistoryAdapter adapter =
                new HistoryAdapter(list, requireContext());

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerView.setAdapter(adapter);

        adapter.setOnRemoveFile(() -> {
            isListEmpty();
        });

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_home_page);

        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {

                Insets systemBars =
                        insets.getInsets(WindowInsetsCompat.Type.statusBars());

                v.setPadding(
                        v.getPaddingLeft(),
                        (int) (systemBars.top / 2f),
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );

                return insets;
            });
        }


    }

    private ArrayList<RecentFileItem> getRecentFiles(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        "files",
                        MODE_PRIVATE
                );

        Gson gson = new Gson();

        String json =
                prefs.getString("recent_files", null);

        Type type =
                new TypeToken<ArrayList<RecentFileItem>>() {}.getType();

        if (json != null && !json.isEmpty()) {

            ArrayList<RecentFileItem> files =
                    gson.fromJson(json, type);

            if (files == null) {
                files = new ArrayList<>();
            }

            updateEmptyState(files);

            return files;
        }

        ArrayList<RecentFileItem> emptyList =
                new ArrayList<>();

        updateEmptyState(emptyList);

        return emptyList;
    }

    public void isListEmpty() {

        if (list == null) {
            return;
        }

        updateEmptyState(list);
    }

    private void updateEmptyState(ArrayList<RecentFileItem> files) {

        if (files.isEmpty()) {

            noRecentFilesImg.setVisibility(View.VISIBLE);
            recentLayout.setVisibility(View.GONE);

        } else {

            noRecentFilesImg.setVisibility(View.GONE);
            recentLayout.setVisibility(View.VISIBLE);
        }
    }

}
