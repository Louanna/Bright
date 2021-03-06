package com.movit.platform.mail.mailstore.migrations;


import java.util.List;

import android.util.Log;

import com.fsck.k9.mail.Folder;
import com.movit.platform.mail.mailstore.LocalFolder;
import com.movit.platform.mail.mailstore.LocalStore;
import com.movit.platform.mail.preferences.Storage;
import com.movit.platform.mail.preferences.StorageEditor;


class MigrationTo42 {
    public static void from41MoveFolderPreferences(MigrationsHelper migrationsHelper) {
        try {
            LocalStore localStore = migrationsHelper.getLocalStore();
            Storage storage = migrationsHelper.getStorage();

            long startTime = System.currentTimeMillis();
            StorageEditor editor = storage.edit();

            List<? extends Folder > folders = localStore.getPersonalNamespaces(true);
            for (Folder folder : folders) {
                if (folder instanceof LocalFolder) {
                    LocalFolder lFolder = (LocalFolder)folder;
                    lFolder.save(editor);
                }
            }

            editor.commit();
            long endTime = System.currentTimeMillis();
            System.out.println("Putting folder preferences for " + folders.size() +
                    " folders back into Preferences took " + (endTime - startTime) + " ms");
        } catch (Exception e) {
            System.out.println("Could not replace Preferences in upgrade from DB_VERSION 41");
        }
    }
}
