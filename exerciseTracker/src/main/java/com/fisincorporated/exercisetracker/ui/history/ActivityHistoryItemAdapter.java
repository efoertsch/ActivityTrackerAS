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

import java.lang.ref.WeakReference;

import static android.view.animation.Animation.AnimationListener;

public class ActivityHistoryItemAdapter extends RecyclerViewCursorAdapter<ActivityHistoryItemAdapter.ItemHolder> {

    private Context context;
    private WeakReference<IHistoryListCallbacks> callbacks;



    public ActivityHistoryItemAdapter(Context context, WeakReference<IHistoryListCallbacks> historyListCallbacks) {
        super(null);
        this.context = context;
        this.callbacks = historyListCallbacks;
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
        private ImageView imgvDeleteCheckmark;
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
            imgvDeleteCheckmark = (ImageView) itemView.findViewById(R.id.activity_history_check_to_delete);
            imgbtnMap.setOnClickListener(this);
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
            imgvDeleteCheckmark.setTag(activityHistorySummary);
            if (callbacks.get().isSetToDelete(activityHistorySummary)){
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
                        viewCallbacks.get().displayStats((ActivityHistorySummary) v.getTag(), getAdapterPosition());
                        break;
                    case R.id.activity_history_row_btnShowMap:
                        viewCallbacks.get().displayMap((ActivityHistorySummary) v.getTag());
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
                    if (!callbacks.get().isSetToDelete(activityHistorySummary)) {
                        callbacks.get().deleteThisActivity(activityHistorySummary);
                        crossFade(imgbtnMap, R.anim.rotate_fade_out, imgvDeleteCheckmark, R.anim.rotate_fade_in);
                    } else {
                        callbacks.get().removeFromDeleteList(activityHistorySummary);
                        crossFade(imgvDeleteCheckmark, R.anim.rotate_fade_out, imgbtnMap, R.anim.rotate_fade_in);
                    }
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


