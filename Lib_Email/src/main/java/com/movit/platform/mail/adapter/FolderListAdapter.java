package com.movit.platform.mail.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.EmailBoxListActivity;
import com.movit.platform.mail.activity.holder.FolderInfoHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jamison on 2016/7/20.
 */
public class FolderListAdapter extends BaseAdapter implements Filterable {

    private static class FolderViewHolder {

        public TextView txt_margin;
        public TextView folderName;
        public TextView newMessageCount;
        public ImageView img_box;
        public ImageView img_box_dot;
        public String rawFolderName;
    }

    public List<FolderInfoHolder> mFilteredFolders = new ArrayList<>();
    private LayoutInflater mInflater;
    private Filter mFilter = new FolderListFilter();
    private int diff;

    public FolderListAdapter(Activity context) {
        mInflater = context.getLayoutInflater();
        WindowManager manager = context.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        diff = outMetrics.widthPixels/12;
    }

    public Object getItem(long position) {
        return getItem((int) position);
    }

    public Object getItem(int position) {
        return mFilteredFolders.get(position);
    }


    public long getItemId(int position) {
        return mFilteredFolders.get(position).folder.getName().hashCode();
    }

    public int getCount() {
        return mFilteredFolders.size();
    }

    @Override
    public boolean isEnabled(int item) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    public int getFolderIndex(String folder) {
        FolderInfoHolder searchHolder = new FolderInfoHolder();
        searchHolder.name = folder;
        return mFilteredFolders.indexOf(searchHolder);
    }

    public FolderInfoHolder getFolder(String folder) {
        FolderInfoHolder holder = null;
        int index = getFolderIndex(folder);
        if (index >= 0) {
            holder = (FolderInfoHolder) getItem(index);
            if (holder != null) {
                return holder;
            }
        }
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position <= getCount()) {
            return getItemView(position, convertView, parent);
        } else {
            System.out.println("getView with illegal positon=" + position
                    + " called! count is only " + getCount());
            return null;
        }
    }

    public View getItemView(int itemPosition, View convertView, ViewGroup parent) {
        FolderInfoHolder folder = (FolderInfoHolder) getItem(itemPosition);
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.folder_list_item, parent, false);
        }
        FolderViewHolder holder = (FolderViewHolder) view.getTag();
        if (holder == null) {
            holder = new FolderViewHolder();
            holder.folderName = (TextView) view.findViewById(R.id.folder_name);
            holder.newMessageCount = (TextView) view.findViewById(R.id.email_number);
            holder.img_box = (ImageView) view.findViewById(R.id.img_box);
            holder.img_box_dot = (ImageView) view.findViewById(R.id.img_box_dot);
            holder.txt_margin = (TextView) view.findViewById(R.id.txt_margin);
            holder.rawFolderName = folder.name;
            view.setTag(holder);
        }
        ViewGroup.LayoutParams layoutParams = holder.txt_margin.getLayoutParams();
        layoutParams.width = (folder.hierarchy - 1) * diff;
        holder.txt_margin.setLayoutParams(layoutParams);
        holder.folderName.setText(folder.displayName);
        if (folder.img_id != 0) {
            holder.img_box.setImageResource(folder.img_id);
        }
        if (folder.unreadMessageCount == -1) {
            folder.unreadMessageCount = 0;
            try {
                folder.unreadMessageCount = folder.folder.getUnreadMessageCount();
            } catch (Exception e) {
                System.out.println("Unable to get unreadMessageCount for " + ":"
                        + folder.name);
            }
        }

        if (folder.unreadMessageCount > 0) {
            if(EmailBoxListActivity.folderUnReadNum.containsKey(folder.folder.getName())){
                holder.newMessageCount.setText(String.format("%d", EmailBoxListActivity.folderUnReadNum.get(folder.folder.getName())));
            }else{
                holder.newMessageCount.setText(String.format("%d", folder.unreadMessageCount));
            }

            holder.img_box_dot.setVisibility(View.VISIBLE);
        } else {
            holder.newMessageCount.setText("");
            holder.img_box_dot.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void setFilter(final Filter filter) {
        this.mFilter = filter;
    }

    public Filter getFilter() {
        return mFilter;
    }

    /**
     * Filter to search for occurences of the search-expression in any place of the
     * folder-name instead of doing jsut a prefix-search.
     *
     * @author Marcus@Wolschon.biz
     */
    public class FolderListFilter extends Filter {
        private CharSequence mSearchTerm;

        public CharSequence getSearchTerm() {
            return mSearchTerm;
        }

        /**
         * Do the actual search.
         * {@inheritDoc}
         *
         * @see #publishResults(CharSequence, FilterResults)
         */
        @Override
        protected FilterResults performFiltering(CharSequence searchTerm) {
            mSearchTerm = searchTerm;
            FilterResults results = new FilterResults();

            Locale locale = Locale.getDefault();
            if ((searchTerm == null) || (searchTerm.length() == 0)) {
                List<FolderInfoHolder> list = new ArrayList<>(mFilteredFolders);
                results.values = list;
                results.count = list.size();
            } else {
                final String searchTermString = searchTerm.toString().toLowerCase(locale);
                final String[] words = searchTermString.split(" ");
                final int wordCount = words.length;

                final List<FolderInfoHolder> newValues = new ArrayList<FolderInfoHolder>();

                for (final FolderInfoHolder value : mFilteredFolders) {
                    if (value.displayName == null) {
                        continue;
                    }
                    final String valueText = value.displayName.toLowerCase(locale);

                    for (int k = 0; k < wordCount; k++) {
                        if (valueText.contains(words[k])) {
                            newValues.add(value);
                            break;
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        /**
         * Publish the results to the user-interface.
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mFilteredFolders = Collections.unmodifiableList((ArrayList<FolderInfoHolder>) results.values);
            // Send notification that the data set changed now
            notifyDataSetChanged();
        }
    }
}