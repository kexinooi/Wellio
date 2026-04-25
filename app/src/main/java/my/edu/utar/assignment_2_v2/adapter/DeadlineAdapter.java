package my.edu.utar.assignment_2_v2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import my.edu.utar.assignment_2_v2.R;
import my.edu.utar.assignment_2_v2.model.Deadline;

public class DeadlineAdapter extends RecyclerView.Adapter<DeadlineAdapter.DeadlineViewHolder> {

    private List<Deadline> deadlines;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final OnDeadlineClickListener listener;

    public interface OnDeadlineClickListener {
        void onDeadlineClick(Deadline deadline);
    }

    public DeadlineAdapter(List<Deadline> deadlines, OnDeadlineClickListener listener) {
        this.deadlines = deadlines;
        this.listener = listener;
    }

    public void updateList(List<Deadline> newList) {
        this.deadlines = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeadlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deadline, parent, false);
        return new DeadlineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineViewHolder holder, int position) {
        Deadline deadline = deadlines.get(position);
        holder.bind(deadline);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDeadlineClick(deadline);
        });
    }

    @Override
    public int getItemCount() {
        return deadlines == null ? 0 : deadlines.size();
    }

    class DeadlineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDue, tvType, tvDaysLeft;
        View statusDot;
        MaterialCardView statusCard;
        ImageView ivIcon;

        DeadlineViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_deadline_title);
            tvDue = itemView.findViewById(R.id.tv_deadline_due);
            tvType = itemView.findViewById(R.id.tv_deadline_type);
            tvDaysLeft = itemView.findViewById(R.id.tv_days_left);
            statusDot = itemView.findViewById(R.id.view_status_dot);
            statusCard = itemView.findViewById(R.id.card_deadline_status);
            ivIcon = itemView.findViewById(R.id.iv_deadline_icon);
        }

        void bind(Deadline deadline) {
            tvTitle.setText(deadline.getTitle());
            tvDue.setText("Due " + (deadline.getDueDate() != null ? dateFormat.format(deadline.getDueDate()) : "N/A"));
            tvType.setText(deadline.getType() != null ? deadline.getType().toUpperCase() : "ASSIGN");

            // Color type label by type
            int typeColor = getTypeColor(deadline.getType());
            tvType.setTextColor(typeColor);

            // Calculate days left
            long daysLeft = calculateDaysLeft(deadline.getDueDate());
            if (daysLeft < 0) {
                tvDaysLeft.setText("Overdue");
                statusCard.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                tvDaysLeft.setTextColor(Color.parseColor("#C62828"));
                statusDot.setBackgroundResource(R.drawable.circle_red);
            } else if (daysLeft == 0) {
                tvDaysLeft.setText("Today");
                statusCard.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                tvDaysLeft.setTextColor(Color.parseColor("#EF6C00"));
                statusDot.setBackgroundResource(R.drawable.circle_orange);
            } else {
                tvDaysLeft.setText(daysLeft + " day" + (daysLeft > 1 ? "s" : "") + " left");
                if (daysLeft <= 2) {
                    statusCard.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                    tvDaysLeft.setTextColor(Color.parseColor("#C62828"));
                    statusDot.setBackgroundResource(R.drawable.circle_red);
                } else if (daysLeft <= 5) {
                    statusCard.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                    tvDaysLeft.setTextColor(Color.parseColor("#EF6C00"));
                    statusDot.setBackgroundResource(R.drawable.circle_orange);
                } else {
                    statusCard.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                    tvDaysLeft.setTextColor(Color.parseColor("#2E7D32"));
                    statusDot.setBackgroundResource(R.drawable.dot_green);
                }
            }

            // Set icon based on type
            int iconRes = getIconForType(deadline.getType());
            ivIcon.setImageResource(iconRes);
        }

        private long calculateDaysLeft(Date dueDate) {
            if (dueDate == null) return 999;
            Date now = new Date();
            long diff = dueDate.getTime() - now.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }

        private int getTypeColor(String type) {
            if (type == null) return Color.parseColor("#5E72E4");
            switch (type.toLowerCase()) {
                case "quiz": return Color.parseColor("#F59E0B");
                case "test": return Color.parseColor("#8B5CF6");
                case "midterm": return Color.parseColor("#EC4899");
                default: return Color.parseColor("#5E72E4"); // assignment
            }
        }

        private int getIconForType(String type) {
            if (type == null) return R.drawable.ic_deadline_clipboard;
            switch (type.toLowerCase()) {
                case "quiz": return R.drawable.ic_type_quiz;
                case "test": return R.drawable.ic_clock;
                case "midterm": return R.drawable.ic_type_midterm;
                default: return R.drawable.ic_deadline_clipboard;
            }
        }
    }
}
