package com.example.ridenow.ui.driver.requests;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ridenow.R;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestsAdapter extends RecyclerView.Adapter<DriverRequestsAdapter.VH> {

    public interface Listener { void onReview(DriverRequestsFragment.RequestItem item); }

    private final List<DriverRequestsFragment.RequestItem> all = new ArrayList<>();
    private final List<DriverRequestsFragment.RequestItem> shown = new ArrayList<>();
    private final Listener listener;

    public DriverRequestsAdapter(List<DriverRequestsFragment.RequestItem> items, Listener listener) {
        this.listener = listener;
        if (items != null) {
            all.addAll(items);
            shown.addAll(items);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DriverRequestsFragment.RequestItem it = shown.get(position);
        holder.tvName.setText(it.name);
        holder.tvMeta.setText("Request: " + it.id + " • " + it.submittedAt);
        holder.tvStatus.setText(it.status == null ? "" : it.status);

        // Color status text based on value
        if (it.status == null) holder.tvStatus.setTextColor(Color.DKGRAY);
        else switch (it.status.toLowerCase()) {
            case "approved":
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "pending":
                holder.tvStatus.setTextColor(Color.parseColor("#FFA000"));
                break;
            case "rejected":
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"));
                break;
            default:
                holder.tvStatus.setTextColor(Color.DKGRAY);
        }

        holder.btnReview.setOnClickListener(v -> listener.onReview(it));

        // Load avatar using Glide if URL present, otherwise show placeholder
        try {
            if (it.avatarUrl != null && !it.avatarUrl.isEmpty()) {
                Glide.with(holder.ivAvatar.getContext())
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person);
            }
        } catch (Exception e) {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() { return shown.size(); }

    public void setFilter(String status) {
        shown.clear();
        if (status == null || status.isEmpty()) shown.addAll(all);
        else {
            for (DriverRequestsFragment.RequestItem r : all) {
                if (status.equalsIgnoreCase(r.status)) shown.add(r);
            }
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvMeta;
        TextView tvStatus;
        Button btnReview;

        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvName = v.findViewById(R.id.tvName);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnReview = v.findViewById(R.id.btnReview);
        }
    }
}
