package com.fisincorporated.exercisetracker.ui.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.ui.history.ActivityHistorySummary;
import com.fisincorporated.interfaces.IHandleSelectedAction;

public class ActivityHistoryItemAdapter extends RecyclerViewCursorAdapter<ActivityHistoryItemAdapter.ItemHolder> {

    private Context context;
    private IHandleSelectedAction iHandleSelectedAction;

    public ActivityHistoryItemAdapter(Context context, IHandleSelectedAction iHandleSelectedAction) {
        super(null);
        this.context = context;
        this.iHandleSelectedAction = iHandleSelectedAction;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_history_row, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, Cursor cursor) {
        holder.bindItem(ActivityHistorySummary.getFromCursor(cursor));
    }

    @Override
    public void swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private TextView tvActivity;
        private TextView tvLocation;
        private TextView tvDate;
        private TextView tvDescription;
        private ImageButton imgbtnMap;

        public ItemHolder(View itemView) {
            super(itemView);
            tvActivity = (TextView) itemView.findViewById(R.id.activity_history_row_activity);
            tvLocation = (TextView) itemView.findViewById(R.id.activity_history_row_location);
            tvDate = (TextView) itemView.findViewById(R.id.activity_history_row_date);
            tvDescription = (TextView) itemView.findViewById(R.id.activity_history_row_description);
            imgbtnMap = (ImageButton) itemView.findViewById(R.id.prior_activity_row_btnshowMap);
        }

        public void bindItem(final ActivityHistorySummary activityHistorySummary) {
            tvActivity.setText(activityHistorySummary.getExercise());
            tvLocation.setText(activityHistorySummary.getLocation());
            tvDate.setText(activityHistorySummary.getActivityDate());
            tvDescription.setText(activityHistorySummary.getDescription());
            imgbtnMap.setTag((Long) activityHistorySummary.getRow_id());
            imgbtnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.displaying_the_map_may_take_a_moment), Toast.LENGTH_SHORT)
                            .show();
                    Bundle args = new Bundle();
                    args.putLong(TrackerDatabase.LocationExercise._ID, (Long) v.getTag());
                    args.putString(GlobalValues.TITLE,
                            activityHistorySummary.getExercise() + "@" +
                                    activityHistorySummary.getLocation() + " " +
                                    activityHistorySummary.getActivityDate());
                    args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
                    args.putString(TrackerDatabase.LocationExercise.DESCRIPTION, activityHistorySummary.getDescription());
                    iHandleSelectedAction.onSelectedAction(args);
                }
            });
        }
    }
}
