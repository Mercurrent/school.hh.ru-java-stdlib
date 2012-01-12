package ru.hh.school.stdlib;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public static final int ACCEPTABLE_ADDITIONAL_WAITING = 1000;
    public static final int EXPECTED_TIMEOUT = 10000;

    private void setFalseStatus(final boolean[] statusArray) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (statusArray) {
            statusArray[0] = false;
        }
        
        Assert.fail();
    }

    @Test
    public void timeoutIndependence() throws InterruptedException {
        final Server target = getNewWorkingServer();

        final boolean[] successStatus = new boolean[1];
        successStatus[0] = true;
        
        final Set<Thread> threads = new HashSet<Thread>();
        for (int i = 0; i < 5; ++i) {
            final Thread currentThread = new Thread() {
                public void run() {
                    try {
                        final Object waitingSyncObject = new Object();

                        final Socket[] connectionSocketArr = new Socket[1];
                        
                        final Long[] workingTimeArr = new Long[1];
                        workingTimeArr[0] = -1L;
                        
                        new Thread() {
                            public void run() {
                                final long startTime = System.currentTimeMillis();

                                final Socket connectionSocket;
                                try {
                                    connectionSocket = connect();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                    setFalseStatus(successStatus);
                                    throw new RuntimeException(ex);
                                }
                                synchronized (connectionSocketArr) {
                                    connectionSocketArr[0] = connectionSocket;
                                }

                                final BufferedReader input;
                                try {
                                    input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                                } catch (IOException ex) {
                                    setFalseStatus(successStatus);
                                    throw new RuntimeException(ex);
                                }
                                try {
                                    final String serverAnswer = input.readLine();
                                    
                                    if (serverAnswer != null) {
                                        // Server has answered but shouldn't.
                                        setFalseStatus(successStatus);
                                    }
                                } catch (IOException ignored) {
                                    // Going here means that socket's input stream is closed.
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
                            waitingSyncObject.wait(EXPECTED_TIMEOUT + ACCEPTABLE_ADDITIONAL_WAITING);
                        }

                        //noinspection SynchronizationOnLocalVariableOrMethodParameter
                        synchronized (connectionSocketArr) {
                            final Socket connectionSocket = connectionSocketArr[0];
                            
                            if ((connectionSocket != null) && !connectionSocket.isClosed()) {
                                connectionSocket.close();
                            }
                        }

                        final long workingTime;
                        //noinspection SynchronizationOnLocalVariableOrMethodParameter
                        synchronized (workingTimeArr) {
                            workingTime = workingTimeArr[0];
                        }

                        // Upper bound check.
                        // workingTime is less than 0 iff the child process couldn't manage to finish in time.
                        if (workingTime < 0) {
                            setFalseStatus(successStatus);
                        }
                        // Lower bound check.
                        if (workingTime < EXPECTED_TIMEOUT) {
                            setFalseStatus(successStatus);
                        }
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                        setFalseStatus(successStatus);
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
        
        Assert.assertTrue(successStatus[0]);
    }
}
