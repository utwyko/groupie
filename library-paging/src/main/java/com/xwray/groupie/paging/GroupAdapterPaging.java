package com.xwray.groupie.paging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

public class GroupAdapterPaging<VH extends ViewHolder> extends PagedListAdapter<Item, VH> {

    private Item lastItemForViewTypeLookup;

    public GroupAdapterPaging() {
        super(diffCallback);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Item<VH> item = getItemForViewType(viewType);
        View itemView = inflater.inflate(item.getLayout(), parent, false);
        return item.createViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

    }

    private Item<VH> getItemForViewType(int viewType) {
        if (lastItemForViewTypeLookup != null
                && lastItemForViewTypeLookup.getViewType() == viewType) {
            // We expect this to be a hit 100% of the time
            return lastItemForViewTypeLookup;
        }

        // To be extra safe in case RecyclerView implementation details change...
        for (int i = 0; i < getItemCount(); i++) {
            Item item = getItem(i);
            if (item.getViewType() == viewType) {
                return item;
            }
        }

        throw new IllegalStateException("Could not find model for view type: " + viewType);
    }

    private static DiffUtil.ItemCallback<Item> diffCallback = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return false;
        }
    };
}
