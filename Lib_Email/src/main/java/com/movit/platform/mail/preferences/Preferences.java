
package com.movit.platform.mail.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.movit.platform.mail.bean.Account;

public class Preferences {

    private static Preferences preferences;

    public static synchronized Preferences getPreferences(Context context) {
        Context appContext = context.getApplicationContext();
        if (preferences == null) {
            preferences = new Preferences(appContext);
        }
        return preferences;
    }


    private Storage mStorage;
    private Map<String, Account> accounts = null;
    private List<Account> accountsInOrder = null;
    private static Account currentAccount;
    private Context mContext;

    private Preferences(Context context) {
        mStorage = Storage.getStorage(context);
        mContext = context;
        if (mStorage.isEmpty()) {
            System.out.println("Preferences storage is zero-size, importing from Android-style preferences");
            StorageEditor editor = mStorage.edit();
            editor.copy(context.getSharedPreferences("AndroidMail.Main", Context.MODE_PRIVATE));
            editor.commit();
        }
    }

    private void loadAccounts() {
        accounts = new HashMap<String, Account>();
        accountsInOrder = new LinkedList<Account>();
        String accountUuids = getStorage().getString("accountUuids", null);
        if ((accountUuids != null) && (accountUuids.length() != 0)) {
            String[] uuids = accountUuids.split(",");
            for (String uuid : uuids) {
                Account newAccount = new Account(this, uuid);
                accounts.put(uuid, newAccount);
                accountsInOrder.add(newAccount);
            }
        }
        if ((currentAccount != null) && currentAccount.getAccountNumber() != -1) {
            accounts.put(currentAccount.getUuid(), currentAccount);
            if (!accountsInOrder.contains(currentAccount)) {
                accountsInOrder.add(currentAccount);
            }
            currentAccount = null;
        }
    }

    /**
     * Returns an array of the accounts on the system. If no accounts are
     * registered the method returns an empty array.
     *
     * @return all accounts
     */
    public synchronized List<Account> getAccounts() {
        if (accounts == null) {
            loadAccounts();
        }
        return Collections.unmodifiableList(new ArrayList<Account>(accountsInOrder));
    }

    public static Account getCurrentAccount(){
        return currentAccount;
    }

    public Account CreateAccount(String uuid) {
        currentAccount = getAccount(uuid);
        if(currentAccount == null){
            currentAccount = newAccount(uuid);
        }
        return currentAccount;
    }

    private Account newAccount(String name) {
        currentAccount = new Account(mContext,name);
        accounts.put(currentAccount.getUuid(), currentAccount);
        accountsInOrder.add(currentAccount);
        return currentAccount;
    }

    public Account getAccount(String uuid){
        if (accounts == null) {
            loadAccounts();
        }
        Account account = accounts.get(uuid);
        return account;
    }

    public Storage getStorage() {
        return mStorage;
    }

    public static <T extends Enum<T>> T getEnumStringPref(Storage storage, String key, T defaultEnum) {
        String stringPref = storage.getString(key, null);

        if (stringPref == null) {
            return defaultEnum;
        } else {
            try {
                return Enum.valueOf(defaultEnum.getDeclaringClass(), stringPref);
            } catch (IllegalArgumentException ex) {
                System.out.println("Unable to convert preference key [" + key +
                        "] value [" + stringPref + "] to enum of type " + defaultEnum.getDeclaringClass());
                return defaultEnum;
            }
        }
    }
}
