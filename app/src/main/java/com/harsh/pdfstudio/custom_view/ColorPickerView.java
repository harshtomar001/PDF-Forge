package com.harsh.pdfstudio.custom_view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.harsh.pdfstudio.adapter.RecentColorsAdapter;
import com.harsh.pdfstudio.databinding.ColorPickerViewBinding;
import java.util.ArrayList;
import java.util.List;


/*
 * Production fixes applied to the original version:
 *
 * 1. SEVERE BUG: attachRgbWatcher() was called from inside updateFromColor(), which
 *    runs on *every single color change* (every drag frame, every button tap). Each
 *    call added three brand-new TextWatchers to the R/G/B fields without ever removing
 *    the old ones. After a short interaction the fields would accumulate dozens of
 *    stacked watchers, each firing on every keystroke - causing runaway
 *    afterTextChanged() calls, visible jank, and increasingly wrong behavior the longer
 *    the picker was used. Watchers are now attached exactly once, in connectViews().
 *
 * 2. BUG: only color changes that came from dragging the hue/saturation views notified
 *    the external OnColorChangedListener. Typing a hex value, using the R/G/B steppers,
 *    or (previously) tapping a "recent color" never told the host app the color had
 *    changed, even though the picker's own UI updated - a host reacting to color
 *    changes (e.g. applying it to a document) would silently miss those updates.
 *    Listener notification is now centralized inside updateFromColor(), so every
 *    code path notifies exactly once, consistently.
 *
 * 3. MISSING FEATURE: the hex field had a copy button but no way to actually type a
 *    hex code and apply it. Added commit-on-focus-lost / commit-on-IME-done handling
 *    with validation (invalid input reverts to the last valid hex instead of crashing
 *    or applying garbage).
 *
 * 4. BUG: parseValue() called Integer.parseInt() with no exception handling - any
 *    non-numeric input in the R/G/B fields (e.g. from a stray character) would crash
 *    the app. Now caught safely.
 *
 * 5. The "recent colors" row was a hardcoded, non-functional mock: tapping a swatch did
 *    nothing, and nothing was ever actually added to it. Implemented a real recent-color
 *    list (de-duplicated, capped, most-recent-first) that applies the color on tap.
 *    Note: this assumes RecentColorsAdapter reads its color list live from the same
 *    List reference passed into its constructor (the common RecyclerView.Adapter
 *    pattern) - if it copies the list internally instead, swap notifyDataSetChanged()
 *    for whatever update method the adapter exposes.
 *
 * 6. Removed duplicate view lookups (findViewById() for views ViewBinding already
 *    exposes with the correct custom-view type) and an unused `context` field in favor
 *    of getContext() - fewer places for the two references to drift apart.
 *
 * 7. Programmatic updates (updateFromColor) now skip re-setting the text of whichever
 *    EditText currently has focus, so typing in one field doesn't get its cursor
 *    reset mid-keystroke by the picker syncing itself.
 */

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.harsh.pdfstudio.adapter.RecentColorsAdapter;
import com.harsh.pdfstudio.databinding.ColorPickerViewBinding;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerView extends FrameLayout {

    private static final int MAX_RECENT_COLORS = 14;

    private ColorPickerViewBinding binding;

    private int selectedColor;
    private final float[] hsv = new float[3];
    private boolean updating = false;

    private final List<Integer> recentColors = new ArrayList<>();
    private RecentColorsAdapter recentColorsAdapter;

    public interface OnColorChangedListener {
        void onColorChanged(int color);
        void getHex(String hex);
    }

    private OnColorChangedListener listener;

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    public ColorPickerView(Context context) {
        super(context);
        init(context);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        binding = ColorPickerViewBinding.inflate(LayoutInflater.from(context), this, true);

        connectViews();
        initRecentColors();

        selectedColor = binding.saturationSlider.getSelectedColor();
        updateFromColor(selectedColor, true); // populate hex/RGB/HSV fields on first load
    }

    private void connectViews() {

        binding.hueSlider.setOnHueChangeListener(hue -> {
            if (updating) return;
            binding.saturationSlider.setHue(hue);
        });

        binding.saturationSlider.setOnColorChangedListener(new SaturationGLView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                if (updating) return;
                updateFromColor(color,  true);
            }

            @Override
            public void getHex(String hex) {
                // No-op: updateFromColor() derives and applies the hex itself.
            }
        });

        binding.tilHex.setEndIconOnClickListener(v -> {

            String hex = binding.etHex.getText() != null ? binding.etHex.getText().toString().trim() : "";

            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard != null) {

                clipboard.setPrimaryClip(ClipData.newPlainText("HEX", hex));

                Toast.makeText(getContext(), "Copied", Toast.LENGTH_SHORT).show();

            }

        });

        binding.etHex.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                applyHexInput();
                return true;
            }
            return false;
        });

        binding.etHex.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) applyHexInput();

        });

        binding.btnRedUp.setOnClickListener(v -> stepChannel(0, +1));
        binding.btnRedDown.setOnClickListener(v -> stepChannel(0, -1));

        binding.btnGreenUp.setOnClickListener(v -> stepChannel(1, +1));
        binding.btnGreenDown.setOnClickListener(v -> stepChannel(1, -1));

        binding.btnBlueUp.setOnClickListener(v -> stepChannel(2, +1));
        binding.btnBlueDown.setOnClickListener(v -> stepChannel(2, -1));


        binding.btnHueUp.setOnClickListener(v -> stepHue(+1));
        binding.btnHueDown.setOnClickListener(v -> stepHue(-1));

        binding.btnSaturationUp.setOnClickListener(v -> stepSaturation(+1));
        binding.btnSaturationDown.setOnClickListener(v -> stepSaturation(-1));

        binding.btnValueUp.setOnClickListener(v -> stepValue(+1));
        binding.btnValueDown.setOnClickListener(v -> stepValue(-1));


        attachRgbWatcher(binding.etRed, 0);
        attachRgbWatcher(binding.etGreen, 1);
        attachRgbWatcher(binding.etBlue, 2);

        attachHsvWatcher(binding.etHue ,0 );
        attachHsvWatcher(binding.etSaturation, 1);
        attachHsvWatcher(binding.etValue, 2);

    }

    private void stepChannel(int channel, int delta) {

        int r = Color.red(selectedColor);
        int g = Color.green(selectedColor);
        int b = Color.blue(selectedColor);

        switch (channel) {

            case 0: r = clamp(r + delta); break;
            case 1: g = clamp(g + delta); break;
            case 2: b = clamp(b + delta); break;
        }

        updateFromColor(Color.rgb(r, g, b), true );
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void applyHexInput() {

        if (updating) return;

        String raw = binding.etHex.getText() != null ? binding.etHex.getText().toString().trim() : "";

        String hex = raw.startsWith("#") ? raw.substring(1) : raw;

        if (!hex.matches("[0-9a-fA-F]{6}")) {
            // Invalid input - revert to the last valid color instead of applying garbage.

            binding.etHex.setText(getSelectedHex(selectedColor));

            return;
        }

        int color = Color.parseColor("#" + hex);

        setColor(color);
        pushRecentColor(color); // a typed hex is a deliberate, discrete choice
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    /** Sets the picker's color programmatically (e.g. "edit existing element" flows). */

    public void setColor(int color) {

        updating = true;

        float[] targetHsv = new float[3];
        Color.colorToHSV(color, targetHsv);

        binding.hueSlider.setHue(targetHsv[0]);
        binding.saturationSlider.setHue(targetHsv[0]);

        updating = false;

        updateFromColor(color ,  false); // single authoritative update + listener notification
    }

    private void initRecentColors() {

        recentColors.clear();
        recentColors.add(Color.WHITE);
        recentColors.add(Color.BLACK);
        recentColors.add(Color.RED);
        recentColors.add(Color.parseColor("#2962FF")); // indigo/electric-blue, matches VaultX palette
        recentColors.add(Color.parseColor("#00C853"));
        recentColors.add(Color.parseColor("#FFD600"));

        recentColorsAdapter = new RecentColorsAdapter(recentColors, (color, position) -> setColor(color));
        binding.rvRecentColors.setAdapter(recentColorsAdapter);
    }

    /**
     * Call this when the user commits to a color (e.g. tapping "Done" on a picker
     * dialog hosting this view), so it gets remembered for next time.
     */
    public void commitCurrentColorToRecents() {

        pushRecentColor(selectedColor);

    }

    private void pushRecentColor(int color) {

        recentColors.remove((Integer) color); // de-dupe if already present

        recentColors.add(0, color);

        while (recentColors.size() > MAX_RECENT_COLORS) {

            recentColors.remove(recentColors.size() - 1);

        }

        if (recentColorsAdapter != null) {
            recentColorsAdapter.notifyDataSetChanged();
        }
    }

    private void updateFromColor(int color, boolean force) {

        if (updating) return;

        updating = true;

        selectedColor = color;
        Color.colorToHSV(color, hsv);

        binding.hueSlider.setHue(hsv[0]);
        binding.saturationSlider.setHSV(hsv[0], hsv[1], hsv[2]);
        binding.selectedColorPreview.setBackgroundColor(color);

        String hex = getSelectedHex(color);

        setTextIfNotFocused(binding.etHex, hex, force);
        setTextIfNotFocused(binding.etRed, String.valueOf(Color.red(color)),force);
        setTextIfNotFocused(binding.etGreen, String.valueOf(Color.green(color)),force);
        setTextIfNotFocused(binding.etBlue, String.valueOf(Color.blue(color)),force);
        setTextIfNotFocused(binding.etHue, String.valueOf(Math.round(hsv[0])), force);
        setTextIfNotFocused(binding.etSaturation, String.valueOf(Math.round(hsv[1] * 100)), force);
        setTextIfNotFocused(binding.etValue, String.valueOf(Math.round(hsv[2] * 100)), force);

        updating = false;

        if (listener != null) {
            listener.onColorChanged(color);
            listener.getHex(hex);
        }
    }

    /** Avoids clobbering text/cursor position in whichever field the user is actively editing. */
    private void setTextIfNotFocused(EditText editText, String value, boolean force) {

        if ( !force && editText.hasFocus()) return;

        String current = editText.getText() == null
                ? ""
                : editText.getText().toString();

        if (!current.equals(value)) {
            editText.setText(value);
        }
    }

    public String getSelectedHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    private int parseValue(String text) {

        if (text == null || text.isEmpty()) return 0;

        try {
            return clamp(Integer.parseInt(text.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void attachRgbWatcher(EditText editText, int channel) {

        editText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                if (updating) return;

                int value = parseValue(s.toString());

                int r = Color.red(selectedColor);
                int g = Color.green(selectedColor);
                int b = Color.blue(selectedColor);

                switch (channel) {
                    case 0: r = value; break;
                    case 1: g = value; break;
                    case 2: b = value; break;
                }

                updateFromColor(Color.rgb(r, g, b), false);
            }
        });
    }

    private void attachHsvWatcher(EditText editText, int channel) {

        editText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                if (updating) return;

                String text = s.toString().trim();
                if (text.isEmpty()) return;

                float[] hsv = new float[3];

                Color.colorToHSV(selectedColor, hsv);

                try {

                    switch (channel) {

                        // Hue
                        case 0:
                            hsv[0] = Math.max(0f,
                                    Math.min(360f,
                                            Float.parseFloat(text)));
                            break;

                        // Saturation
                        case 1:
                            hsv[1] = Math.max(0f,
                                    Math.min(100f,
                                            Float.parseFloat(text)))
                                    / 100f;
                            break;

                        // Value
                        case 2:
                            hsv[2] = Math.max(0f,
                                    Math.min(100f,
                                            Float.parseFloat(text)))
                                    / 100f;
                            break;
                    }

                    updateFromColor(Color.HSVToColor(hsv), false);

                } catch (NumberFormatException ignored) {

                }
            }
        });
    }

    private void stepHue(int delta) {

        float[] hsv = new float[3];
        Color.colorToHSV(selectedColor, hsv);

        hsv[0] += delta;

        if (hsv[0] < 0)
            hsv[0] += 360;

        if (hsv[0] > 360)
            hsv[0] -= 360;

        updateFromColor(Color.HSVToColor(hsv), true);
    }

    private void stepSaturation(int delta) {

        float[] hsv = new float[3];
        Color.colorToHSV(selectedColor, hsv);

        hsv[1] = Math.max(0f,
                Math.min(1f,
                        hsv[1] + delta / 100f));

        updateFromColor(Color.HSVToColor(hsv) ,  true);
    }

    private void stepValue(int delta) {

        float[] hsv = new float[3];
        Color.colorToHSV(selectedColor, hsv);

        hsv[2] = Math.max(0f,
                Math.min(1f,
                        hsv[2] + delta / 100f));

        updateFromColor(Color.HSVToColor(hsv)  , true);
    }

    public  int  getColor(){

        return  selectedColor;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public abstract void afterTextChanged(Editable s);

    }
}