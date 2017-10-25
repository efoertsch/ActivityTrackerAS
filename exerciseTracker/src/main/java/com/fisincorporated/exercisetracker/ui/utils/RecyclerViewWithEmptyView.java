package com.fisincorporated.exercisetracker.ui.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import org.jetbrains.annotations.NotNull;

//From https://gist.github.com/adelnizamutdinov/31c8f054d1af4588dc5c
// use  setEmptyView(@Nullable View emptyView) to display empty view
public class RecyclerViewWithEmptyView extends RecyclerView {
    @Nullable
    View emptyView;

    public RecyclerViewWithEmptyView(Context context) { super(context); }

    public RecyclerViewWithEmptyView(Context context, AttributeSet attrs) { super(context, attrs); }

    public RecyclerViewWithEmptyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void checkIfEmpty() {
        if (emptyView != null) {
            emptyView.setVisibility(getAdapter().getItemCount() > 0 ? GONE : VISIBLE);
        }
    }

    final @NotNull
    AdapterDataObserver observer = new AdapterDataObserver() {
        @Override public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }
    };

    @Override public void setAdapter(@Nullable Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
    }

    public void setEmptyView(@Nullable View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}
