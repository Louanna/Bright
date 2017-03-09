package com.movit.platform.mail.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fsck.k9.mail.Address;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.widget.CircleImageView;
import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.EmailListActivity;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.controller.MailboxEntry;
import com.movit.platform.mail.mailstore.DatabasePreviewType;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.util.FontSizes;
import com.movit.platform.mail.util.MessageHelper;

/**
 * Created by Jamison on 2016/6/19.
 */
public class MessageListAdapter extends CursorAdapter {

    class MessageViewHolder {

        public TextView preview;
        public TextView from;
        public TextView date;
        public int position = -1;
        public CircleImageView img_head;
        public TextView subject2;
        public TextView threadCount;
        public CheckBox flagged;
        public CheckBox selected;
        public ImageView img_spot;

    }

//    private class SelectCheckBoxListener implements View.OnClickListener {
//        private int position;
//
//        public void setPosition(int position) {
//            this.position = position;
//        }
//
//        @Override
//        public void onClick(View view) {
//            if (position != -1) {
//                emailListFragment.toggleMessageSelectWithAdapterPosition(position);
//            }
//        }
//    }
//
//    private class RightCheckBoxListener implements View.OnClickListener {
//
//        private int position;
//
//        public void setPosition(int position) {
//            this.position = position;
//        }
//
//        @Override
//        public void onClick(View view) {
//            if (position != -1) {
//                emailListFragment.toggleMessageFlagWithAdapterPosition(position);
//            }
//        }
//    }


    private Activity activity;
    private Drawable mAttachmentIcon;
    private Drawable mForwardedIcon;
    private Drawable mAnsweredIcon;
    private Drawable mForwardedAnsweredIcon;
    private LayoutInflater mInflater;
    private FontSizes mFontSizes;
    private MessageHelper mMessageHelper;
    private AQuery aq;
    private boolean showHead;
    private String mFolderName;

    public void setFolderName(String mFolderName){
        this.mFolderName = mFolderName;
    }

    public MessageListAdapter(Activity activity, boolean show) {
        super(activity, null, 0);
        this.activity = activity;
        aq = new AQuery(this.activity);
        mInflater = LayoutInflater.from(activity);
        showHead = show;
        mAttachmentIcon = activity.getResources().getDrawable(R.drawable.ic_email_attachment_small);
        mAnsweredIcon = activity.getResources().getDrawable(R.drawable.ic_email_answered_small);
        mForwardedIcon = activity.getResources().getDrawable(R.drawable.ic_email_forwarded_small);
        mForwardedAnsweredIcon = activity.getResources().getDrawable(R.drawable.ic_email_forwarded_answered_small);
        mFontSizes = MailboxController.getFontSizes();
        mMessageHelper = MessageHelper.getInstance(activity);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.message_list_item, parent, false);
        MessageViewHolder holder = new MessageViewHolder();
        holder.from = (TextView) view.findViewById(R.id.subject);
        holder.date = (TextView) view.findViewById(R.id.date);
        holder.preview = (TextView) view.findViewById(R.id.preview);
        holder.subject2 = (TextView) view.findViewById(R.id.subject2);
        mFontSizes.setViewTextSize(holder.from, mFontSizes.getMessageListSender());
        mFontSizes.setViewTextSize(holder.date, mFontSizes.getMessageListDate());
        mFontSizes.setViewTextSize(holder.preview, mFontSizes.getMessageListPreview());
        holder.preview.setLines(MailboxController.EmailPreviewLine);
        holder.img_head = (CircleImageView) view.findViewById(R.id.img_head);
        holder.img_spot = (ImageView) view.findViewById(R.id.img_spot);
        if (showHead) {
            holder.img_spot.setVisibility(View.VISIBLE);
            holder.img_head.setVisibility(View.VISIBLE);
        } else {
            holder.img_spot.setVisibility(View.GONE);
            holder.img_head.setVisibility(View.GONE);
        }
        //don't need now ,so set them GONE
        view.findViewById(R.id.selected_checkbox_wrapper).setVisibility(View.GONE);
        holder.flagged = (CheckBox) view.findViewById(R.id.flagged_bottom_right);
        holder.flagged.setVisibility(View.GONE);
        holder.selected = (CheckBox) view.findViewById(R.id.selected_checkbox);
        holder.selected.setVisibility(view.GONE);
        holder.threadCount = (TextView) view.findViewById(R.id.thread_count);
        mFontSizes.setViewTextSize(holder.threadCount, mFontSizes.getMessageListSubject()); // thread count is next to subject
        holder.threadCount.setVisibility(view.GONE);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Account account = Preferences.getCurrentAccount();
        String fromList = cursor.getString(EmailListActivity.SENDER_LIST_COLUMN);
        String toList = cursor.getString(EmailListActivity.TO_LIST_COLUMN);
        String ccList = cursor.getString(EmailListActivity.CC_LIST_COLUMN);
        Address[] fromAddrs = Address.unpack(fromList);
        UserInfo userInfo = null;
        if (showHead && fromAddrs != null && fromAddrs.length > 0) {
            String address = fromAddrs[0].getAddress();
//            if (address.equalsIgnoreCase(MFSPHelper.getString(CommConstants.EMAIL_ADDRESS))) {
//                userInfo = new UserInfo();
//                userInfo.setGender(MFSPHelper.getString(CommConstants.GENDER));
//                userInfo.setAvatar(MFSPHelper.getString(CommConstants.AVATAR));
//            } else {
//                userInfo = MailboxEntry.getUserInfo(address,activity);
//            }
            userInfo = MailboxEntry.getUserInfo(address,activity);
        }
        Address[] toAddrs = Address.unpack(toList);
        Address[] ccAddrs = Address.unpack(ccList);
        boolean fromMe = mMessageHelper.toMe(account, fromAddrs);
        boolean toMe = mMessageHelper.toMe(account, toAddrs);
        boolean ccMe = mMessageHelper.toMe(account, ccAddrs);
        CharSequence displayName = mMessageHelper.getDisplayName(account, fromAddrs, toAddrs,mFolderName);
//        CharSequence displayDate = DateUtils.getRelativeTimeSpanString(context, cursor.getLong(EmailListActivity.DATE_COLUMN));
        CharSequence displayDate = com.movit.platform.framework.utils.DateUtils.getDate(cursor.getLong(EmailListActivity.INTERNAL_DATE_COLUMN));

        String subject = cursor.getString(EmailListActivity.SUBJECT_COLUMN);
        if (TextUtils.isEmpty(subject)) {
            subject = activity.getString(R.string.general_no_subject);
        }
//        else  {
//            // If this is a thread, strip the RE/FW from the subject.  "Be like Outlook."
//            subject = Utility.stripSubject(subject);
//        }
        boolean read = (cursor.getInt(EmailListActivity.READ_COLUMN) == 1);
        boolean flagged = (cursor.getInt(EmailListActivity.FLAGGED_COLUMN) == 1);
        boolean answered = (cursor.getInt(EmailListActivity.ANSWERED_COLUMN) == 1);
        boolean forwarded = (cursor.getInt(EmailListActivity.FORWARDED_COLUMN) == 1);
        boolean hasAttachments = (cursor.getInt(EmailListActivity.ATTACHMENT_COUNT_COLUMN) > 0);
        MessageViewHolder holder = (MessageViewHolder) view.getTag();
        holder.position = cursor.getPosition();
//        long uniqueId = cursor.getLong(mUniqueIdColumn);
//        boolean selected = mSelected.contains(uniqueId);
//        if (mCheckboxes) {
//            holder.selected.setChecked(selected);
//        }
//        if (mStars) {
//            holder.flagged.setChecked(flagged);
//        }
//        // Background color
//        if (selected) {
//            int res;
//            if (selected) {
//                res = R.attr.messageListSelectedBackgroundColor;
//            } else if (read) {
//                res = R.attr.messageListReadItemBackgroundColor;
//            } else {
//                res = R.attr.messageListUnreadItemBackgroundColor;
//            }
//            TypedValue outValue = new TypedValue();
//            activity.getTheme().resolveAttribute(res, outValue, true);
//            view.setBackgroundColor(outValue.data);
//        } else {
//            view.setBackgroundColor(Color.TRANSPARENT);
//        }

//        Address counterpartyAddress = null;
//        if (fromMe) {
//            if (toAddrs.length > 0) {
//                counterpartyAddress = toAddrs[0];
//            } else if (ccAddrs.length > 0) {
//                counterpartyAddress = ccAddrs[0];
//            }
//        } else if (fromAddrs.length > 0) {
//            counterpartyAddress = fromAddrs[0];
//        }
//
//        if (holder.contactBadge != null) {
//            if (counterpartyAddress != null) {
//                Utility.setContactForBadge(holder.contactBadge, counterpartyAddress);
//                    /*
//                     * At least in Android 2.2 a different background + padding is used when no
//                     * email address is available. ListView reuses the views but QuickContactBadge
//                     * doesn't reset the padding, so we do it ourselves.
//                     */
//                holder.contactBadge.setPadding(0, 0, 0, 0);
//                mContactsPictureLoader.loadContactPicture(counterpartyAddress, holder.contactBadge);
//            } else {
//                holder.contactBadge.assignContactUri(null);
//                holder.contactBadge.setImageResource(R.drawable.ic_contact_picture);
//            }
//        }

//        if (mActiveMessage != null) {
//            String uid = cursor.getString(EmailListFragment.UID_COLUMN);
//            String folderName = cursor.getString(EmailListFragment.FOLDER_NAME_COLUMN);
//
//            if (account.getUuid().equals(mActiveMessage.getAccountUuid()) &&
//                    folderName.equals(mActiveMessage.getFolderName()) &&
//                    uid.equals(mActiveMessage.getUid())) {
//                int res = R.attr.messageListActiveItemBackgroundColor;
//
//                TypedValue outValue = new TypedValue();
//                activity.getTheme().resolveAttribute(res, outValue, true);
//                view.setBackgroundColor(outValue.data);
//            }
//        }
//        // Thread count
//        if (threadCount > 1) {
//            holder.threadCount.setText(String.format("%d", threadCount));
//            holder.threadCount.setVisibility(View.VISIBLE);
//        } else {
//            holder.threadCount.setVisibility(View.GONE);
//        }
        if (read) {
            holder.img_spot.setVisibility(View.GONE);
        } else {
            holder.img_spot.setVisibility(View.VISIBLE);
        }
        holder.subject2.setText(subject);
        String preview = getPreview(cursor);
        holder.preview.setText(preview);
        holder.from.setCompoundDrawablesWithIntrinsicBounds(
                null, // left
                null, // top
                hasAttachments ? mAttachmentIcon : null, // right
                null); // bottom
        holder.from.setText(displayName);
        holder.date.setText(displayDate);
        if (userInfo != null) {
            String avatar = userInfo.getAvatar();
            String gender = userInfo.getGender();
            int picId = com.movit.platform.common.R.drawable.avatar_male;
            if (!TextUtils.isEmpty(gender) && "女".equals(gender)) {
                picId = com.movit.platform.common.R.drawable.avatar_female;
            }
            if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
                AQuery aQuery = aq.recycle(view);
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(CommConstants.URL_DOWN + avatar).memCache(true)
                        .fileCache(true).targetWidth(128);
                aQuery.id(holder.img_head).image(callback);
            } else {
                holder.img_head.setImageResource(picId);
            }
        } else {
            holder.img_head.setImageResource(R.drawable.avatar_email);
        }
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
