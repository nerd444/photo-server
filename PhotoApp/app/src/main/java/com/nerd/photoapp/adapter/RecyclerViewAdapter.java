package com.nerd.photoapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nerd.photoapp.R;
import com.nerd.photoapp.model.Item;
import com.nerd.photoapp.model.Post;
import com.nerd.photoapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    Context context;
    List<Item> postArrayList;

    public RecyclerViewAdapter(Context context, List<Item> postArrayList) {
        this.context = context;
        this.postArrayList = postArrayList;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Item post = postArrayList.get(position);

        holder.txt_content.setText(post.getContent());
        holder.txt_date.setText(post.getCreatedAt());

        String url = Utils.BASEURL + "/uploads/"+post.getPhotoUrl();
        Glide.with(context).load(url).into(holder.img_photo);

    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView img_photo;
        public TextView txt_content;
        public TextView txt_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img_photo = itemView.findViewById(R.id.img_photo);
            txt_content = itemView.findViewById(R.id.txt_content);
            txt_date = itemView.findViewById(R.id.txt_date);

        }
    }
}
