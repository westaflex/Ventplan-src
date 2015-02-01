package com.ventplan.desktop.griffon;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AWTHelper {

    private static boolean started = false;

    public static int MILLISECONDS = 1000;

    private static AtomicInteger eventCount = new AtomicInteger(0);

    private static RateChecker rateChecker;

    private static Map<AWTRateCountObserver, Integer> rateCountBelowObserver = new ConcurrentHashMap<>();

    /**
     * AWTEventListener counting events.
     */
    private static class AWTEventCounter implements AWTEventListener {

        public void eventDispatched(AWTEvent event) {
            eventCount.incrementAndGet();
        }

    }

    /**
     * Check rate of AWT events per time period.
     */
    private static class RateChecker extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(MILLISECONDS);
                    int rateCount = eventCount.get();
                    //System.out.format("%d / %d msec%n", rateCount, MILLISECONDS);
                    for (AWTRateCountObserver observer : rateCountBelowObserver.keySet()) {
                        Integer rate = rateCountBelowObserver.get(observer);
                        if (rateCount <= rate) {
                            observer.rateEvent(rateCount);
                        }
                    }
                    eventCount.set(0);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    public static void registerDropsBelowObserver(AWTRateCountObserver awtRateCountObserver, int rate) {
        rateCountBelowObserver.put(awtRateCountObserver, rate);
    }

    public static void unregister(AWTRateCountObserver awtRateCountObserver) {
        rateCountBelowObserver.remove(awtRateCountObserver);
    }

    /**
     * Create and attach a listener that counts events.
     */
    public static void startAWTEventListener() {
        if (!started) {
            Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventCounter(), -1);
            rateChecker = new RateChecker();
            rateChecker.start();
        }
    }

    public static void stopAWTEventListener() {
        rateChecker.interrupt();
    }

}
