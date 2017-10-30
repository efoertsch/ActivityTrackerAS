package com.fisincorporated.exercisetracker.ui.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.exercisetracker.R;

import java.lang.ref.WeakReference;


public class ActivityHistoryFragment extends AbstractActivityHistoryFragment implements IHistoryListCallbacks {

    protected RecyclerView recyclerView;

    private ActivityHistoryItemAdapter activityHistoryItemAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        restoreSortFilter(getArguments());
        View view = inflater.inflate(R.layout.activity_history_list, container,
                false);
        recyclerView = (RecyclerView) view.findViewById(R.id.activity_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        // a bit of a hack. If you return from the delete history screen with no changes the recycler view is empty,
        // so forcing a redraw
        if (recyclerView != null && activityHistoryItemAdapter != null){
           recyclerView.setAdapter(activityHistoryItemAdapter);
        }
    }

    // LoaderCallBacks interface methods
    // Note this gets called before onResume
    // #2
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        if (activityHistoryItemAdapter == null) {
            activityHistoryItemAdapter = new ActivityHistoryItemAdapter(getContext(),new WeakReference<IHistoryListCallbacks>(this));
            recyclerView.setAdapter(activityHistoryItemAdapter);
        }
        activityHistoryItemAdapter.swapCursor(cursor);

    }

    // #3
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is reset, we need to clear out the
        //current cursor from the adapter.
        activityHistoryItemAdapter.swapCursor(null);
    }

}
