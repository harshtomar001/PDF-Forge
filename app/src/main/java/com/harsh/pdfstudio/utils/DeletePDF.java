package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.harsh.pdfstudio.model.RecentFileItem;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class DeletePDF {



    public static boolean Remove_PDF_From_Storage(Context context , int i) {

        SharedPreferences prefs = context.getSharedPreferences("files", context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        String json = prefs.getString("recent_files", null);

        Gson gson = new Gson();

        Type type=new TypeToken<ArrayList<RecentFileItem>>(){}.getType();

        ArrayList<RecentFileItem> list=gson.fromJson(json,type);

        if (list != null){
            list.remove(i);
            editor.putString("recent_files",gson.toJson(list));
            editor.apply();
            return  true;
        }
        return false;

    }

}
