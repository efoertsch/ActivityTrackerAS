package com.fisincorporated.exercisetracker.ui.history;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.AnimRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.utils.RecyclerViewCursorAdapter;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.HashSet;

import static android.view.animation.Animation.AnimationListener;

public class ActivityHistoryItemAdapter extends RecyclerViewCursorAdapter<ActivityHistoryItemAdapter.ItemHolder> {

    private Context context;
    private PublishRelay<Object> publishRelay;
    private HashSet<Long> deleteSet = new HashSet<>();

    ActivityHistoryItemAdapter(Context context, PublishRelay publishRelay) {
        super(null);
        this.context = context;
        this.publishRelay = publishRelay;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_history_row, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, Cursor cursor) {
        holder.bindItem(ActivityHistorySummary.getFromCursor(cursor));
        deleteSet.clear();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View summaryTextView;
        private TextView tvActivity;
        private TextView tvLocation;
        private TextView tvDate;
        private TextView tvDescription;
        private ImageButton imgbtnMap;
        private ImageView imgvDeleteCheckmark;
        private boolean ignoreClick = false;

        ItemHolder(View itemView) {
            super(itemView);
            summaryTextView = itemView.findViewById(R.id.activity_row_summary_layout);
            tvActivity = (TextView) itemView.findViewById(R.id.activity_history_row_activity);
            tvLocation = (TextView) itemView.findViewById(R.id.activity_history_row_location);
            tvDate = (TextView) itemView.findViewById(R.id.activity_history_row_date);
            tvDescription = (TextView) itemView.findViewById(R.id.activity_history_row_description);
            imgbtnMap = (ImageButton) itemView.findViewById(R.id.activity_history_row_btnShowMap);
            imgvDeleteCheckmark = (ImageView) itemView.findViewById(R.id.activity_history_check_to_delete);
            imgbtnMap.setOnClickListener(this);
            summaryTextView.setOnLongClickListener(this);
            summaryTextView.setOnClickListener(this);
        }

        void bindItem(final ActivityHistorySummary activityHistorySummary) {
            summaryTextView.setTag(activityHistorySummary);
            summaryTextView.setOnClickListener(this);
            tvActivity.setText(activityHistorySummary.getExercise());
            tvLocation.setText(activityHistorySummary.getLocation());
            tvDate.setText(activityHistorySummary.getActivityDate());
            tvDescription.setText(activityHistorySummary.getDescription());
            imgbtnMap.setTag(activityHistorySummary);
            imgvDeleteCheckmark.setTag(activityHistorySummary);
            if (deleteSet.contains(activityHistorySummary.getRow_id())){
                imgbtnMap.setVisibility(View.GONE);
                imgvDeleteCheckmark.setVisibility(View.VISIBLE);
            } else {
                imgbtnMap.setVisibility(View.VISIBLE);
                imgvDeleteCheckmark.setVisibility(View.GONE);
            }

        }

        @Override
        public void onClick(View v) {
            if (!ignoreClick) {
                switch (v.getId()) {
                    case R.id.activity_row_summary_layout:
                        ActivityHistorySummary activityHistorySummary = ((ActivityHistorySummary) v.getTag()).setPosition(getAdapterPosition());
                        publishRelay.accept(activityHistorySummary.setAction(ActivityHistorySummary.ACTIVITY_HISTORY_ACTION.DISPLAY_STATS));
                        break;
                    case R.id.activity_history_row_btnShowMap:
                        publishRelay.accept(((ActivityHistorySummary) v.getTag()).setAction(ActivityHistorySummary.ACTIVITY_HISTORY_ACTION.DISPLAY_MAP));
                        break;
                }
            }
            ignoreClick = false;
        }

        @Override
        public boolean onLongClick(View v) {
            if (v.getTag() == null || !(v.getTag() instanceof ActivityHistorySummary) ) {
                return false;
            }
            ActivityHistorySummary activityHistorySummary = (ActivityHistorySummary) v.getTag();

            switch (v.getId()) {
                case R.id.activity_row_summary_layout:
                    // toggle to delete/undelete activity
                    if (!deleteSet.contains(activityHistorySummary.getRow_id())) {
                        deleteSet.add(activityHistorySummary.getRow_id());
                        crossFade(imgbtnMap, R.anim.rotate_fade_out, imgvDeleteCheckmark, R.anim.rotate_fade_in);
                    } else {
                        deleteSet.remove(activityHistorySummary.getRow_id());
                        crossFade(imgvDeleteCheckmark, R.anim.rotate_fade_out, imgbtnMap, R.anim.rotate_fade_in);
                    }
                    publishRelay.accept(new DeleteHistoryListEvent(deleteSet));
                    // longclick also fires click event so bypass click event
                    ignoreClick = true;
                    break;
            }
            return false;
        }
    }

    private void crossFade(View outView, @AnimRes int outAnimation, View inView, @AnimRes int inAnimation) {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(outView.getContext(), outAnimation);
        fadeOutAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                outView.setAlpha(0.0f);
                outView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        outView.setAlpha(1.0f);
        outView.setAnimation(fadeOutAnimation);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(inView.getContext(), inAnimation);
        fadeInAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                inView.setAlpha(1.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        inView.setAnimation(fadeInAnimation);
        inView.setAlpha(0.0f);
        inView.setVisibility(View.VISIBLE);

        outView.startAnimation(fadeOutAnimation);
        inView.startAnimation(fadeInAnimation);


    }


}


