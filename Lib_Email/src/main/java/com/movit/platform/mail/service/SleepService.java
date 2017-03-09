package com.movit.platform.mail.service;

import android.content.Context;
import android.content.Intent;
import com.fsck.k9.mail.power.TracingPowerManager.TracingWakeLock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SleepService extends CoreService {

    private static String ALARM_FIRED = "com.fsck.k9.service.SleepService.ALARM_FIRED";
    private static String LATCH_ID = "com.fsck.k9.service.SleepService.LATCH_ID_EXTRA";


    private static ConcurrentHashMap<Integer, SleepDatum> sleepData = new ConcurrentHashMap<Integer, SleepDatum>();

    private static AtomicInteger latchId = new AtomicInteger();

    public static void sleep(Context context, long sleepTime, TracingWakeLock wakeLock, long wakeLockTimeout) {
        Integer id = latchId.getAndIncrement();
        SleepDatum sleepDatum = new SleepDatum();
        CountDownLatch latch = new CountDownLatch(1);
        sleepDatum.latch = latch;
        sleepDatum.reacquireLatch = new CountDownLatch(1);
        sleepData.put(id, sleepDatum);

        Intent i = new Intent(context, SleepService.class);
        i.putExtra(LATCH_ID, id);
        i.setAction(ALARM_FIRED + "." + id);
        long startTime = System.currentTimeMillis();
        long nextTime = startTime + sleepTime;
        BootReceiver.scheduleIntent(context, nextTime, i);
        if (wakeLock != null) {
            sleepDatum.wakeLock = wakeLock;
            sleepDatum.timeout = wakeLockTimeout;
            wakeLock.release();
        }
        try {
            boolean countedDown = latch.await(sleepTime, TimeUnit.MILLISECONDS);
            if (!countedDown) {
               System.out.println("SleepService latch timed out for id = " + id + ", thread " + Thread.currentThread().getName());
            }
        } catch (InterruptedException ie) {
        }
        SleepDatum releaseDatum = sleepData.remove(id);
        if (releaseDatum == null) {
            try {
                if (!sleepDatum.reacquireLatch.await(5000, TimeUnit.MILLISECONDS)) {
                    System.out.println("SleepService reacquireLatch timed out for id = " + id + ", thread " + Thread.currentThread().getName());
                }
            } catch (InterruptedException ie) {
                System.out.println("SleepService Interrupted while awaiting reacquireLatch");
            }
        } else {
            reacquireWakeLock(releaseDatum);
        }

        long endTime = System.currentTimeMillis();
        long actualSleep = endTime - startTime;

        if (actualSleep < sleepTime) {
           System.out.println("SleepService sleep time too short: requested was " + sleepTime + ", actual was " + actualSleep);
        }
    }

    private static void endSleep(Integer id) {
        if (id != -1) {
            SleepDatum sleepDatum = sleepData.remove(id);
            if (sleepDatum != null) {
                CountDownLatch latch = sleepDatum.latch;
                if (latch == null) {
                    System.out.println("SleepService No CountDownLatch available with id = " + id);
                } else {
                    latch.countDown();
                }
                reacquireWakeLock(sleepDatum);
                sleepDatum.reacquireLatch.countDown();
            }
        }
    }

    private static void reacquireWakeLock(SleepDatum sleepDatum) {
        TracingWakeLock wakeLock = sleepDatum.wakeLock;
        if (wakeLock != null) {
            synchronized (wakeLock) {
                long timeout = sleepDatum.timeout;
                wakeLock.acquire(timeout);
            }
        }
    }

    @Override
    public int startService(Intent intent, int startId) {
        try {
          if (intent.getAction().startsWith(ALARM_FIRED)) {
              Integer id = intent.getIntExtra(LATCH_ID, -1);
              endSleep(id);
          }
          return START_NOT_STICKY;
        }
        finally {
          stopSelf(startId);
        }
    }

    private static class SleepDatum {
        CountDownLatch latch;
        TracingWakeLock wakeLock;
        long timeout;
        CountDownLatch reacquireLatch;
    }

}
