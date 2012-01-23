package ru.hh.school.stdlib;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimeoutTest extends BaseFunctionalTest {
    public static final int ACCEPTABLE_ADDITIONAL_WAITING = 1000;
    public static final int EXPECTED_TIMEOUT = 10000;

    private void setFalseStatus(final AtomicBoolean status) {
        status.set(false);
        Assert.fail();
    }

    private synchronized void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    public void timeoutIndependence() throws InterruptedException {
        final Server target = getNewWorkingServer();

        final AtomicBoolean successStatus = new AtomicBoolean(true);


        final Set<Thread> threads = new HashSet<Thread>();
        for (int i = 0; i < 5; ++i) {
            final Thread currentThread = new Thread() {
                public void run() {
                    try {
                        final long startTime = System.currentTimeMillis();

                        final Socket connectionSocket;

                        try {
                            connectionSocket = connect();
                        } catch (IOException ex) {
                            setFalseStatus(successStatus);
                            throw new RuntimeException(ex);
                        }

                        final AtomicBoolean canThreadRun = new AtomicBoolean(true);
                        final Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                if (canThreadRun.getAndSet(false)) {
                                    closeSocket(connectionSocket);
                                }
                            }
                        }, EXPECTED_TIMEOUT + ACCEPTABLE_ADDITIONAL_WAITING);

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

                        final long workingTime = System.currentTimeMillis() - startTime;
                        
                        if (canThreadRun.getAndSet(false)) {
                            timer.cancel();
                            closeSocket(connectionSocket);
                        } else {
                            // Upper bound check.
                            // Process couldn't manage to finish in time.
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
        
        Assert.assertTrue(successStatus.get());
    }
}
