package ru.hh.school.stdlib;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tiana
 * Date: 12.01.12
 * Time: 22:30
 */
public class TimeoutTest extends BaseFunctionalTest {
    public static final int ASKING_PERIOD = 500;
    public static final int MAX_CONNECTED_TIME = 11000;

    @Test
    public void timeoutIndependence() throws InterruptedException {
        final boolean[] successStatus = new boolean[1];
        successStatus[0] = true;

        final Set<Thread> threads = new HashSet<Thread>();
        for (int i = 0; i < 5; ++i) {
            final Thread currentThread = new Thread() {
                public void run() {
                    try {
                        final long startTime = System.currentTimeMillis();

                        final Socket connectionSocket = connect();

                        while (!connectionSocket.isClosed()) {
                            Thread.sleep(ASKING_PERIOD);
                        }

                        final long workingTime = System.currentTimeMillis() - startTime;

                        if (workingTime > MAX_CONNECTED_TIME + ASKING_PERIOD) {
                            synchronized (successStatus) {
                                successStatus[0] = false;
                            }
                        }
                    } catch (final Throwable ex) {
                        ex.printStackTrace();
                        synchronized (successStatus) {
                            successStatus[0] = false;
                        }
                    }
                }
            };

            currentThread.start();
            threads.add(currentThread);
        }

        for (final Thread thread : threads) {
            thread.join();
        }

        Assert.assertTrue(successStatus[0]);
    }
}
