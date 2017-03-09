package com.movit.platform.mail.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.compose.ActivityListener;
import com.movit.platform.mail.activity.compose.EmailComparator;
import com.movit.platform.mail.adapter.MessageListAdapter;
import com.movit.platform.mail.adapter.SearchResultAdapter;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.bean.Account.SortType;
import com.movit.platform.mail.bean.MessageReference;
import com.movit.platform.mail.cache.EmailProviderCache;
import com.movit.platform.mail.controller.MailboxEntry;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.controller.MessagingListener;
import com.movit.platform.mail.mailstore.LocalFolder;
import com.movit.platform.mail.mailstore.LocalMessage;
import com.movit.platform.mail.mailstore.LocalStore;
import com.movit.platform.mail.mailstore.StorageManager;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.provider.EmailProvider;
import com.movit.platform.mail.provider.EmailProvider.MessageColumns;
import com.movit.platform.mail.pulltorefresh.PullToRefreshBase;
import com.movit.platform.mail.pulltorefresh.PullToRefreshListView;
import com.movit.platform.mail.search.ConditionsTreeNode;
import com.movit.platform.mail.search.LocalSearch;
import com.movit.platform.mail.search.SearchSpecification;
import com.movit.platform.mail.search.SqlQueryBuilder;
import com.movit.platform.mail.util.MyEmailBroadcast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class EmailListActivity extends Activity implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private class LoadMoreMessagingListener extends MessagingListener {
        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            isLoadingMore = true;
            super.synchronizeMailboxStarted(account, folder);
        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder,
                                               int totalMessagesInMailbox, int numNewMessages) {
            isLoadingMore = false;
            super.synchronizeMailboxFinished(account, folder, totalMessagesInMailbox, numNewMessages);
        }

        @Override
        public void synchronizeMailboxFailed(Account account, String folder, String message) {
            canLoadMore = false;
            isLoadingMore = false;
            super.synchronizeMailboxFailed(account, folder, message);
        }
    }

    private class MessageListActivityListener extends ActivityListener {
        @Override
        public void remoteSearchFailed(String folder, final String err) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(EmailListActivity.this, R.string.remote_search_error,
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void remoteSearchStarted(String folder) {
            mHandler.progress(true);
            mHandler.updateFooter(mContext.getString(R.string.remote_search_sending_query));
        }

        @Override
        public void enableProgressIndicator(boolean enable) {
            mHandler.progress(enable);
        }

        @Override
        public void remoteSearchFinished(String folder, int numResults, int maxResults, List<Message> extraResults) {
            mHandler.progress(false);
            mHandler.remoteSearchFinished();
            mExtraSearchResults = extraResults;
            if (extraResults != null && extraResults.size() > 0) {
                mHandler.updateFooter(String.format(mContext.getString(R.string.load_more_messages_fmt), maxResults));
            } else {
                mHandler.updateFooter(null);
            }
            setProgress(Window.PROGRESS_END);
        }

        @Override
        public void remoteSearchServerQueryComplete(String folderName, int numResults, int maxResults) {
            mHandler.progress(true);
            if (maxResults != 0 && numResults > maxResults) {
                mHandler.updateFooter(mContext.getString(R.string.remote_search_downloading_limited,
                        maxResults, numResults));
            } else {
                mHandler.updateFooter(mContext.getString(R.string.remote_search_downloading, numResults));
            }
            setProgress(Window.PROGRESS_START);
        }

        @Override
        public void informUserOfStatus() {
            mHandler.refreshTitle();
        }

        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            if (updateForMe(account, folder)) {
                mHandler.progress(true);
                mHandler.folderLoading(folder, true);
            }
            super.synchronizeMailboxStarted(account, folder);
        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder,
                                               int totalMessagesInMailbox, int numNewMessages) {

            if (updateForMe(account, folder)) {
                mHandler.progress(false);
                mHandler.folderLoading(folder, false);
            }
            super.synchronizeMailboxFinished(account, folder, totalMessagesInMailbox, numNewMessages);
        }

        @Override
        public void synchronizeMailboxFailed(final Account account, String folder, String message) {
            if (updateForMe(account, folder)) {
                mHandler.progress(false);
                mHandler.folderLoading(folder, false);
            }
            Log.d("EmailListActivity", "synchronizeMailboxFailed: "+message);
            Log.d("EmailListActivity", "folder: "+folder);
//            if(message.contains("Authentication") && isShow){
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(EmailListActivity.this,EmailLoginActivity.class);
//                        intent.putExtra("emailAddress",account.getEmail());
//                        startActivityForResult(intent, MailboxEntry.CHECKLOGIN_REQUESTCODE);
//                        isShow = false;
//                    }
//                });
//            }
            super.synchronizeMailboxFailed(account, folder, message);
        }

        @Override
        public void folderStatusChanged(Account account, String folder, int unreadMessageCount) {
            if (mAccount.equals(account) && mFolderName.equals(folder)) {
                mUnreadMessageCount = unreadMessageCount;
            }
            super.folderStatusChanged(account, folder, unreadMessageCount);
        }

        private boolean updateForMe(Account account, String folder) {
            if (account == null || folder == null) {
                return false;
            }
            if (!mAccountUuid.equals(account.getUuid())) {
                return false;
            }
            List<String> folderNames = mSearch.getFolderNames();
            return (folderNames.isEmpty() || folderNames.contains(folder));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MailboxEntry.CHECKLOGIN_REQUESTCODE){
            checkMail();
        }
    }

    private static class MessageListHandler extends Handler {
        private static final int ACTION_FOLDER_LOADING = 1;
        private static final int ACTION_REFRESH_TITLE = 2;
        private static final int ACTION_PROGRESS = 3;
        private static final int ACTION_REMOTE_SEARCH_FINISHED = 4;
        private static final int ACTION_GO_BACK = 5;
        private static final int ACTION_RESTORE_LIST_POSITION = 6;

        private WeakReference<EmailListActivity> mActivity;

        public MessageListHandler(EmailListActivity activity) {
            mActivity = new WeakReference<EmailListActivity>(activity);
        }

        public void folderLoading(String folder, boolean loading) {
            android.os.Message msg = android.os.Message.obtain(this, ACTION_FOLDER_LOADING,
                    (loading) ? 1 : 0, 0, folder);
            sendMessage(msg);
        }

        public void refreshTitle() {
            android.os.Message msg = android.os.Message.obtain(this, ACTION_REFRESH_TITLE);
            sendMessage(msg);
        }

        public void progress(final boolean progress) {
            android.os.Message msg = android.os.Message.obtain(this, ACTION_PROGRESS,
                    (progress) ? 1 : 0, 0);
            sendMessage(msg);
        }

        public void remoteSearchFinished() {
            android.os.Message msg = android.os.Message.obtain(this, ACTION_REMOTE_SEARCH_FINISHED);
            sendMessage(msg);
        }

        public void updateFooter(final String message) {
            post(new Runnable() {
                @Override
                public void run() {
                    updateFooter(message);
                }
            });
        }

        public void restoreListPosition() {
            EmailListActivity activity = mActivity.get();
            if (activity != null) {
                android.os.Message msg = android.os.Message.obtain(this, ACTION_RESTORE_LIST_POSITION,
                        activity.mSavedListState);
                activity.mSavedListState = null;
                sendMessage(msg);
            }
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            EmailListActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case ACTION_REMOTE_SEARCH_FINISHED: {

                    break;
                }
                case ACTION_FOLDER_LOADING: {
                    String folder = (String) msg.obj;
                    boolean loading = (msg.arg1 == 1);
                    folderLoading(folder, loading);
                    break;
                }
                case ACTION_REFRESH_TITLE: {

                    break;
                }
                case ACTION_PROGRESS: {
                    boolean progress = (msg.arg1 == 1);
                    //activity.progress(progress);
                    break;
                }
                case ACTION_GO_BACK: {
                    activity.finish();
                    break;
                }
                case ACTION_RESTORE_LIST_POSITION: {
                    activity.mListView.onRestoreInstanceState((Parcelable) msg.obj);
                    break;
                }
            }
        }
    }

    private static final String[] THREADED_PROJECTION = {
            EmailProvider.MessageColumns.ID,
            EmailProvider.MessageColumns.UID,
            EmailProvider.MessageColumns.INTERNAL_DATE,
            EmailProvider.MessageColumns.SUBJECT,
            EmailProvider.MessageColumns.DATE,
            EmailProvider.MessageColumns.SENDER_LIST,
            EmailProvider.MessageColumns.TO_LIST,
            EmailProvider.MessageColumns.CC_LIST,
            EmailProvider.MessageColumns.READ,
            EmailProvider.MessageColumns.FLAGGED,
            EmailProvider.MessageColumns.ANSWERED,
            EmailProvider.MessageColumns.FORWARDED,
            EmailProvider.MessageColumns.ATTACHMENT_COUNT,
            EmailProvider.MessageColumns.FOLDER_ID,
            EmailProvider.MessageColumns.PREVIEW_TYPE,
            EmailProvider.MessageColumns.PREVIEW,
            EmailProvider.ThreadColumns.ROOT,
            EmailProvider.SpecialColumns.ACCOUNT_UUID,
            EmailProvider.SpecialColumns.FOLDER_NAME,
            EmailProvider.SpecialColumns.THREAD_COUNT,
    };

    static {
        // fill the mapping at class time loading
        final Map<SortType, Comparator<Cursor>> map =
                new EnumMap<SortType, Comparator<Cursor>>(SortType.class);
        map.put(SortType.SORT_ATTACHMENT, new EmailComparator.AttachmentComparator());
        map.put(SortType.SORT_DATE, new EmailComparator.DateComparator());
        map.put(SortType.SORT_ARRIVAL, new EmailComparator.ArrivalComparator());
        map.put(SortType.SORT_FLAGGED, new EmailComparator.FlaggedComparator());
        map.put(SortType.SORT_SUBJECT, new EmailComparator.SubjectComparator());
        map.put(SortType.SORT_SENDER, new EmailComparator.SenderComparator());
        map.put(SortType.SORT_UNREAD, new EmailComparator.UnreadComparator());

        // make it immutable to prevent accidental alteration (content is immutable already)
        SORT_COMPARATORS = Collections.unmodifiableMap(map);
    }

    public static String getSenderAddressFromCursor(Cursor cursor) {
        String fromList = cursor.getString(SENDER_LIST_COLUMN);
        Address[] fromAddrs = Address.unpack(fromList);
        return (fromAddrs.length > 0) ? fromAddrs[0].getAddress() : null;
    }

    public static final int ID_COLUMN = 0;
    public static final int UID_COLUMN = 1;
    public static final int INTERNAL_DATE_COLUMN = 2;
    public static final int SUBJECT_COLUMN = 3;
    public static final int DATE_COLUMN = 4;
    public static final int SENDER_LIST_COLUMN = 5;
    public static final int TO_LIST_COLUMN = 6;
    public static final int CC_LIST_COLUMN = 7;
    public static final int READ_COLUMN = 8;
    public static final int FLAGGED_COLUMN = 9;
    public static final int ANSWERED_COLUMN = 10;
    public static final int FORWARDED_COLUMN = 11;
    public static final int ATTACHMENT_COUNT_COLUMN = 12;
    public static final int FOLDER_ID_COLUMN = 13;
    public static final int PREVIEW_TYPE_COLUMN = 14;
    public static final int PREVIEW_COLUMN = 15;
    public static final int THREAD_ROOT_COLUMN = 16;
    public static final int ACCOUNT_UUID_COLUMN = 17;
    public static final int FOLDER_NAME_COLUMN = 18;
    public static final int THREAD_COUNT_COLUMN = 19;

    private static final String[] PROJECTION = Arrays.copyOf(THREADED_PROJECTION,
            THREAD_COUNT_COLUMN);
    private static final Map<SortType, Comparator<Cursor>> SORT_COMPARATORS;

    private EditText edit_search;
    private ListView mListView;
    private PullToRefreshListView mPullToRefreshView;
    private MessageListAdapter mAdapter;
    private ListView resultListView;
    private SearchResultAdapter resultAdapter;
    private ArrayList<Integer> resultPosition = new ArrayList<>();
    private Parcelable mSavedListState;

    private Account mAccount;
    private String mAccountUuid;

    private String mFolderName;
    private boolean mCursorValid;
    private boolean showHead;
    private boolean isShow = true;
    private boolean isLoading = false;
    private boolean canLoadMore = true;
    private boolean isLoadingMore = false;
    public List<Message> mExtraSearchResults;
    private LocalSearch mSearch = null;

    private MessageController mController;
    private MessageListHandler mHandler = new MessageListHandler(this);
    private LoadMoreMessagingListener loadMoreMessagingListener = new LoadMoreMessagingListener();
    private SortType mSortType = SortType.SORT_DATE;
    private boolean isFirst = false;
    private boolean mSortAscending = true;
    private boolean mSortDateAscending = false;
    private Context mContext;

    private final ActivityListener mListener = new MessageListActivityListener();
    private boolean mLoaderInitialized;
    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mCacheBroadcastReceiver;
    private MyBroadcastReceiver myBroadcastReceiver;
    private IntentFilter mCacheIntentFilter;

    private Set<Long> mSelected = new HashSet<Long>();

    private int mUnreadMessageCount = 0;
    private int mUniqueIdColumn;
    private int mSelectedCount = 0;
    private Future<?> mRemoteSearchFuture = null;

    private MessageReference mActiveMessage;
    /**
     * Relevant messages for the current context when we have to remember the chosen messages
     * between user interactions (e.g. selecting a folder for move operation).
     */
    private List<LocalMessage> mActiveMessages;
    // private ActionMode mActionMode;
    //private ActionModeCallback mActionModeCallback = new ActionModeCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirst = getIntent().getBooleanExtra("first", false);
        mContext = this.getApplicationContext();
        mController = MessageController.getInstance(this.getApplication());
        mAccount = Preferences.getCurrentAccount();
        mAccountUuid = mAccount.getUuid();
        mSearch = getIntent().getParcelableExtra("EXTRA_SEARCH");
        if (mSearch == null) {
            mSearch = new LocalSearch(mAccount.getAutoExpandFolderName());
            mSearch.addAllowedFolder(mAccount.getAutoExpandFolderName());
            mSearch.addAccountUuid(mAccount.getUuid());
        }
        mFolderName = mSearch.getFolderNames().get(0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EmailContentActivity.DELETE_ALL_MESSAGE);
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);
        setContentView(R.layout.activity_email_list);
        TextView common_top_title = (TextView) findViewById(R.id.common_top_title);
        if (mFolderName.equals(Account.INBOX)) {
            showHead = true;
            common_top_title.setText(R.string.inbox);
        } else {
            showHead = false;
            String title = getIntent().getStringExtra("title");
            if (title != null) {
                common_top_title.setText(title);
            }
        }

        TextView txt_top_left = (TextView) findViewById(R.id.txt_top_left);
        txt_top_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFirst) {
                    startActivity(new Intent(EmailListActivity.this, EmailBoxListActivity.class));
                }
                finish();
            }
        });
        TextView txt_top_right = (TextView) findViewById(R.id.txt_top_right);
        txt_top_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyEmailBroadcast.CLOSE_ALL);
                sendBroadcast(intent);
                finish();
            }
        });
        ImageView img_write_email = (ImageView) findViewById(R.id.img_write_email);
        img_write_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmailListActivity.this, SendMailActivity.class));
            }
        });
        TextView txt_add_more = (TextView) findViewById(R.id.text_add_more);
        txt_add_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalFolder currentLocalFolder = getFolder(mFolderName, mAccount);
                    if (currentLocalFolder != null) {
                        if (!currentLocalFolder.hasMoreMessages()) {
                            Toast.makeText(EmailListActivity.this, R.string.after_load_all, Toast.LENGTH_SHORT).show();
                        } else {
                            if (canLoadMore) {
                                Toast.makeText(EmailListActivity.this, R.string.loading_email, Toast.LENGTH_SHORT).show();
                                if (!isLoadingMore) {
                                    mController.loadMoreMessages(mAccount, mFolderName, loadMoreMessagingListener);
                                }
                            } else {
                                Toast.makeText(EmailListActivity.this, R.string.cannot_load_more, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        });
        ImageView img_search = (ImageView) findViewById(R.id.img_search);
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchResult();
            }
        });
        edit_search = (EditText) findViewById(R.id.edit_search);
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String ss = edit_search.getText().toString();
                if (ss.equals("")) {
                    resultListView.setVisibility(View.GONE);
                    mPullToRefreshView.setVisibility(View.VISIBLE);
                }
            }
        });
        edit_search.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String content = edit_search.getText().toString();
                    if (!TextUtils.isEmpty(content)) {
                        searchResult();
                    }
                    InputMethodManager inputManager = (InputMethodManager) EmailListActivity.this.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(
                            edit_search.getWindowToken(), 0);
                }
                return false;
            }
        });
        initializeSortSettings();
        createCacheBroadcastReceiver();
        initializeLoader();
        initializeMessageList();
        //fixme by zoro 一进入就同步邮件
        checkMail();
    }

    private void createCacheBroadcastReceiver() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mCacheBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAdapter.notifyDataSetChanged();
            }
        };
        mCacheIntentFilter = new IntentFilter(EmailProviderCache.ACTION_CACHE_UPDATED);
    }

    private void initializeSortSettings() {
        mSortType = mAccount.getSortType();
        mSortAscending = mAccount.isSortAscending(mSortType);
        mSortDateAscending = mAccount.isSortAscending(SortType.SORT_DATE);
    }

    private void initializeMessageList() {
        mPullToRefreshView = (PullToRefreshListView) findViewById(R.id.email_list);
        @SuppressLint("InflateParams")
        LayoutInflater inflater = LayoutInflater.from(this);
        View loadingView = inflater.inflate(R.layout.message_list_loading, null);
        mPullToRefreshView.setEmptyView(loadingView);
        mPullToRefreshView.setOnRefreshListener(
                new PullToRefreshBase.OnRefreshListener<ListView>() {
                    @Override
                    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                        isShow = true;
                        checkMail();
                    }
                });
        mListView = mPullToRefreshView.getRefreshableView();
        mAdapter = new MessageListAdapter(this, showHead);
        //mListView.addFooterView(getFooterView(mListView));
        mListView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mListView.setFastScrollEnabled(true);
        mListView.setScrollingCacheEnabled(false);
        mListView.setVerticalScrollBarEnabled(false);
        //updateFooterView();
        mAdapter.setFolderName(mFolderName);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        resultListView = (ListView) findViewById(R.id.result_list);
        resultAdapter = new SearchResultAdapter(this, resultPosition, mAdapter);
        resultAdapter.setFolderName(mFolderName);
        resultListView.setAdapter(resultAdapter);
        resultListView.setOnItemClickListener(new ResultItemClickListener());
    }

    private void setPullToRefreshEnabled(boolean enable) {
        mPullToRefreshView.setMode((enable) ?
                PullToRefreshBase.Mode.PULL_FROM_START : PullToRefreshBase.Mode.DISABLED);
    }

    private void checkMail() {
        if (!isLoading) {
            isLoading = true;
            mController.synchronizeMailbox(mAccount, mFolderName, mListener, null);
            mController.sendPendingMessages(mAccount, mListener);
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.onRefreshComplete();
                        isLoading = false;
                    }
                }, 4000);
            }
        }
    }

//    private FolderInfoHolder getFolderInfoHolder(String folderName, Account account) {
//        try {
//            LocalFolder localFolder = getFolder(folderName, account);
//            return new FolderInfoHolder(mContext, localFolder, account);
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    private View getFooterView(ViewGroup parent) {
//        if (mFooterView == null) {
//            mFooterView = LayoutInflater.from(this).inflate(R.layout.message_list_item_footer, parent, false);
//            FooterViewHolder holder = new FooterViewHolder();
//            holder.main = (TextView) mFooterView.findViewById(R.id.main_text);
//            mFooterView.setTag(holder);
//        }
//        return mFooterView;
//    }

    private void initializeLoader() {
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, this);
        mCursorValid = false;
        mLoaderInitialized = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLoaderInitialized) {
            restartLoader();
        } else {
            mLoaderInitialized = false;
        }
        mLocalBroadcastManager.registerReceiver(mCacheBroadcastReceiver, mCacheIntentFilter);
        mListener.onResume(this);
        mController.addListener(mListener);
        if (mAccount != null && mFolderName != null && !mSearch.isManualSearch()) {
            mController.getFolderUnreadMessageCount(mAccount, mFolderName, mListener);
        }
        StorageManager.getInstance(getApplication()).addListener(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mCacheBroadcastReceiver);
        mListener.onPause(this);
        mController.removeListener(mListener);
    }

    private void restartLoader() {
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(0, null, this);
        mCursorValid = false;
    }

    private LocalFolder getFolder(String folderName, Account account) throws MessagingException {
        LocalStore localStore = account.getLocalStore();
        LocalFolder localFolder = localStore.getFolder(folderName);
        localFolder.open(Folder.OPEN_MODE_RO);
        return localFolder;
    }

    private void openMessage(MessageReference messageReference) {
        String folderName = messageReference.getFolderName();
        if (folderName.equals(mAccount.getDraftsFolderName())) {
            toEdit(messageReference);
        } else {
            toContent(messageReference);
        }
    }

    private void openMessageAtPosition(int position) {
        // Scroll message into view if necessary
        int listViewPosition = adapterToListViewPosition(position);
        if (listViewPosition != AdapterView.INVALID_POSITION &&
                (listViewPosition < mListView.getFirstVisiblePosition() ||
                        listViewPosition > mListView.getLastVisiblePosition())) {
            mListView.setSelection(listViewPosition);
        }
        MessageReference ref = getReferenceForPosition(position);
        openMessage(ref);
    }

    private void toEdit(MessageReference messageReference) {
        Intent i = new Intent(this, SendMailActivity.class);
        i.putExtra(SendMailActivity.EXTRA_MESSAGE_REFERENCE, messageReference);
        i.setAction(SendMailActivity.ACTION_EDIT_DRAFT);
        this.startActivity(i);
    }

    private void toContent(MessageReference messageReference) {
        Intent i = new Intent(this, EmailContentActivity.class);
        i.putExtra("message", messageReference);
        i.putExtra("messageNumber", mAdapter.getCount());
        this.startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != AdapterView.INVALID_POSITION) {
            mListView.setSelection(position);
        }
        // For some reason the mListView.setSelection() above won't do anything when we call
        // onOpenMessage() (and consequently mAdapter.notifyDataSetChanged()) right away. So we
        // defer the call using MessageListHandler.
        openMessageAtPosition(listViewToAdapterPosition(position));
    }

    private int listViewToAdapterPosition(int position) {
        if (position > 0 && position <= mAdapter.getCount()) {
            return position - 1;
        }

        return AdapterView.INVALID_POSITION;
    }

    private int adapterToListViewPosition(int position) {
        if (position >= 0 && position < mAdapter.getCount()) {
            return position + 1;
        }

        return AdapterView.INVALID_POSITION;
    }


    private MessageReference getReferenceForPosition(int position) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        String accountUuid = cursor.getString(ACCOUNT_UUID_COLUMN);
        String folderName = cursor.getString(FOLDER_NAME_COLUMN);
        String messageUid = cursor.getString(UID_COLUMN);
        return new MessageReference(accountUuid, folderName, messageUid, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String threadId = getThreadId(mSearch);
        Uri uri;
        String[] projection;
        boolean needConditions;
        if (threadId != null) {
            uri = Uri.withAppendedPath(EmailProvider.CONTENT_URI, "account/" + mAccountUuid + "/thread/" + threadId);
            projection = PROJECTION;
            needConditions = false;
        } else {
            uri = Uri.withAppendedPath(EmailProvider.CONTENT_URI, "account/" + mAccountUuid + "/messages/threaded");
            projection = THREADED_PROJECTION;
            needConditions = true;
        }
        StringBuilder query = new StringBuilder();
        List<String> queryArgs = new ArrayList<>();
        if (needConditions) {
            boolean selectActive = mActiveMessage != null && mActiveMessage.getAccountUuid().equals(mAccountUuid);

            if (selectActive) {
                query.append("(" + EmailProvider.MessageColumns.UID + " = ? AND " + EmailProvider.SpecialColumns.FOLDER_NAME + " = ?) OR (");
                queryArgs.add(mActiveMessage.getUid());
                queryArgs.add(mActiveMessage.getFolderName());
            }
            SqlQueryBuilder.buildWhereClause(mAccount, mSearch.getConditions(), query, queryArgs);
            if (selectActive) {
                query.append(')');
            }
        }
        String selection = query.toString();
        String[] selectionArgs = queryArgs.toArray(new String[0]);
        String sortOrder = buildSortOrder();
        return new CursorLoader(this, uri, projection, selection, selectionArgs,
                sortOrder);
    }

    private String getThreadId(LocalSearch search) {
        for (ConditionsTreeNode node : search.getLeafSet()) {
            SearchSpecification.SearchCondition condition = node.mCondition;
            if (condition.field == SearchSpecification.SearchField.THREAD_ID) {
                return condition.value;
            }
        }

        return null;
    }

    private String buildSortOrder() {
        String sortColumn = EmailProvider.MessageColumns.ID;
        switch (mSortType) {
            case SORT_ARRIVAL: {
                sortColumn = MessageColumns.INTERNAL_DATE;
                break;
            }
            case SORT_ATTACHMENT: {
                sortColumn = "(" + MessageColumns.ATTACHMENT_COUNT + " < 1)";
                break;
            }
            case SORT_FLAGGED: {
                sortColumn = "(" + MessageColumns.FLAGGED + " != 1)";
                break;
            }
            case SORT_SENDER: {
                //FIXME
                sortColumn = MessageColumns.SENDER_LIST;
                break;
            }
            case SORT_SUBJECT: {
                sortColumn = MessageColumns.SUBJECT + " COLLATE NOCASE";
                break;
            }
            case SORT_UNREAD: {
                sortColumn = MessageColumns.READ;
                break;
            }
            case SORT_DATE:
            default: {
                sortColumn = MessageColumns.DATE;
            }
        }
        String sortDirection = (mSortAscending) ? " ASC" : " DESC";
        String secondarySort;
        if (mSortType == SortType.SORT_DATE || mSortType == SortType.SORT_ARRIVAL) {
            secondarySort = "";
        } else {
            secondarySort = MessageColumns.DATE + ((mSortDateAscending) ? " ASC, " : " DESC, ");
        }
        String sortOrder = sortColumn + sortDirection + ", " + secondarySort +
                MessageColumns.ID + " DESC";
        return sortOrder;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mPullToRefreshView != null) {
            mPullToRefreshView.onRefreshComplete();
            mPullToRefreshView.setEmptyView(null);
        }
        if (data.getCount() == 0) {
            mAdapter.swapCursor(data);
            return;
        }
        //mPullToRefreshView.setEmptyView(null);
        //setPullToRefreshEnabled(isPullToRefreshAllowed());
        mCursorValid = true;
        Cursor cursor = data;
        mUniqueIdColumn = ID_COLUMN;
//        if (mIsThreadDisplay) {
//            if (cursor.moveToFirst()) {
//                mTitle = cursor.getString(SUBJECT_COLUMN);
//                if (!TextUtils.isEmpty(mTitle)) {
//                    mTitle = Utility.stripSubject(mTitle);
//                }
//                if (TextUtils.isEmpty(mTitle)) {
//                    mTitle = getString(R.string.general_no_subject);
//                }
//                updateTitle();
//            } else {
//                //TODO: empty thread view -> return to full message list
//            }
//        }
        mAdapter.swapCursor(cursor);
        if (mCursorValid) {
            if (mSavedListState != null) {
                mHandler.restoreListPosition();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSelected.clear();
        mAdapter.swapCursor(null);
    }

    private void searchResult() {
        String s = edit_search.getText().toString().trim();
        if (mAdapter != null && !s.equals("")) {
            resultPosition.clear();
            resultAdapter.notifyDataSetChanged();
            mPullToRefreshView.setVisibility(View.GONE);
            resultListView.setVisibility(View.VISIBLE);
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Cursor cursor = (Cursor) mAdapter.getItem(i);
                String subject = cursor.getString(EmailListActivity.SUBJECT_COLUMN);
                String sender = cursor.getString(EmailListActivity.SENDER_LIST_COLUMN);
                String receiver = cursor.getString(EmailListActivity.TO_LIST_COLUMN);
                String cc = cursor.getString(EmailListActivity.CC_LIST_COLUMN);
                CharSequence displayDate = DateUtils.getRelativeTimeSpanString(EmailListActivity.this, cursor.getLong(EmailListActivity.DATE_COLUMN));
                String date = displayDate.toString();
                if (!TextUtils.isEmpty(s)) {
                    String s2 = s.toLowerCase();
                    if(!TextUtils.isEmpty(sender) && sender.toLowerCase().contains(s2)){
                        resultPosition.add(i);
                    }else if(!TextUtils.isEmpty(subject) && subject.toLowerCase().contains(s2)){
                        resultPosition.add(i);
                    }else if(!TextUtils.isEmpty(date) && date.toLowerCase().contains(s2)){
                        resultPosition.add(i);
                    }else if(!TextUtils.isEmpty(receiver) && receiver.toLowerCase().contains(s2)){
                        resultPosition.add(i);
                    }else if(!TextUtils.isEmpty(cc) && cc.toLowerCase().contains(s2)){
                        resultPosition.add(i);
                    }
//                    if (mFolderName.equals(Account.INBOX)) {
//                        if (sender.toLowerCase().contains(s2) ||
//                                subject.toLowerCase().contains(s2) ||
//                                date.toLowerCase().contains(s2)) {
//                            resultPosition.add(i);
//                        }
//                    } else {
//                        if ((!TextUtils.isEmpty(subject) && subject.toLowerCase().contains(s2)) ||
//                                (!TextUtils.isEmpty(receiver) && receiver.toLowerCase().contains(s2)) ||
//                                (!TextUtils.isEmpty(cc) && cc.toLowerCase().contains(s2))) {
//                            resultPosition.add(i);
//                        }
//                    }
                }
            }
            if (resultPosition.size() > 0) {
                resultAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isFirst) {
            startActivity(new Intent(EmailListActivity.this, EmailBoxListActivity.class));
        }
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    private class ResultItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int truePosition = resultPosition.get(position);
            MessageReference ref = getReferenceForPosition(truePosition);
            openMessage(ref);
            resultListView.setVisibility(View.GONE);
            mPullToRefreshView.setVisibility(View.VISIBLE);
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(EmailContentActivity.DELETE_ALL_MESSAGE)) {
                restartLoader();
            }
        }
    }
}
