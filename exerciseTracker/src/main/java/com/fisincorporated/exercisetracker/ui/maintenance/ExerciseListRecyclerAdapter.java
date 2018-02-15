package com.fisincorporated.exercisetracker.ui.maintenance;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.ui.utils.RecyclerViewCursorAdapter;
import com.jakewharton.rxrelay2.PublishRelay;


public class ExerciseListRecyclerAdapter extends RecyclerViewCursorAdapter<ExerciseListRecyclerAdapter.ItemHolder> {

    private Context context;
    private PublishRelay<Object> publishRelay;

    public ExerciseListRecyclerAdapter(Context context, PublishRelay<Object> publishRelay) {
        super(null);
        this.context = context;
        this.publishRelay = publishRelay;
    }

    @Override
    public ExerciseListRecyclerAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercise_recycler_row, parent, false);
        return new ExerciseListRecyclerAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, Cursor cursor) {
        holder.bindItem(cursor.getInt(cursor.getColumnIndex(TrackerDatabase.Exercise._ID)),
                cursor.getString(cursor.getColumnIndex(TrackerDatabase.Exercise.EXERCISE)));
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvExercise;

        public ItemHolder(View itemView) {
            super(itemView);
            tvExercise = (TextView) itemView.findViewById(R.id.exercise_recycler_row_exercise);

        }

        public void bindItem(int exerciseId, String exercise) {
            tvExercise.setTag(exerciseId);
            tvExercise.setOnClickListener(this);
            tvExercise.setText(exercise);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.exercise_recycler_row_exercise:
                    publishRelay.accept(new ExerciseSelectedMsg((Integer) v.getTag(), getAdapterPosition()));

                    break;
            }
        }
    }
}
