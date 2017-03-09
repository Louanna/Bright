package com.movit.platform.mail.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.mail.R;
import com.movit.platform.mail.bean.Recipient;


public class RecipientAdapter extends BaseAdapter implements Filterable {
    private final Context context;
    private List<Recipient> selectedRecipient = new ArrayList<>();
    private List<Recipient> allRecipient;
    private AQuery aq;
    private Filter filter;

    public RecipientAdapter(Context context, List<Recipient> allRecipient) {
        super();
        this.context = context;
        aq = new AQuery(this.context);
        this.allRecipient = allRecipient;
    }

    @Override
    public int getCount() {
        return selectedRecipient == null ? 0 : selectedRecipient.size();
    }

    @Override
    public Recipient getItem(int position) {
        return selectedRecipient == null ? null : selectedRecipient.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = newView(parent);
        }
        Recipient recipient = getItem(position);
        bindView(view, recipient);
        return view;
    }

    private View newView(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipient_select_item, parent, false);
        RecipientTokenHolder holder = new RecipientTokenHolder(view);
        view.setTag(holder);
        return view;
    }

    private void bindView(View view, Recipient userInfo) {
        RecipientTokenHolder holder = (RecipientTokenHolder) view.getTag();
        holder.name.setText(userInfo.getDisplayNameOrAddress());
        String avatar = userInfo.avatar;
        String gender = userInfo.gender;
        if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
            AQuery aQuery = aq.recycle(view);
            int picId = com.movit.platform.common.R.drawable.avatar_male;
            if (gender != null && "女".equals(gender)) {
                picId = com.movit.platform.common.R.drawable.avatar_female;
            }
            BitmapAjaxCallback callback = new BitmapAjaxCallback();
            callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                    .round(10).fallback(picId)
                    .url(CommConstants.URL_DOWN + avatar).memCache(true)
                    .fileCache(true).targetWidth(128);
            aQuery.id(holder.photo).image(callback);
        } else {
            holder.photo.setImageResource(R.drawable.avatar_email);
        }
    }


    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    if (allRecipient == null) {
                        return null;
                    }
                    String prefixString = constraint.toString().toLowerCase();
                    System.out.println("prefixString=" + prefixString);
                    FilterResults result = new FilterResults();
                    selectedRecipient.clear();
                    for (Recipient recipient : allRecipient) {
                        if (!TextUtils.isEmpty(recipient.getDisplayNameOrAddress())
                                && recipient.getDisplayNameOrAddress().toLowerCase().startsWith(prefixString)) {
                            selectedRecipient.add(recipient);
                        }
                    }
                    result.values = selectedRecipient;
                    result.count = selectedRecipient.size();
                    return result;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }


    private static class RecipientTokenHolder {
        public final ImageView photo;
        public final TextView name;

        public RecipientTokenHolder(View view) {
            photo = (ImageView) view.findViewById(R.id.img_recipient_head);
            name = (TextView) view.findViewById(R.id.txt_recipient_name);
        }
    }


}
