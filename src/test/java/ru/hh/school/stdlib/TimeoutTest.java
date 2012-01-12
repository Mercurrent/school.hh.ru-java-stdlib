package ru.hh.school.stdlib;

import org.junit.Assert;
import org.junit.Test;

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
    public static final int ACCEPTABLE_ADDITIONAL_WAITING = 1000;
    public static final int EXPECTED_TIMEOUT = 10000;

    @Test
    public void timeoutIndependence() throws InterruptedException {
        final Server target = getNewWorkingServer();
        
        final Set<Thread> threads = new HashSet<Thread>();
        for (int i = 0; i < 5; ++i) {
            final Thread currentThread = new Thread() {
                public void run() {
                    try {
                        final Object waitingSyncObject = new Object();
                        final Long[] workingTimeArr = new Long[1];
                        
                        final boolean[] finishStatus = new boolean[1];

                        workingTimeArr[0] = -1L;
                        
                        new Thread() {
                            public void run() {
                                final long startTime = System.currentTimeMillis();

                                final Socket connectionSocket = connect();

                                while (!connectionSocket.isClosed()) {
                                    try {
                                        Thread.sleep(ASKING_PERIOD);
                                    } catch (InterruptedException e) {
                                        Assert.fail();
                                    }

                                    synchronized (finishStatus) {
                                        if (finishStatus[0]) {
                                            break;
                                        }
                                    }
                                }

                                synchronized (workingTimeArr) {
                                    workingTimeArr[0] = System.currentTimeMillis() - startTime;
                                }

                                synchronized (waitingSyncObject) {
                                    waitingSyncObject.notify();
                                }
                            }
                        }.start();

                        //noinspection SynchronizationOnLocalVariableOrMethodParameter
                        synchronized (waitingSyncObject) {
                            // Upper bound of time.
                            waitingSyncObject.wait(EXPECTED_TIMEOUT + ACCEPTABLE_ADDITIONAL_WAITING + 2 * ASKING_PERIOD);
                        }

                        final long workingTime;
                        //noinspection SynchronizationOnLocalVariableOrMethodParameter
                        synchronized (workingTimeArr) {
                            workingTime = workingTimeArr[0];
                        }
                        //noinspection SynchronizationOnLocalVariableOrMethodParameter
                        synchronized (finishStatus) {
                            finishStatus[0] = true;
                        }

                        // Upper bound check.
                        // workingTime is less than 0 iff the child process couldn't manage to finish in time.
                        Assert.assertFalse(workingTime >= 0);
                        // Lower bound check.
                        Assert.assertFalse(workingTime < EXPECTED_TIMEOUT);
                    } catch (final Throwable ex) {
                        Assert.fail();
                    }
                }
            };

            currentThread.start();
            threads.add(currentThread);
        }

        for (final Thread thread : threads) {
            thread.join();
        }

        target.stop();
    }
}
