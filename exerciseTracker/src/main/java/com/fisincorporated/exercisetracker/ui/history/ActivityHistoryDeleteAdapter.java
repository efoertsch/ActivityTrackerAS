package com.fisincorporated.exercisetracker.ui.history;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.utils.RecyclerViewCursorAdapter;

import java.lang.ref.WeakReference;


public class ActivityHistoryDeleteAdapter extends RecyclerViewCursorAdapter<ActivityHistoryDeleteAdapter.ItemHolder> {

    private Context context;
    private final WeakReference<IHistoryDeleteCallbacks> callbacks;

    public ActivityHistoryDeleteAdapter(Context context, WeakReference<IHistoryDeleteCallbacks> callbacks) {
        super(null);
        this.context = context;
        this.callbacks = callbacks;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_history_delete_row, parent, false);
        return new ItemHolder(view, callbacks);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, Cursor cursor) {
        holder.bindItem(ActivityHistorySummary.getFromCursor(cursor));
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private TextView tvActivity;
        private TextView tvLocation;
        private TextView tvDate;
        private TextView tvDescription;
        private ImageView ivCheckedToDelete;

        public ItemHolder(View itemView, WeakReference<IHistoryDeleteCallbacks> callbacks) {
            super(itemView);
            view = itemView.findViewById(R.id.activity_row_summary_layout);
            tvActivity = (TextView) itemView.findViewById(R.id.activity_history_row_activity);
            tvLocation = (TextView) itemView.findViewById(R.id.activity_history_row_location);
            tvDate = (TextView) itemView.findViewById(R.id.activity_history_row_date);
            tvDescription = (TextView) itemView.findViewById(R.id.activity_history_row_description);
            ivCheckedToDelete = (ImageView) itemView.findViewById(R.id.activity_history_check_to_delete);
        }

        public void bindItem(final ActivityHistorySummary activityHistorySummary) {
            view.setTag(activityHistorySummary);
            view.setOnClickListener(this);
            tvActivity.setText(activityHistorySummary.getExercise());
            tvLocation.setText(activityHistorySummary.getLocation());
            tvDate.setText(activityHistorySummary.getActivityDate());
            tvDescription.setText(activityHistorySummary.getDescription());
            ivCheckedToDelete.setTag(activityHistorySummary);
            ivCheckedToDelete.setOnLongClickListener(this);
            setCheckedToDelete(activityHistorySummary);
        }

        @Override
        public void onClick(View v) {
            callbacks.get().displayStats((ActivityHistorySummary) v.getTag(), getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            callbacks.get().deleteThisActivity((ActivityHistorySummary) v.getTag());
            setCheckedToDelete((ActivityHistorySummary) v.getTag());
            return true;
        }

        private void setCheckedToDelete(ActivityHistorySummary activityHistorySummary) {
            ivCheckedToDelete.setVisibility(callbacks.get().isSetToDelete(activityHistorySummary) ? View.VISIBLE : View.INVISIBLE);

        }
    }
}