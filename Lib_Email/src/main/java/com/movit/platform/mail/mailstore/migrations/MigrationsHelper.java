package com.movit.platform.mail.mailstore.migrations;


import java.util.List;

import android.content.Context;

import com.movit.platform.mail.bean.Account;
import com.fsck.k9.mail.Flag;
import com.movit.platform.mail.mailstore.LocalStore;
import com.movit.platform.mail.preferences.Storage;


/**
 * Helper to allow accessing classes and methods that aren't visible or accessible to the 'migrations' package
 */
public interface MigrationsHelper {
    LocalStore getLocalStore();
    Storage getStorage();
    Account getAccount();
    Context getContext();
    String serializeFlags(List<Flag> flags);
}
