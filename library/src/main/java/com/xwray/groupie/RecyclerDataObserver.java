package com.xwray.groupie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerDataObserver {
    boolean notifyChanged(@NonNull RecyclerView.ViewHolder holder, @Nullable Object payload);
}
