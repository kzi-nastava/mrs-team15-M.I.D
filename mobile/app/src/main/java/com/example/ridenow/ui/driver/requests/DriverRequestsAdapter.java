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
        // prefer current name/email when available
        String displayName = (it.curName != null && !it.curName.trim().isEmpty()) ? it.curName : (it.name != null ? it.name : "-");
        String displayEmail = (it.curEmail != null && !it.curEmail.trim().isEmpty()) ? it.curEmail : (it.email != null ? it.email : "-");
        holder.tvName.setText(displayName);
        // show current email and submitted date (remove request id)
        holder.tvMeta.setText(displayEmail + " • " + it.submittedAt);
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

        // Load avatar using Glide: prefer current avatar if available, otherwise proposed
        try {
            String imageUrl = null;
            if (it.curAvatarUrl != null && !it.curAvatarUrl.isEmpty()) imageUrl = it.curAvatarUrl;
            else if (it.avatarUrl != null && !it.avatarUrl.isEmpty()) imageUrl = it.avatarUrl;

            if (imageUrl != null) {
                String url = imageUrl;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = com.example.ridenow.util.ClientUtils.getServerBaseUrl() + (url.startsWith("/") ? "" : "/") + url;
                }
                Glide.with(holder.ivAvatar.getContext())
                        .load(url)
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

    public void setItems(List<DriverRequestsFragment.RequestItem> items) {
        all.clear();
        if (items != null) all.addAll(items);
        setFilter(null);
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
