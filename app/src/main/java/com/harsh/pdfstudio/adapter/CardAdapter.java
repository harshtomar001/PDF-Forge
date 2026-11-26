package com.harsh.pdfstudio.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.CardItem;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    List<CardItem> list;


    private static final int[] ICON_BG_COLORS = {
            Color.parseColor("#E6F7F5"), // Teal
            Color.parseColor("#F0EAFE"), // Purple
            Color.parseColor("#FFF7E6"), // Amber
            Color.parseColor("#EAF8EE"), // Green
            Color.parseColor("#EAF2FF"), // Blue
            Color.parseColor("#FDEAF4")  // Pink
    };

    public CardAdapter(List<CardItem> list){
        this.list=list;
    }


    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_item_file,parent,false);

        return new ViewHolder(v);



    }

    public void  onBindViewHolder(ViewHolder holder,int postion){

        CardItem item=list.get(postion);

        holder.image.setImageResource(item.getImage());
        holder.title.setText(item.getTitle());
        holder.text.setText(item.getText());

        holder.iconCard.setCardBackgroundColor(
                ICON_BG_COLORS[postion % ICON_BG_COLORS.length]);

        holder.itemView.setContentDescription(item.getTitle());

        holder.itemView.setOnClickListener(
                v -> {
                    Intent intent=new Intent(v.getContext(),item.getActivityClass());
                    v.getContext().startActivity(intent);
                }
        );




    }

    public int getItemCount() {
        return list.size();
    }










    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        CardView iconCard;

        TextView text;


        public  ViewHolder(View itemView) {
            super(itemView);

            image=itemView.findViewById(R.id.imageView_card_item);
            title=itemView.findViewById(R.id.textView_title_card_item);
            iconCard=itemView.findViewById(R.id.iconCard_item);
            text=itemView.findViewById(R.id.textView_text_card_item);

        }


    }
}