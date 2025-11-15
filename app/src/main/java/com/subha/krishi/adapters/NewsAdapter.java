package com.subha.krishi.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.subha.krishi.R;
import com.subha.krishi.model.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final Context context;
    private final List<NewsItem> newsList;

    public NewsAdapter(Context context, List<NewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        NewsItem item = newsList.get(position);
        holder.author.setText(item.getAuthor());
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        holder.publishedAt.setText(item.getPublishedAt());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_news)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (item.getUrl() != null && !item.getUrl().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }


    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView author, title, description, publishedAt;
        ImageView image;

        public NewsViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.tvSource);
            title = itemView.findViewById(R.id.tvTitle);
            description = itemView.findViewById(R.id.tvDescription);
            publishedAt = itemView.findViewById(R.id.tvDate);
            image = itemView.findViewById(R.id.ivNews);
        }
    }
}
