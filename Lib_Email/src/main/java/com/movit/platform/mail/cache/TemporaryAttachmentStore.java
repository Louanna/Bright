package com.movit.platform.mail.cache;


import java.io.File;
import java.io.IOException;

import android.content.Context;

import com.movit.platform.mail.util.FileHelper;


public class TemporaryAttachmentStore {
    private static String TEMPORARY_ATTACHMENT_DIRECTORY = "attachments";
    private static long MAX_FILE_AGE = 12 * 60 * 60 * 1000;   // 12h

    public static File getFile(Context context, String attachmentName) {
        File directory = getTemporaryAttachmentDirectory(context);
        String filename = FileHelper.sanitizeFilename(attachmentName);
        return new File(directory, filename);
    }

    public static File getFileForWriting(Context context, String attachmentName) throws IOException {
        File directory = createOrCleanAttachmentDirectory(context);
        String filename = FileHelper.sanitizeFilename(attachmentName);
        return new File(directory, filename);
    }

    private static File createOrCleanAttachmentDirectory(Context context) throws IOException {
        File directory = getTemporaryAttachmentDirectory(context);
        if (directory.exists()) {
            cleanOldFiles(directory);
        } else {
            if (!directory.mkdir()) {
                throw new IOException("Couldn't create temporary attachment store: " + directory.getAbsolutePath());
            }
        }
        return directory;
    }

    private static File getTemporaryAttachmentDirectory(Context context) {
        return new File(context.getExternalCacheDir(), TEMPORARY_ATTACHMENT_DIRECTORY);
    }

    private static void cleanOldFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        long cutOffTime = System.currentTimeMillis() - MAX_FILE_AGE;
        for (File file : files) {
            if (file.lastModified() < cutOffTime) {
                file.delete();
            }
        }
    }
}
