package com.harsh.pdfstudio.managers;


import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.harsh.pdfstudio.model.FontResult;

import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private final Context context;

    private boolean fontFallback = false;

    private  boolean weightFallback = false;


    // Cache loaded Typefaces so we don't repeatedly load .ttf files
    private final Map<String, Typeface> fontCache = new HashMap<>();

    public FontManager(Context context) {
        this.context = context.getApplicationContext();

    }


    public FontResult getFont(String family, int weight, boolean italic) {

        if (family == null || family.isEmpty()) {
            family = "Poppins";
        }

        // Only allow the weights your application supports
        weight = normalizeWeight(weight);


        String cacheKey = family + "_" + weight + "_" + italic;


        // Return cached Typeface
        if (fontCache.containsKey(cacheKey)) {

            return new FontResult(fontCache.get(cacheKey) , this.weightFallback , this.fontFallback);
        }

        // Load the actual weight file
        Typeface typeface = loadFont(family, weight);


        // Apply synthetic italic
        if (italic) {

            typeface = Typeface.create(
                    typeface,
                    Typeface.ITALIC
            );
        }

        // Save in cache
        fontCache.put(
                cacheKey,
                typeface
        );

        return new FontResult(typeface , this.weightFallback, this.fontFallback );
    }


    private Typeface loadFont(String family, int weight) {



        String path = getFontPath(family, weight);

        // If exact weight is unavailable,
        // fall back to Regular.

        if (path == null) {

            this.weightFallback = true;

            this.fontFallback = false;

            path = getFontPath(family, 400);

        }


        if (path == null) {

            this.weightFallback  = false; //  make sure that weight fallback is not enabled //  only shows font unavailable message rather than both

            this.fontFallback = true;

            return Typeface.DEFAULT;

        }

        try {

            return Typeface.createFromAsset(
                    context.getAssets(),
                    path
            );

        }

        catch (Exception e) {

            return Typeface.DEFAULT;
        }
    }


    private String getFontPath(String family, int weight) {

        switch (family) {

            case "Poppins":

                switch (weight) {

                    case 300:
                        return "fonts/Poppins/poppins_light.ttf";

                    case 400:
                        return "fonts/Poppins/poppins_regular.ttf";

                    case 500:
                        return "fonts/Poppins/poppins_medium.ttf";

                    case 600:
                        return "fonts/Poppins/poppins_semibold.ttf";

                    case 700:
                        return "fonts/Poppins/poppins_bold.ttf";
                }

                break;


            case "Inter":

                switch (weight) {

                    case 300:
                        return "fonts/Inter/inter_light.ttf";

                    case 400:
                        return "fonts/Inter/inter_regular.ttf";

                    case 500:
                        return "fonts/Inter/inter_medium.ttf";

                    case 600:
                        return "fonts/Inter/inter_semibold.ttf";

                    case 700:
                        return "fonts/Inter/inter_bold.ttf";
                }

                break;


            case "Roboto":

                switch (weight) {

                    case 300:
                        return "fonts/Roboto/roboto_light.ttf";

                    case 400:
                        return "fonts/Roboto/roboto_regular.ttf";

                    case 500:
                        return "fonts/Roboto/roboto_medium.ttf";

                    case 600:
                        return "fonts/Roboto/roboto_semibold.ttf";

                    case 700:
                        return "fonts/Roboto/roboto_bold.ttf";
                }

                break;


            case "Montserrat":

                switch (weight) {

                    case 300:
                        return "fonts/Montserrat/montserrat_light.ttf";

                    case 400:
                        return "fonts/Montserrat/montserrat_regular.ttf";

                    case 500:
                        return "fonts/Montserrat/montserrat_medium.ttf";

                    case 600:
                        return "fonts/Montserrat/montserrat_semibold.ttf";

                    case 700:
                        return "fonts/Montserrat/montserrat_bold.ttf";
                }

                break;


            case "Lato":

                switch (weight) {

                    case 300:
                        return "fonts/Lato/lato_light.ttf";

                    case 400:
                        return "fonts/Lato/lato_regular.ttf";

                    case 500:
                        return "fonts/Lato/lato_medium.ttf";

                    case 600:
                        return "fonts/Lato/lato_semibold.ttf";

                    case 700:
                        return "fonts/Lato/lato_bold.ttf";
                }

                break;


            case "Nunito":

                switch (weight) {

                    case 300:
                        return "fonts/Nunito/nunito_light.ttf";

                    case 400:
                        return "fonts/Nunito/nunito_regular.ttf";

                    case 500:
                        return "fonts/Nunito/nunito_medium.ttf";

                    case 600:
                        return "fonts/Nunito/nunito_semibold.ttf";

                    case 700:
                        return "fonts/Nunito/nunito_bold.ttf";
                }

                break;


            case "Merriweather":

                switch (weight) {

                    case 300:
                        return "fonts/Merriweather/merriweather_24pt_light.ttf";

                    case 400:
                        return "fonts/Merriweather/merriweather_24pt_regular.ttf";

                    case 500:
                        return "fonts/Merriweather/merriweather_24pt_medium.ttf";

                    case 600:
                        return "fonts/Merriweather/merriweather_24pt_semibold.ttf";

                    case 700:
                        return "fonts/Merriweather/merriweather_24pt_bold.ttf";
                }

                break;


            case "Playfair Display":

                switch (weight) {

                    case 300:
                        return "fonts/Playfair Display/playfairdisplay_light.ttf";

                    case 400:
                        return "fonts/Playfair Display/playfairdisplay_regular.ttf";

                    case 500:
                        return "fonts/Playfair Display/playfairdisplay_medium.ttf";

                    case 600:
                        return "fonts/Playfair Display/playfairdisplay_semibold.ttf";

                    case 700:
                        return "fonts/Playfair Display/playfairdisplay_bold.ttf";
                }

                break;


            case "JetBrains Mono":

                switch (weight) {

                    case 300:
                        return "fonts/JetBrains Mono/jetbrainsmono_light.ttf";

                    case 400:
                        return "fonts/JetBrains Mono/jetbrainsmono_regular.ttf";

                    case 500:
                        return "fonts/JetBrains Mono/jetbrainsmono_medium.ttf";

                    case 600:
                        return "fonts/JetBrains Mono/jetbrainsmono_semibold.ttf";

                    case 700:
                        return "fonts/JetBrains Mono/jetbrainsmono_bold.ttf";
                }

                break;


            case "Roboto Mono":

                switch (weight) {

                    case 300:
                        return "fonts/Roboto Mono/robotomono_light.ttf";

                    case 400:
                        return "fonts/Roboto Mono/robotomono_regular.ttf";

                    case 500:
                        return "fonts/Roboto Mono/robotomono_medium.ttf";

                    case 600:
                        return "fonts/Roboto Mono/robotomono_semibold.ttf";

                    case 700:
                        return "fonts/Roboto Mono/robotomono_bold.ttf";
                }

                break;


            case "Inconsolata":

                switch (weight) {

                    case 300:
                        return "fonts/Inconsolata/inconsolata_light.ttf";

                    case 400:
                        return "fonts/Inconsolata/inconsolata_regular.ttf";

                    case 500:
                        return "fonts/Inconsolata/inconsolata_medium.ttf";

                    case 600:
                        return "fonts/Inconsolata/inconsolata_semibold.ttf";

                    case 700:
                        return "fonts/Inconsolata/inconsolata_bold.ttf";
                }

                break;


            case "Source Code Pro":

                switch (weight) {

                    case 300:
                        return "fonts/Source Code Pro/sourcecodepro_light.ttf";

                    case 400:
                        return "fonts/Source Code Pro/sourcecodepro_regular.ttf";

                    case 500:
                        return "fonts/Source Code Pro/sourcecodepro_medium.ttf";

                    case 600:
                        return "fonts/Source Code Pro/sourcecodepro_semibold.ttf";

                    case 700:
                        return "fonts/Source Code Pro/sourcecodepro_bold.ttf";
                }

                break;


            case "Pacifico":

                // Pacifico only has Regular
                if (weight == 400) {
                    return "fonts/Pacifico/pacifico_regular.ttf";
                }

                break;


            case "Asul":

                switch (weight) {

                    case 400:
                        return "fonts/Asul/asul_regular.ttf";

                    case 700:
                        return "fonts/Asul/asul_bold.ttf";
                }

                break;


            case "Bitcount Prop Double":

                switch (weight) {

                    case 300:
                        return "fonts/BitcountPropDouble/bitcountpropdouble_light.ttf";

                    case 400:
                        return "fonts/BitcountPropDouble/bitcountpropdouble_regular.ttf";

                    case 500:
                        return "fonts/BitcountPropDouble/bitcountpropdouble_medium.ttf";

                    case 600:
                        return "fonts/BitcountPropDouble/bitcountpropdouble_semibold.ttf";

                    case 700:
                        return "fonts/BitcountPropDouble/bitcountpropdouble_bold.ttf";
                }



                break;

            case "Sekuya" :

                if(weight ==  400){
                    return "fonts/Sekuya/sekuya_regular.ttf";
                }
                break;

            default:

                return "fonts/Poppins/poppins_regular.ttf";


        }

        return null;
    }


    private int normalizeWeight(int weight) {

        if (weight <= 300) {
            return 300;
        }

        if (weight <= 400) {
            return 400;
        }

        if (weight <= 500) {
            return 500;
        }

        if (weight <= 600) {
            return 600;
        }

        return 700;
    }


    public void clearCache() {
        fontCache.clear();
    }
}