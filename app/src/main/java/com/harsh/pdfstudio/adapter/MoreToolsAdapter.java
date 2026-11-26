package com.harsh.pdfstudio.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.CardItem;
import com.harsh.pdfstudio.model.FileItem;

import java.util.ArrayList;

public class MoreToolsAdapter extends RecyclerView.Adapter<MoreToolsAdapter.ViewHolder> {
     final private ArrayList<CardItem> list;

     private  onClick_pdf_doc listener;

     public  void setOnClick_pdf_doc(onClick_pdf_doc listener){
         this.listener=listener;
     }

    public MoreToolsAdapter(ArrayList<CardItem> list){
        this.list=list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup group, int viewType){

        View v= LayoutInflater.from(group.getContext())
                .inflate(R.layout.card_item_tools_page,group,false);

        return  new ViewHolder(v);
    }

    @Override

    public void onBindViewHolder(ViewHolder holder, int position) {
        CardItem item = list.get(position);

        holder.image.setImageResource(item.getImage());
        holder.title.setText(item.getTitle());
        holder.text.setText(item.getText());

        holder.itemView.setOnClickListener( v -> {

            if (item.getNeedIntent()){
                listener.onClick(item.getType());
            }
            else{

                Intent intent =new Intent(v.getContext(),item.getActivityClass());
                v.getContext().startActivity(intent);

            }

        });


    }

    @Override
    public int getItemCount(){
        return list.size();
    }




        static  class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView  title;
        TextView text;


        public ViewHolder (View view){
            super(view);
            image=view.findViewById(R.id.image_card_item);
            title=view.findViewById(R.id.title_card_item);
            text=view.findViewById(R.id.text_card_item);


        }

    }

    public  interface onClick_pdf_doc{
        void onClick(String item);

    }


}
