package com.movit.platform.mail.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fsck.k9.mail.Message;
import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.compose.ActivityListener;
import com.movit.platform.mail.activity.holder.FolderInfoHolder;
import com.movit.platform.mail.adapter.FolderListAdapter;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.bean.AccountCreator.BaseAccount;
import com.movit.platform.mail.bean.AccountStats;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.controller.MailboxEntry;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.mailstore.LocalFolder;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.search.LocalSearch;
import com.movit.platform.mail.util.MyEmailBroadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EmailBoxListActivity extends Activity {

    public static Map<String,Integer> folderUnReadNum = new HashMap<>();

    private final BroadcastReceiver EmailBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(MyEmailBroadcast.CLOSE_ALL)) {
                finish();
            }
        }
    };

    private class FolderListHandler extends Handler {

        public void newFolders(final List<FolderInfoHolder> newFolders) {
            runOnUiThread(new Runnable() {
                public void run() {
                    MailboxEntry.saveNeedRefreshFolder(false);
                    mAdapter.mFilteredFolders.clear();
                    mAdapter.mFilteredFolders.addAll(newFolders);
                    mHandler.dataChanged();
                }
            });
        }

        public void folderLoading(final String folder, final boolean loading) {
            runOnUiThread(new Runnable() {
                public void run() {
                    FolderInfoHolder folderHolder = mAdapter.getFolder(folder);
                    if (folderHolder != null) {
                        folderHolder.loading = loading;
                    }

                }
            });
        }

        public void dataChanged() {
            runOnUiThread(new Runnable() {
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private ActivityListener mListener = new ActivityListener() {
        @Override
        public void informUserOfStatus() {
            mHandler.dataChanged();
        }

        @Override
        public void accountStatusChanged(BaseAccount account, AccountStats stats) {
            if (!account.equals(mAccount)) {
                return;
            }
            if (stats == null) {
                return;
            }
        }

        @Override
        public void listFoldersStarted(Account account) {
            super.listFoldersStarted(account);

        }

        @Override
        public void listFoldersFailed(Account account, String message) {
            super.listFoldersFailed(account, message);
        }

        @Override
        public void listFoldersFinished(Account account) {
            if (account.equals(mAccount)) {
                MessageController.getInstance(getApplication()).refreshListener(mListener);
                mHandler.dataChanged();
            }
            super.listFoldersFinished(account);
        }

        @Override
        public void listFolders(Account account, List<LocalFolder> folders) {
            if (account.equals(mAccount)) {
                List<FolderInfoHolder> ALLFolders = new LinkedList<>();
                List<FolderInfoHolder> INBOXFolders = new LinkedList<>();
                List<FolderInfoHolder> SENTFolders = new LinkedList<>();
                List<FolderInfoHolder> TRASHFolders = new LinkedList<>();
                List<FolderInfoHolder> DRAFTFolders = new LinkedList<>();
//                List<FolderInfoHolder> DRAFTEnglishFolders = new LinkedList<>();
                List<FolderInfoHolder> SPAMFolders = new LinkedList<>();
//                List<FolderInfoHolder> SPAMEnglishFolders = new LinkedList<>();
//                    Account.FolderMode aMode = account.getFolderDisplayMode();

                List<FolderInfoHolder> CUSTOMFolders = new LinkedList<>();

                for (LocalFolder folder : folders) {
//                        Folder.FolderClass fMode = folder.getDisplayClass();
//                        if ((aMode == FolderMode.FIRST_CLASS && fMode != Folder.FolderClass.FIRST_CLASS)
//                                || (aMode == FolderMode.FIRST_AND_SECOND_CLASS &&
//                                fMode != Folder.FolderClass.FIRST_CLASS &&
//                                fMode != Folder.FolderClass.SECOND_CLASS)
//                                || (aMode == FolderMode.NOT_SECOND_CLASS && fMode == Folder.FolderClass.SECOND_CLASS)) {
//                            continue;
//                        }
                    String name = folder.getName();
                    System.out.println("folderName=" + name);
                    if (name != null) {
                        String key = name.split("/")[0];
                        FolderInfoHolder holder = new FolderInfoHolder(EmailBoxListActivity.this, folder, mAccount, -1);
                        switch (key) {
                            case Account.INBOX:
                                INBOXFolders.remove(holder);
                                INBOXFolders.add(holder);
                                break;
                            case Account.DRAFTS:
                                DRAFTFolders.add(holder);
                                break;
//                            case Account.DRAFTSEnglish:
//                                DRAFTEnglishFolders.add(holder);
//                                break;
                            case Account.SENT:
                                SENTFolders.add(holder);
                                break;
                            case Account.TRASH:
                                TRASHFolders.add(holder);
                                break;
                            case Account.SPAM:
                                SPAMFolders.add(holder);
                                break;
//                            case Account.SPAMEnglish:
//                                SPAMEnglishFolders.add(holder);
//                                break;
                            case Account.OUTBOX:
                                break;
                            case MailboxController.ERROR_FOLDER_NAME:
                                break;
                            default:
                                CUSTOMFolders.add(holder);
                                break;
                        }
                    }
                }
                ALLFolders.addAll(INBOXFolders);
                ALLFolders.addAll(SENTFolders);
                ALLFolders.addAll(TRASHFolders);
//                if (DRAFTEnglishFolders.size() > 0) {
//                    ALLFolders.addAll(DRAFTEnglishFolders);
//                } else {
                    ALLFolders.addAll(DRAFTFolders);
//                }
//                if (SPAMEnglishFolders.size() > 0) {
//                    ALLFolders.addAll(SPAMEnglishFolders);
//                } else {
                    ALLFolders.addAll(SPAMFolders);
//                }
                ALLFolders.addAll(CUSTOMFolders);
                mHandler.newFolders(ALLFolders);
            }
            super.listFolders(account, folders);
        }

        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            super.synchronizeMailboxStarted(account, folder);
            if (account.equals(mAccount)) {
                mHandler.folderLoading(folder, true);
                mHandler.dataChanged();
            }

        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder, int totalMessagesInMailbox, int numNewMessages) {
            super.synchronizeMailboxFinished(account, folder, totalMessagesInMailbox, numNewMessages);
            if (account.equals(mAccount)) {
                mHandler.folderLoading(folder, false);
                refreshFolder(account, folder);

                //Anna:通知更新首页"收发邮件"右上角数字
                if(folderUnReadNum.containsKey(Account.INBOX)&&folderUnReadNum.get(Account.INBOX)>0){
                    Intent intent = new Intent();
                    intent.setAction(MyEmailBroadcast.UNREAD_EMAIL);
                    intent.putExtra("unread", folderUnReadNum.get(Account.INBOX));
                    sendBroadcast(intent);
                }
            }
        }

        private void refreshFolder(Account account, String folderName) {
            // There has to be a cheaper way to get at the localFolder object than this
            LocalFolder localFolder = null;
            try {
                if (account != null && folderName != null) {
                    if (!account.isAvailable(EmailBoxListActivity.this)) {
                        System.out.println("not refreshing folder of unavailable account");
                        return;
                    }
                    localFolder = account.getLocalStore().getFolder(folderName);
                    FolderInfoHolder folderHolder = mAdapter.getFolder(folderName);
                    if (folderHolder != null) {
                        folderHolder.populate(EmailBoxListActivity.this, localFolder, mAccount, -1);
                        folderHolder.flaggedMessageCount = -1;

                        mHandler.dataChanged();
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception while populating folder");
            } finally {
                if (localFolder != null) {
                    localFolder.close();
                }
            }

        }

        @Override
        public void synchronizeMailboxFailed(Account account, String folder, String message) {
            super.synchronizeMailboxFailed(account, folder, message);
            if (!account.equals(mAccount)) {
                return;
            }
            mHandler.folderLoading(folder, false);
            FolderInfoHolder holder = mAdapter.getFolder(folder);
            if (holder != null) {
                holder.lastChecked = 0;
            }
            mHandler.dataChanged();

        }

        @Override
        public void setPushActive(Account account, String folderName, boolean enabled) {
            if (!account.equals(mAccount)) {
                return;
            }
            FolderInfoHolder holder = mAdapter.getFolder(folderName);

            if (holder != null) {
                holder.pushActive = enabled;
                mHandler.dataChanged();
            }
        }


        @Override
        public void messageDeleted(Account account, String folder, Message message) {
            synchronizeMailboxRemovedMessage(account, folder, message);
        }

        @Override
        public void emptyTrashCompleted(Account account) {
            if (account.equals(mAccount)) {
                refreshFolder(account, mAccount.getTrashFolderName());
            }
        }

        @Override
        public void folderStatusChanged(Account account, String folderName, int unreadMessageCount) {
            if (account.equals(mAccount)) {
                refreshFolder(account, folderName);
                informUserOfStatus();
            }
        }

        @Override
        public void sendPendingMessagesCompleted(Account account) {
            super.sendPendingMessagesCompleted(account);
            if (account.equals(mAccount)) {
                refreshFolder(account, mAccount.getOutboxFolderName());
            }
        }

        @Override
        public void sendPendingMessagesStarted(Account account) {
            super.sendPendingMessagesStarted(account);

            if (account.equals(mAccount)) {
                mHandler.dataChanged();
            }
        }

        @Override
        public void sendPendingMessagesFailed(Account account) {
            super.sendPendingMessagesFailed(account);
            if (account.equals(mAccount)) {
                refreshFolder(account, mAccount.getOutboxFolderName());
            }
        }

        @Override
        public void accountSizeChanged(Account account, long oldSize, long newSize) {

        }
    };

    private FolderListHandler mHandler = new FolderListHandler();
    private FolderListAdapter mAdapter;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccount = Preferences.getCurrentAccount();
        setContentView(R.layout.activity_email_box_list);
        TextView common_top_title = (TextView) findViewById(R.id.common_top_title);
        common_top_title.setText(R.string.email);
        ImageView common_top_left = (ImageView) findViewById(R.id.common_top_left);
        common_top_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView list_box = (ListView) findViewById(R.id.list_box);
        mAdapter = new FolderListAdapter(this);
        restorePreviousData();
        list_box.setAdapter(mAdapter);
        list_box.setTextFilterEnabled(mAdapter.getFilter() != null);
        list_box.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onOpenFolder(((FolderInfoHolder) mAdapter.getItem(position)));
            }
        });
        IntentFilter intentFilter = new IntentFilter(MyEmailBroadcast.CLOSE_ALL);
        registerReceiver(EmailBroadcastReceiver, intentFilter);
    }

    @SuppressWarnings("unchecked")
    private void restorePreviousData() {
        final Object previousData = getLastNonConfigurationInstance();
        if (previousData != null) {
            mAdapter.mFilteredFolders = (ArrayList<FolderInfoHolder>) previousData;
        }
    }

    private void onOpenFolder(FolderInfoHolder folder) {
        String folderName = folder.name;
        LocalSearch search = new LocalSearch(folderName);
        search.addAccountUuid(mAccount.getUuid());
        search.addAllowedFolder(folderName);
        Intent intent = new Intent(EmailBoxListActivity.this, EmailListActivity.class);
        intent.putExtra("EXTRA_SEARCH", search);
        intent.putExtra("title", folder.displayName);
        startActivity(intent);
    }

    /**
     * On resume we refresh the folder list (in the background) and we refresh the
     * messages for any folder that is currently open. This guarantees that things
     * like unread message count and read status are updated.
     */
    @Override
    public void onResume() {
        super.onResume();
        MessageController.getInstance(getApplication()).addListener(mListener);
        MessageController.getInstance(getApplication()).getAccountStats(this, mAccount, mListener);

        //原来是这么写的
//        onRefresh(MailboxEntry.getNeedRefreshFolder());

        //Anna:更改为每次都刷新
        onRefresh(true);
        MessageController.getInstance(getApplication()).cancelNotificationsForAccount(mAccount);
        mListener.onResume(this);
    }

    private void onRefresh(final boolean forceRemote) {
        MessageController.getInstance(getApplication()).listFolders(mAccount, forceRemote, mListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        MessageController.getInstance(getApplication()).removeListener(mListener);
        mListener.onPause(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(EmailBroadcastReceiver);
    }

}