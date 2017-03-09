package com.movit.platform.mail.activity.compose;

import android.database.Cursor;

import com.movit.platform.mail.activity.EmailListActivity;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016/6/20.
 */
public class EmailComparator {

    public static class ReverseComparator<T> implements Comparator<T> {
        private Comparator<T> mDelegate;

        public ReverseComparator(final Comparator<T> delegate) {
            mDelegate = delegate;
        }

        @Override
        public int compare(final T object1, final T object2) {
            // arg1 & 2 are mixed up, this is done on purpose
            return mDelegate.compare(object2, object1);
        }
    }

    public static class ComparatorChain<T> implements Comparator<T> {
        private List<Comparator<T>> mChain;

        public ComparatorChain(final List<Comparator<T>> chain) {
            mChain = chain;
        }

        @Override
        public int compare(T object1, T object2) {
            int result = 0;
            for (final Comparator<T> comparator : mChain) {
                result = comparator.compare(object1, object2);
                if (result != 0) {
                    break;
                }
            }
            return result;
        }
    }

    public static class ReverseIdComparator implements Comparator<Cursor> {
        private int mIdColumn = -1;

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            if (mIdColumn == -1) {
                mIdColumn = cursor1.getColumnIndex("_id");
            }
            long o1Id = cursor1.getLong(mIdColumn);
            long o2Id = cursor2.getLong(mIdColumn);
            return (o1Id > o2Id) ? -1 : 1;
        }
    }

    public static class AttachmentComparator implements Comparator<Cursor> {

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            int o1HasAttachment = (cursor1.getInt(EmailListActivity.ATTACHMENT_COUNT_COLUMN) > 0) ? 0 : 1;
            int o2HasAttachment = (cursor2.getInt(EmailListActivity.ATTACHMENT_COUNT_COLUMN) > 0) ? 0 : 1;
            return o1HasAttachment - o2HasAttachment;
        }
    }

    public static class FlaggedComparator implements Comparator<Cursor> {

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            int o1IsFlagged = (cursor1.getInt(EmailListActivity.FLAGGED_COLUMN) == 1) ? 0 : 1;
            int o2IsFlagged = (cursor2.getInt(EmailListActivity.FLAGGED_COLUMN) == 1) ? 0 : 1;
            return o1IsFlagged - o2IsFlagged;
        }
    }

    public static class UnreadComparator implements Comparator<Cursor> {

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            int o1IsUnread = cursor1.getInt(EmailListActivity.READ_COLUMN);
            int o2IsUnread = cursor2.getInt(EmailListActivity.READ_COLUMN);
            return o1IsUnread - o2IsUnread;
        }
    }

    public static class DateComparator implements Comparator<Cursor> {

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            long o1Date = cursor1.getLong(EmailListActivity.DATE_COLUMN);
            long o2Date = cursor2.getLong(EmailListActivity.DATE_COLUMN);
            if (o1Date < o2Date) {
                return -1;
            } else if (o1Date == o2Date) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public static class ArrivalComparator implements Comparator<Cursor> {

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            long o1Date = cursor1.getLong(EmailListActivity.INTERNAL_DATE_COLUMN);
            long o2Date = cursor2.getLong(EmailListActivity.INTERNAL_DATE_COLUMN);
            if (o1Date == o2Date) {
                return 0;
            } else if (o1Date < o2Date) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static class SubjectComparator implements Comparator<Cursor> {

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            String subject1 = cursor1.getString(EmailListActivity.SUBJECT_COLUMN);
            String subject2 = cursor2.getString(EmailListActivity.SUBJECT_COLUMN);

            if (subject1 == null) {
                return (subject2 == null) ? 0 : -1;
            } else if (subject2 == null) {
                return 1;
            }

            return subject1.compareToIgnoreCase(subject2);
        }
    }

    public static class SenderComparator implements Comparator<Cursor> {

        @Override
        public int compare(Cursor cursor1, Cursor cursor2) {
            String sender1 = EmailListActivity.getSenderAddressFromCursor(cursor1);
            String sender2 = EmailListActivity.getSenderAddressFromCursor(cursor2);

            if (sender1 == null && sender2 == null) {
                return 0;
            } else if (sender1 == null) {
                return 1;
            } else if (sender2 == null) {
                return -1;
            } else {
                return sender1.compareToIgnoreCase(sender2);
            }
        }
    }
}
