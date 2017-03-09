package com.movit.platform.mail.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fsck.k9.mail.Address;
import com.movit.platform.framework.widget.CircleImageView;
import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.EmailListActivity;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.mailstore.DatabasePreviewType;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.util.FontSizes;
import com.movit.platform.mail.util.MessageHelper;

import java.util.List;

/**
 * Created by M on 2016/6/27.
 */
public class SearchResultAdapter extends BaseAdapter {

    class MessageViewHolder {

        public TextView preview;
        public TextView from;
        public TextView date;
        public CircleImageView img_head;
        public TextView subject2;
        public TextView threadCount;
        public CheckBox flagged;
        public CheckBox selected;
        public ImageView img_spot;
    }

    private Activity activity;
    private LayoutInflater mInflater;
    private List<Integer> mData;
    private FontSizes mFontSizes;
    private MessageHelper mMessageHelper;
    private MessageListAdapter adapter;
    private Drawable mAttachmentIcon;

    private String mFolderName;

    public SearchResultAdapter(Activity activity, List<Integer> mData, MessageListAdapter adapter) {
        this.activity = activity;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(activity);
        this.adapter = adapter;
        mFontSizes = MailboxController.getFontSizes();
        mMessageHelper = MessageHelper.getInstance(activity);
        mAttachmentIcon = activity.getResources().getDrawable(R.drawable.ic_email_attachment_small);
    }

    public void setFolderName(String mFolderName){
        this.mFolderName = mFolderName;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        MessageViewHolder holder = null;
        if (convertView == null) {
            holder = new MessageViewHolder();
            convertView = mInflater.inflate(R.layout.message_list_item, arg2, false);
            holder.from = (TextView) convertView.findViewById(R.id.subject);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.preview = (TextView) convertView.findViewById(R.id.preview);
            holder.subject2 = (TextView) convertView.findViewById(R.id.subject2);
            mFontSizes.setViewTextSize(holder.from, mFontSizes.getMessageListSender());
            mFontSizes.setViewTextSize(holder.date, mFontSizes.getMessageListDate());
            mFontSizes.setViewTextSize(holder.preview, mFontSizes.getMessageListPreview());
            holder.preview.setLines(MailboxController.EmailPreviewLine);
            holder.img_head = (CircleImageView) convertView.findViewById(R.id.img_head);
            holder.img_spot = (ImageView) convertView.findViewById(R.id.img_spot);
            //don't need now ,so set them GONE
            convertView.findViewById(R.id.selected_checkbox_wrapper).setVisibility(View.GONE);
            holder.flagged = (CheckBox) convertView.findViewById(R.id.flagged_bottom_right);
            holder.flagged.setVisibility(View.GONE);
            holder.selected = (CheckBox) convertView.findViewById(R.id.selected_checkbox);
            holder.selected.setVisibility(convertView.GONE);
            holder.threadCount = (TextView) convertView.findViewById(R.id.thread_count);
            mFontSizes.setViewTextSize(holder.threadCount, mFontSizes.getMessageListSubject());
            holder.threadCount.setVisibility(convertView.GONE);
            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }
        Cursor cursor = (Cursor)adapter.getItem(mData.get(position));
        Account account = Preferences.getCurrentAccount();
        String fromList = cursor.getString(EmailListActivity.SENDER_LIST_COLUMN);
        String toList = cursor.getString(EmailListActivity.TO_LIST_COLUMN);
        String ccList = cursor.getString(EmailListActivity.CC_LIST_COLUMN);
        Address[] fromAddrs = Address.unpack(fromList);
        Address[] toAddrs = Address.unpack(toList);
        CharSequence displayName = mMessageHelper.getDisplayName(account, fromAddrs, toAddrs,mFolderName);
        CharSequence displayDate = DateUtils.getRelativeTimeSpanString(activity, cursor.getLong(EmailListActivity.DATE_COLUMN));
        String subject = cursor.getString(EmailListActivity.SUBJECT_COLUMN);
        if (TextUtils.isEmpty(subject)) {
            subject = activity.getString(R.string.general_no_subject);
        }
        boolean read = (cursor.getInt(EmailListActivity.READ_COLUMN) == 1);
        boolean hasAttachments = (cursor.getInt(EmailListActivity.ATTACHMENT_COUNT_COLUMN) > 0);
        if (read) {
            holder.img_spot.setVisibility(View.GONE);
        } else {
            holder.img_spot.setVisibility(View.VISIBLE);
        }
        holder.subject2.setText(subject);
        holder.preview.setText(getPreview(cursor));
        holder.from.setCompoundDrawablesWithIntrinsicBounds(
                null, // left
                null, // top
                hasAttachments ? mAttachmentIcon : null, // right
                null); // bottom
        holder.from.setText(displayName);
        holder.date.setText(displayDate);
        return convertView;
    }

    private String getPreview(Cursor cursor) {
        String previewTypeString = cursor.getString(EmailListActivity.PREVIEW_TYPE_COLUMN);
        DatabasePreviewType previewType = DatabasePreviewType.fromDatabaseValue(previewTypeString);

        switch (previewType) {
            case NONE: {
                return "";
            }
            case ENCRYPTED: {
                return activity.getString(R.string.preview_encrypted);
            }
            case TEXT: {
                return cursor.getString(EmailListActivity.PREVIEW_COLUMN);
            }
        }

        throw new AssertionError("Unknown preview type: " + previewType);
    }
}
