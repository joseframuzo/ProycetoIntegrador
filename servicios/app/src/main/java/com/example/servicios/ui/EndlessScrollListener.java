package com.example.servicios.ui;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private boolean loading = true;
    private int visibleThreshold = 5;
    private int previousTotal = 0;

    public void reset() {
        loading = true;
        previousTotal = 0;
    }

    public abstract void onLoadMore();

    @Override
    public void onScrolled(RecyclerView rv, int dx, int dy) {
        if (dy <= 0) return;
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) return;

        LinearLayoutManager llm = (LinearLayoutManager) lm;
        int totalItemCount = llm.getItemCount();
        int lastVisible = llm.findLastVisibleItemPosition();

        if (loading && totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
        }

        if (!loading && totalItemCount <= (lastVisible + visibleThreshold)) {
            loading = true;
            onLoadMore();
        }
    }
}
