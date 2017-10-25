package com.fisincorporated.exercisetracker.ui.history;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private WeakReference<IHistoryDeleteCallbacks> viewCallbacks;
        private View view;
        private TextView tvActivity;
        private TextView tvLocation;
        private TextView tvDate;
        private TextView tvDescription;
        private CheckBox cbxDelete;

        public ItemHolder(View itemView, WeakReference<IHistoryDeleteCallbacks> callbacks) {
            super(itemView);
            viewCallbacks = callbacks;
            view = itemView.findViewById(R.id.activity_row_summary_layout);
            tvActivity = (TextView) itemView.findViewById(R.id.activity_history_row_activity);
            tvLocation = (TextView) itemView.findViewById(R.id.activity_history_row_location);
            tvDate = (TextView) itemView.findViewById(R.id.activity_history_row_date);
            tvDescription = (TextView) itemView.findViewById(R.id.activity_history_row_description);
            cbxDelete = (CheckBox) itemView.findViewById(R.id.activity_history_delete_delete_checkbox);
        }

        public void bindItem(final ActivityHistorySummary activityHistorySummary) {
            view.setTag(activityHistorySummary);
            view.setOnClickListener(this);
            tvActivity.setText(activityHistorySummary.getExercise());
            tvLocation.setText(activityHistorySummary.getLocation());
            tvDate.setText(activityHistorySummary.getActivityDate());
            tvDescription.setText(activityHistorySummary.getDescription());
            cbxDelete.setTag(activityHistorySummary);
            cbxDelete.setOnClickListener(this);
            cbxDelete.setChecked(callbacks.get().isSetToDelete(activityHistorySummary));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.activity_history_delete_delete_checkbox:
                    callbacks.get().deleteThisActivity(((ActivityHistorySummary) v.getTag()));
                    break;
                default:
                    callbacks.get().displayStats((ActivityHistorySummary) v.getTag(), getAdapterPosition());
                    break;
            }
        }

        ;
    }
}