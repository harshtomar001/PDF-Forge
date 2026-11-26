package com.harsh.pdfstudio.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;

import java.util.List;


public class FileSelector extends RecyclerView.Adapter<FileSelector.VH> {

    private final List<FileItem> list;

    public FileSelector(List<FileItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.compress_item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FileItem item = list.get(position);

        String pdf_name=item.getName();

        int totalPage=item.getPageCount();

        String fileSize= Byte_MB.convertByte(item.getFileSize());

        int nameLength=pdf_name.length();

        holder.fileName.setText(nameLength<22 ? pdf_name : pdf_name.substring(0,21) + "...");

        holder.pageCount.setText("Total Pages: "+totalPage);
        holder.fileSize.setText("Size: "+fileSize);


        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();

            Intent intent = new Intent(context, PdfViewerActivity.class);
            intent.putExtra("pdf_uri", item.getUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent);
        });


        holder.remove.setOnClickListener(v -> {
            list.clear();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView fileName;
        ImageView remove;

        TextView fileSize;

        TextView pageCount;


        VH(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.tvFileName);

            remove = itemView.findViewById(R.id.btnRemove);

            fileSize = itemView.findViewById(R.id.file_size);

            pageCount = itemView.findViewById(R.id.page_count);

        }
    }
}