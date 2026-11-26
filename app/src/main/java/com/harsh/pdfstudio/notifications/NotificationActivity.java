package com.harsh.pdfstudio.notifications;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.harsh.pdfstudio.R;

import java.util.ArrayList;
import java.util.List;


public class NotificationActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private Chip chipAll;
    private Chip chipSuccess;
    private Chip chipInfo;
    private Chip chipWarning;

    private RecyclerView recyclerView;
    private View emptyStateContainer;

    private ImageButton backButton;

    private NotificationAdapter adapter;

    /** Full, unfiltered dataset. This is the "source of truth" for the screen. */
    private final List<NotificationItem> allNotifications = new ArrayList<>();

    /** Currently selected filter. Null represents "All". */
    @Nullable
    private NotificationType selectedFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        bindViews();
        setUpToolbar();
        setUpRecyclerView();
        setUpChipFiltering();
        loadData();
    }

    private void bindViews() {
        chipGroup = findViewById(R.id.chip_group_filters);
        chipAll = findViewById(R.id.chip_all);
        chipSuccess = findViewById(R.id.chip_success);
        chipInfo = findViewById(R.id.chip_info);
        chipWarning = findViewById(R.id.chip_warning);
        recyclerView = findViewById(R.id.recycler_notifications);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        backButton =  findViewById(R.id.backButtonNotificationActivity);
    }

    private void setUpToolbar() {
        LinearLayout toolbar = findViewById(R.id.toolbar);

        backButton.setOnClickListener(v -> finish());



        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                v.getPaddingLeft(),
                                (int) (systemBars.top / 1.5f),
                                v.getPaddingRight(),
                                v.getPaddingBottom()
                        );

                        return insets;
                    });
        }

        View settingsButton = findViewById(R.id.btn_settings);
        settingsButton.setOnClickListener(this::showSettingsMenu);
    }

    private void showSettingsMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_notification_settings, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_mark_all_read) {
                markAllAsRead();
                return true;
            } else if (id == R.id.action_clear_notifications) {
                clearAllNotifications();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void setUpRecyclerView() {
        adapter = new NotificationAdapter((item, position) -> onNotificationClicked(item));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    private void setUpChipFiltering() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Never allow zero selection - fall back to "All".
                chipAll.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                selectedFilter = null;
            } else if (checkedId == R.id.chip_success) {
                selectedFilter = NotificationType.SUCCESS;
            } else if (checkedId == R.id.chip_info) {
                selectedFilter = NotificationType.INFO;
            } else if (checkedId == R.id.chip_warning) {
                selectedFilter = NotificationType.WARNING;
            }
            applyFilter();
        });
    }

    private void loadData() {
        allNotifications.clear();
        allNotifications.addAll(NotificationRepository.getNotifications());
        updateChipCounts();
        applyFilter();
    }

    /** Filters {@link #allNotifications} by {@link #selectedFilter} and pushes to the adapter. */
    private void applyFilter() {
        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem item : allNotifications) {
            if (selectedFilter == null || item.getType() == selectedFilter) {
                filtered.add(item);
            }
        }
        adapter.submitList(filtered);
        toggleEmptyState(filtered.isEmpty());
    }

    private void toggleEmptyState(boolean isEmpty) {
        emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Updates the count badge shown on each chip based on the current
     * contents of {@link #allNotifications}. Called after data loads and
     * after mutating actions (mark all read doesn't change counts, but
     * clearing does).
     */
    private void updateChipCounts() {
        int successCount = 0;
        int infoCount = 0;
        int warningCount = 0;

        for (NotificationItem item : allNotifications) {
            switch (item.getType()) {
                case SUCCESS:
                    successCount++;
                    break;
                case INFO:
                    infoCount++;
                    break;
                case WARNING:
                    warningCount++;
                    break;
            }
        }

        int allCount = allNotifications.size();

        chipAll.setText(getString(R.string.filter_chip_format, getString(R.string.filter_all), allCount));
        chipSuccess.setText(getString(R.string.filter_chip_format, getString(R.string.filter_success), successCount));
        chipInfo.setText(getString(R.string.filter_chip_format, getString(R.string.filter_info), infoCount));
        chipWarning.setText(getString(R.string.filter_chip_format, getString(R.string.filter_warning), warningCount));
    }

    private void markAllAsRead() {
        for (NotificationItem item : allNotifications) {
            item.setRead(true);
        }
        applyFilter();
        // TODO: persist read state via Room/SharedPreferences/API here.
    }

    private void clearAllNotifications() {
        allNotifications.clear();
        updateChipCounts();
        applyFilter();
        // TODO: persist the cleared state via Room/SharedPreferences/API here.
    }

    private void onNotificationClicked(NotificationItem item) {
        if (!item.isRead()) {
            item.setRead(true);
            applyFilter();
            // TODO: persist read state for this single item.
        }
        // TODO: navigate to a detail screen or perform the notification's action.
    }
}
