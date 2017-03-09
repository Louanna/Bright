package com.movit.platform.mail.activity.holder;

import android.content.Context;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.R;
import com.fsck.k9.mail.Folder;
import com.movit.platform.mail.mailstore.LocalFolder;


public class FolderInfoHolder implements Comparable<FolderInfoHolder> {
    public static final int JUNK = 4, IN = 0, SENT = 1, DELETE = 2, DRAFT = 3;
    public String name;
    public String displayName;
    public long lastChecked;
    public int unreadMessageCount = -1;
    public int flaggedMessageCount = -1;
    public boolean loading;
    public String status;
    public Folder folder;
    public boolean pushActive;
    public boolean moreMessages;
    public int img_id = 0;
    public int hierarchy = 1;

    @Override
    public boolean equals(Object o) {
        return o instanceof FolderInfoHolder && name.equals(((FolderInfoHolder) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(FolderInfoHolder o) {
        String s1 = this.name;
        String s2 = o.name;

        int ret = s1.compareToIgnoreCase(s2);
        if (ret != 0) {
            return ret;
        } else {
            return s1.compareTo(s2);
        }

    }

    private String truncateStatus(String mess) {
        if (mess != null && mess.length() > 27) {
            mess = mess.substring(0, 27);
        }
        return mess;
    }

    // constructor for an empty object for comparisons
    public FolderInfoHolder() {
    }

    public FolderInfoHolder(Context context, LocalFolder folder, Account account) {
        if (context == null) {
            throw new IllegalArgumentException("null context given");
        }
        populate(context, folder, account);
    }

    public FolderInfoHolder(Context context, LocalFolder folder, Account account, int unreadCount) {
        populate(context, folder, account, unreadCount);
    }

    public void populate(Context context, LocalFolder folder, Account account, int unreadCount) {
        populate(context, folder, account);
        this.unreadMessageCount = unreadCount;
        folder.close();

    }


    public void populate(Context context, LocalFolder folder, Account account) {
        this.folder = folder;
        this.name = folder.getName();
        this.lastChecked = folder.getLastUpdate();
        this.status = truncateStatus(folder.getStatus());
        setDisplayName(context, name);
        setMoreMessagesFromFolder(folder);
    }


    /**
     * Returns the display name for a folder.
     * <p>
     * <p>
     * This will return localized strings for special folders like the Inbox or the Trash folder.
     * </p>
     *
     * @param context A {@link Context} instance that is used to get the string resources.
     * @param name    The name of the folder for which to return the display name.
     * @return The localized name for the provided folder if it's a special folder or the original
     * folder name if it's a non-special folder.
     */
    private void setDisplayName(Context context, String name) {
        String[] all = name.split("/");
        if (all[0].equals(Account.SPAM)) {
            img_id = R.drawable.icon_garbage;
        } else if (all[0].equals(Account.SENT)) {
            img_id = R.drawable.icon_sent;
        } else if (all[0].equals(Account.TRASH)) {
            img_id = R.drawable.icon_delete_email;
        } else if (all[0].equals(Account.DRAFTS)) {
            img_id = R.drawable.icon_drafts;
        } else if (all[0].equalsIgnoreCase(Account.INBOX)) {
            img_id = R.drawable.icon_inbox;
        }
        if (all[all.length - 1].equals(Account.SPAM)) {
            displayName = context.getString(R.string.junk);
        } else if (all[all.length - 1].equals(Account.SENT)) {
            displayName = context.getString(R.string.have_send);
        } else if (all[all.length - 1].equals(Account.TRASH)) {
            displayName = context.getString(R.string.have_delete);
        } else if (all[all.length - 1].equals(Account.DRAFTS)) {
            displayName = context.getString(R.string.draft);
        } else if (all[all.length - 1].equalsIgnoreCase(Account.INBOX)) {
            displayName = context.getString(R.string.special_mailbox_name_inbox);
        } else {
            displayName = all[all.length - 1];
        }
        hierarchy = all.length;
    }


    public void setMoreMessagesFromFolder(LocalFolder folder) {
        moreMessages = folder.hasMoreMessages();
    }
}
