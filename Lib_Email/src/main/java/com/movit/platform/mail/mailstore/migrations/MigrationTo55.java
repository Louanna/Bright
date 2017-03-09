package com.movit.platform.mail.mailstore.migrations;


import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fsck.k9.mail.FetchProfile;
import com.fsck.k9.mail.MessagingException;
import com.movit.platform.mail.mailstore.LocalFolder;
import com.movit.platform.mail.mailstore.LocalMessage;
import com.movit.platform.mail.mailstore.LocalStore;
import com.movit.platform.mail.message.extractors.MessageFulltextCreator;


public class MigrationTo55 {
    public static void createFtsSearchTable(SQLiteDatabase db, MigrationsHelper migrationsHelper) {
        db.execSQL("CREATE VIRTUAL TABLE messages_fulltext USING fts4 (fulltext)");

        LocalStore localStore = migrationsHelper.getLocalStore();

        try {
            List<LocalFolder> folders = localStore.getPersonalNamespaces(true);
            ContentValues cv = new ContentValues();
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            for (LocalFolder folder : folders) {
                List<LocalMessage> localMessages = folder.getMessages(null, false);
                for (LocalMessage localMessage : localMessages) {
                    folder.fetch(Collections.singletonList(localMessage), fp, null);
                    MessageFulltextCreator fulltextCreator = localStore.getMessageFulltextCreator();
                    String fulltext = fulltextCreator.createFulltext(localMessage);
                    if (fulltext != null) {
                        System.out.println("fulltext for msg id " + localMessage.getId() + " is " + fulltext.length() + " chars long");
                        cv.clear();
                        cv.put("docid", localMessage.getId());
                        cv.put("fulltext", fulltext);
                        db.insert("messages_fulltext", null, cv);
                    } else {
                        System.out.println("no fulltext for msg id " + localMessage.getId() + " :(");
                    }
                }
            }
        } catch (MessagingException e) {
            System.out.println("error indexing fulltext - skipping rest, fts index is incomplete!");
        }
    }
}
