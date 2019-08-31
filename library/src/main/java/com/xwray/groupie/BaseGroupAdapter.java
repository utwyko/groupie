package com.xwray.groupie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An adapter that holds a list of Groups.
 */
public class BaseGroupAdapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH> implements GroupDataObserver {

    private final List<Group> groups = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private int spanCount = 1;
    private Item lastItemForViewTypeLookup;

    private AsyncDiffUtil.Callback diffUtilCallbacks = new AsyncDiffUtil.Callback() {
        @Override
        public void onDispatchAsyncResult(@NonNull Collection<? extends Group> newGroups) {
            setNewGroups(newGroups);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            notifyItemRangeChanged(position, count, payload);
        }
    };

    private AsyncDiffUtil asyncDiffUtil = new AsyncDiffUtil(diffUtilCallbacks);

    private final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            try {
                return getItem(position).getSpanSize(spanCount, position);
            } catch (IndexOutOfBoundsException e) {
                // Bug in support lib?  TODO investigate further
                return spanCount;
            }
        }
    };

    @NonNull
    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return spanSizeLookup;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public int getSpanCount() {
        return spanCount;
    }

    /**
     * Updates the adapter with a new list that will be diffed on a background thread
     * and displayed once diff results are calculated.
     *
     * NOTE: This update method is NOT compatible with partial updates (change notifications
     * driven by individual groups and items).  If you update using this method, all partial
     * updates will no longer work and you must use this method to update exclusively.
     * <br/> <br/>
     * If you want to receive a callback once the update is complete call the
     * {@link #updateAsync(List, boolean, OnAsyncUpdateListener)} version
     *
     * This will default detectMoves to true.
     *
     * @param newGroups List of {@link Group}
     */
    @SuppressWarnings("unused")
    public void updateAsync(@NonNull final List<? extends Group> newGroups) {
        this.updateAsync(newGroups, true, null);
    }

    /**
     * Updates the adapter with a new list that will be diffed on a background thread
     * and displayed once diff results are calculated.
     *
     * NOTE: This update method is NOT compatible with partial updates (change notifications
     * driven by individual groups and items).  If you update using this method, all partial
     * updates will no longer work and you must use this method to update exclusively.
     * <br/> <br/>
     *
     * This will default detectMoves to true.
     *
     * @see #updateAsync(List, boolean, OnAsyncUpdateListener)
     * @param newGroups List of {@link Group}
     */
    @SuppressWarnings("unused")
    public void updateAsync(@NonNull final List<? extends Group> newGroups, @Nullable final OnAsyncUpdateListener onAsyncUpdateListener) {
        this.updateAsync(newGroups, true, onAsyncUpdateListener);
    }

    /**
     * Updates the adapter with a new list that will be diffed on a background thread
     * and displayed once diff results are calculated.
     *
     * NOTE: This update method is NOT compatible with partial updates (change notifications
     * driven by individual groups and items).  If you update using this method, all partial
     * updates will no longer work and you must use this method to update exclusively.
     *
     * @param newGroups List of {@link Group}
     * @param onAsyncUpdateListener Optional callback for when the async update is complete
     * @param detectMoves Boolean is passed to {@link DiffUtil#calculateDiff(DiffUtil.Callback, boolean)}. Set to true
     *                    if you want DiffUtil to detect moved items.
     */
    @SuppressWarnings("unused")
    public void updateAsync(@NonNull final List<? extends Group> newGroups, boolean detectMoves, @Nullable final OnAsyncUpdateListener onAsyncUpdateListener) {
        final List<Group> oldGroups = new ArrayList<>(groups);
        final int oldBodyItemCount = getItemCount(oldGroups);
        final int newBodyItemCount = getItemCount(newGroups);

        final DiffCallback diffUtilCallback = new DiffCallback(oldBodyItemCount, newBodyItemCount, oldGroups, newGroups);
        asyncDiffUtil.calculateDiff(newGroups, diffUtilCallback, onAsyncUpdateListener, detectMoves);
    }

    /**
     * Updates the adapter with a new list that will be diffed on the <em>main</em> thread
     * and displayed once diff results are calculated. Not recommended for huge lists.
     *
     * This will default detectMoves to true.
     *
     * @param newGroups List of {@link Group}
     */
    @SuppressWarnings("unused")
    public void update(@NonNull final Collection<? extends Group> newGroups) {
        update(newGroups, true);
    }

    /**
     * Updates the adapter with a new list that will be diffed on the <em>main</em> thread
     * and displayed once diff results are calculated. Not recommended for huge lists.
     * @param newGroups List of {@link Group}
     * @param detectMoves is passed to {@link DiffUtil#calculateDiff(DiffUtil.Callback, boolean)}. Set to false
     *                    if you don't want DiffUtil to detect moved items.
     */
    @SuppressWarnings("unused")
    public void update(@NonNull final Collection<? extends Group> newGroups, boolean detectMoves) {
        final List<Group> oldGroups = new ArrayList<>(groups);
        final int oldBodyItemCount = getItemCount(oldGroups);
        final int newBodyItemCount = getItemCount(newGroups);

        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DiffCallback(oldBodyItemCount, newBodyItemCount, oldGroups, newGroups),
                detectMoves
        );

        setNewGroups(newGroups);

        diffResult.dispatchUpdatesTo(diffUtilCallbacks);
    }

    /**
     * Optionally register an {@link OnItemClickListener} that listens to click at the root of
     * each Item where {@link Item#isClickable()} returns true
     *
     * @param onItemClickListener The click listener to set
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Optionally register an {@link OnItemLongClickListener} that listens to long click at the root of
     * each Item where {@link Item#isLongClickable()} returns true
     *
     * @param onItemLongClickListener The long click listener to set
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    @NonNull
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Item<VH> item = getItemForViewType(viewType);
        View itemView = inflater.inflate(item.getLayout(), parent, false);
        return item.createViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Never called (all binds go through the version with payload)
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        Item contentItem = getItem(position);
        contentItem.bind(holder, position, payloads, onItemClickListener, onItemLongClickListener);
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        Item contentItem = holder.getItem();
        contentItem.unbind(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull VH holder) {
        Item contentItem = holder.getItem();
        return contentItem.isRecyclable();
    }

    @Override
    public int getItemViewType(int position) {
        lastItemForViewTypeLookup = getItem(position);
        if (lastItemForViewTypeLookup == null)
            throw new RuntimeException("Invalid position " + position);
        return lastItemForViewTypeLookup.getViewType();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public @NonNull Item getItem(@NonNull VH holder) {
        return holder.getItem();
    }

    private static Item getItem(Collection<? extends Group> groups, int position) {
        int count = 0;
        for (Group group : groups) {
            if (position < count + group.getItemCount()) {
                return group.getItem(position - count);
            } else {
                count += group.getItemCount();
            }
        }
        throw new IndexOutOfBoundsException("Requested position " + position + "in group adapter " +
                "but there are only " + count + " items");
    }

    public @NonNull Item getItem(int position) {
        return getItem(groups, position);
    }

    public int getAdapterPosition(@NonNull Item contentItem) {
        int count = 0;
        for (Group group : groups) {
            int index = group.getPosition(contentItem);
            if (index >= 0) return index + count;
            count += group.getItemCount();
        }
        return -1;
    }

    /**
     * The position in the flat list of individual items at which the group starts
     *
     * @param group
     * @return
     */
    public int getAdapterPosition(@NonNull Group group) {
        int index = groups.indexOf(group);
        if (index == -1) return -1;
        int position = 0;
        for (int i = 0; i < index; i++) {
            position += groups.get(i).getItemCount();
        }
        return position;
    }

    /**
     * Returns the number of top-level groups present in the adapter.
     */
    public int getGroupCount() {
        return groups.size();
    }

    private static int getItemCount(Collection<? extends Group> groups) {
        int count = 0;
        for (Group group : groups) {
            count += group.getItemCount();
        }
        return count;
    }

    @Override
    public int getItemCount() {
        return getItemCount(groups);
    }

    public int getItemCount(int groupIndex) {
        if (groupIndex >= groups.size()) {
            throw new IndexOutOfBoundsException("Requested group index " + groupIndex + " but there are " + groups.size() + " groups");
        }
        return groups.get(groupIndex).getItemCount();
    }

    public void clear() {
        for (Group group : groups) {
            group.unregisterGroupDataObserver(this);
        }
        groups.clear();
        notifyDataSetChanged();
    }

    public void add(@NonNull Group group) {
        if (group == null) throw new RuntimeException("Group cannot be null");
        int itemCountBeforeGroup = getItemCount();
        group.registerGroupDataObserver(this);
        groups.add(group);
        notifyItemRangeInserted(itemCountBeforeGroup, group.getItemCount());
    }

    /**
     * Adds the contents of the list of groups, in order, to the end of the adapter contents.
     * All groups in the list must be non-null.
     *
     * @param groups
     */
    public void addAll(@NonNull Collection<? extends Group> groups) {
        if (groups.contains(null)) throw new RuntimeException("List of groups can't contain null!");
        int itemCountBeforeGroup = getItemCount();
        int additionalSize = 0;
        for (Group group : groups) {
            additionalSize += group.getItemCount();
            group.registerGroupDataObserver(this);
        }
        this.groups.addAll(groups);
        notifyItemRangeInserted(itemCountBeforeGroup, additionalSize);
    }

    public void remove(@NonNull Group group) {
        if (group == null) throw new RuntimeException("Group cannot be null");
        int position = groups.indexOf(group);
        remove(position, group);
    }

    public void removeAll(@NonNull Collection<? extends Group> groups) {
        for (Group group : groups) {
            remove(group);
        }
    }

    public void removeGroup(int index) {
        Group group = getGroup(index);
        remove(index, group);
    }

    private void remove(int position, @NonNull Group group) {
        int itemCountBeforeGroup = getItemCountBeforeGroup(position);
        group.unregisterGroupDataObserver(this);
        groups.remove(position);
        notifyItemRangeRemoved(itemCountBeforeGroup, group.getItemCount());
    }

    public void add(int index, @NonNull Group group) {
        if (group == null) throw new RuntimeException("Group cannot be null");
        group.registerGroupDataObserver(this);
        groups.add(index, group);
        int itemCountBeforeGroup = getItemCountBeforeGroup(index);
        notifyItemRangeInserted(itemCountBeforeGroup, group.getItemCount());
    }

    /**
     * Get group, given a raw position in the list.
     *
     * @param position
     * @return
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public Group getGroup(int position) {
        int previous = 0;
        int size;
        for (Group group : groups) {
            size = group.getItemCount();
            if (position - previous < size) return group;
            previous += group.getItemCount();
        }
        throw new IndexOutOfBoundsException("Requested position " + position + " in group adapter " +
                "but there are only " + previous + " items");
    }

    private int getItemCountBeforeGroup(int groupIndex) {
        int count = 0;
        for (Group group : groups.subList(0, groupIndex)) {
            count += group.getItemCount();
        }
        return count;
    }

    @NonNull
    public Group getGroup(Item contentItem) {
        for (Group group : groups) {
            if (group.getPosition(contentItem) >= 0) {
                return group;
            }
        }
        throw new IndexOutOfBoundsException("Item is not present in adapter or in any group");
    }

    private void setNewGroups(@NonNull Collection<? extends Group> newGroups) {
        for (Group group : groups) {
            group.unregisterGroupDataObserver(this);
        }

        groups.clear();
        groups.addAll(newGroups);

        for (Group group : newGroups) {
            group.registerGroupDataObserver(this);
        }
    }

    @Override
    public void onChanged(@NonNull Group group) {
        notifyItemRangeChanged(getAdapterPosition(group), group.getItemCount());
    }

    @Override
    public void onItemInserted(@NonNull Group group, int position) {
        notifyItemInserted(getAdapterPosition(group) + position);
    }

    @Override
    public void onItemChanged(@NonNull Group group, int position) {
        notifyItemChanged(getAdapterPosition(group) + position);
    }

    @Override
    public void onItemChanged(@NonNull Group group, int position, Object payload) {
        notifyItemChanged(getAdapterPosition(group) + position, payload);
    }

    @Override
    public void onItemRemoved(@NonNull Group group, int position) {
        notifyItemRemoved(getAdapterPosition(group) + position);
    }

    @Override
    public void onItemRangeChanged(@NonNull Group group, int positionStart, int itemCount) {
        notifyItemRangeChanged(getAdapterPosition(group) + positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(@NonNull Group group, int positionStart, int itemCount, Object payload) {
        notifyItemRangeChanged(getAdapterPosition(group) + positionStart, itemCount, payload);
    }

    @Override
    public void onItemRangeInserted(@NonNull Group group, int positionStart, int itemCount) {
        notifyItemRangeInserted(getAdapterPosition(group) + positionStart, itemCount);
    }

    @Override
    public void onItemRangeRemoved(@NonNull Group group, int positionStart, int itemCount) {
        notifyItemRangeRemoved(getAdapterPosition(group) + positionStart, itemCount);
    }

    @Override
    public void onItemMoved(@NonNull Group group, int fromPosition, int toPosition) {
        int groupAdapterPosition = getAdapterPosition(group);
        notifyItemMoved(groupAdapterPosition + fromPosition, groupAdapterPosition + toPosition);
    }

    /**
     * This idea was copied from Epoxy. :wave: Bright idea guys!
     * <p>
     * Find the model that has the given view type so we can create a viewholder for that model.
     * <p>
     * To make this efficient, we rely on the RecyclerView implementation detail that {@link
     * BaseGroupAdapter#getItemViewType(int)} is called immediately before {@link
     * BaseGroupAdapter#onCreateViewHolder(ViewGroup, int)}. We cache the last model
     * that had its view type looked up, and unless that implementation changes we expect to have a
     * very fast lookup for the correct model.
     * <p>
     * To be safe, we fallback to searching through all models for a view type match. This is slow and
     * shouldn't be needed, but is a guard against RecyclerView behavior changing.
     */
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

    private static class DiffCallback extends DiffUtil.Callback {
        private final int oldBodyItemCount;
        private final int newBodyItemCount;
        private final Collection<? extends Group> oldGroups;
        private final Collection<? extends Group> newGroups;

        DiffCallback(int oldBodyItemCount,
                     int newBodyItemCount,
                     Collection<? extends Group> oldGroups,
                     Collection<? extends Group> newGroups) {
            this.oldBodyItemCount = oldBodyItemCount;
            this.newBodyItemCount = newBodyItemCount;
            this.oldGroups = oldGroups;
            this.newGroups = newGroups;
        }

        @Override
        public int getOldListSize() {
            return oldBodyItemCount;
        }

        @Override
        public int getNewListSize() {
            return newBodyItemCount;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Item oldItem = getItem(oldGroups, oldItemPosition);
            Item newItem = getItem(newGroups, newItemPosition);
            return newItem.isSameAs(oldItem);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Item oldItem = getItem(oldGroups, oldItemPosition);
            Item newItem = getItem(newGroups, newItemPosition);
            return newItem.equals(oldItem);
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            Item oldItem = getItem(oldGroups, oldItemPosition);
            Item newItem = getItem(newGroups, newItemPosition);
            return oldItem.getChangePayload(newItem);
        }
    }
}
