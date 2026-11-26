package com.harsh.pdfstudio.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
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

public class History extends AppCompatActivity {

    View noRecentFilesImg;
    LinearLayout recentLayout;
    ArrayList<RecentFileItem> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_page);


        MaterialToolbar toolbar1=findViewById(R.id.toolbar_home_page);

//        LinearLayout clearAll=findViewById(R.id.clearAll);

        toolbar1.setNavigationOnClickListener(v -> {
            finish();
        });

        noRecentFilesImg=findViewById(R.id.no_recentFileImg);

        recentLayout=findViewById(R.id.history_layout);

        RecyclerView recyclerView=findViewById(R.id.recycler_view_recent_files);

        ArrayList<RecentFileItem> list=getRecentFiles(this);

        HistoryAdapter adapter=new HistoryAdapter(list,this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        adapter.setOnRemoveFile(() -> {
            isListEmpty();
        });

//        clearAll.setOnClickListener(v ->{
//            SharedPreferences prefs=getSharedPreferences("files",MODE_PRIVATE);
//            SharedPreferences.Editor editor=prefs.edit();
//            editor.putString("recent_files",null);
//            editor.apply();
//            list.clear();
//            isListEmpty();
//            adapter.notifyDataSetChanged();
//
//        });

    }


    private  ArrayList<RecentFileItem> getRecentFiles(Context context) {

        SharedPreferences prefs=context.getSharedPreferences("files",MODE_PRIVATE);

        Gson gson=new Gson();
        String json=prefs.getString("recent_files",null);
        Type type=new TypeToken<ArrayList<RecentFileItem>>(){}.getType();

        if(json!=null) {

             list=gson.fromJson(json,type);

           isListEmpty();
            return list;
        }

        noRecentFilesImg.setVisibility(View.VISIBLE);
        recentLayout.setVisibility(View.GONE);
        return new ArrayList<>();

    }


    public void isListEmpty(){

        if ( list.size()==0){
            noRecentFilesImg.setVisibility(View.VISIBLE);
            recentLayout.setVisibility(View.GONE);
        }


    }
}
