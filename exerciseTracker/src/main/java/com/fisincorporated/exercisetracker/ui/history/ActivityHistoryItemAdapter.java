package com.fisincorporated.exercisetracker.ui.history;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.utils.RecyclerViewCursorAdapter;

import java.lang.ref.WeakReference;

public class ActivityHistoryItemAdapter extends RecyclerViewCursorAdapter<ActivityHistoryItemAdapter.ItemHolder> {

    private Context context;
    private WeakReference<IHistoryListCallbacks> callbacks;

    public ActivityHistoryItemAdapter(Context context, WeakReference<IHistoryListCallbacks> callbacks) {
        super(null);
        this.context = context;
        this.callbacks = callbacks;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_history_row, parent, false);
        return new ItemHolder(view, callbacks);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, Cursor cursor) {
        holder.bindItem(ActivityHistorySummary.getFromCursor(cursor));
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private WeakReference<IHistoryListCallbacks> viewCallbacks;
        private View summaryTextView;
        private TextView tvActivity;
        private TextView tvLocation;
        private TextView tvDate;
        private TextView tvDescription;
        private ImageButton imgbtnMap;
        private boolean ignoreClick = false;

        public ItemHolder(View itemView, WeakReference<IHistoryListCallbacks> callbacks) {
            super(itemView);
            viewCallbacks = callbacks;
            summaryTextView = itemView.findViewById(R.id.activity_row_summary_layout);
            tvActivity = (TextView) itemView.findViewById(R.id.activity_history_row_activity);
            tvLocation = (TextView) itemView.findViewById(R.id.activity_history_row_location);
            tvDate = (TextView) itemView.findViewById(R.id.activity_history_row_date);
            tvDescription = (TextView) itemView.findViewById(R.id.activity_history_row_description);
            imgbtnMap = (ImageButton) itemView.findViewById(R.id.activity_history_row_btnShowMap);
            summaryTextView.setOnLongClickListener(this);
            summaryTextView.setOnClickListener(this);

        }

        public void bindItem(final ActivityHistorySummary activityHistorySummary) {
            summaryTextView.setTag(activityHistorySummary);
            summaryTextView.setOnClickListener(this);
            tvActivity.setText(activityHistorySummary.getExercise());
            tvLocation.setText(activityHistorySummary.getLocation());
            tvDate.setText(activityHistorySummary.getActivityDate());
            tvDescription.setText(activityHistorySummary.getDescription());
            imgbtnMap.setTag(activityHistorySummary);
        }

        @Override
        public void onClick(View v) {
            if (ignoreClick) {
                // was longclick
                return;
            }
            switch (v.getId()) {
                case R.id.activity_row_summary_layout:
                    viewCallbacks.get().displayStats((ActivityHistorySummary) v.getTag(), getAdapterPosition());
                    break;
                case R.id.activity_history_row_btnShowMap:
                    viewCallbacks.get().displayMap((ActivityHistorySummary) v.getTag());
                    break;
            }
            ignoreClick=false;
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.activity_row_summary_layout:
                    // TODO animate in checkmark
                    // toggle to delete/undelete activity
                    ignoreClick = true;
                    break;
            }
            return false;
        }
    }


}
