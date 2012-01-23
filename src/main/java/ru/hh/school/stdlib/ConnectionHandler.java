package ru.hh.school.stdlib;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHandler implements Runnable {
    public static final int COMMAND_WAITING_TIMEOUT = 10000;

    protected final Socket socket;
    protected final Substitutor3000 substitutor;

    private synchronized void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public ConnectionHandler(final Socket socket, final Substitutor3000 substitutor) {
        this.socket = socket;
        this.substitutor = substitutor;
    }

    public void run() {
        final Writer output;
        final BufferedReader input;

        try {
            try {
                output = new OutputStreamWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException ex) {
                System.err.println("ERROR: Unable to get input or output stream of the given socket.");
                return;
            }

            final AtomicBoolean canThreadRun = new AtomicBoolean(true);

            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    if (canThreadRun.getAndSet(false)) {
                        System.out.println("INFO: Waiting of the first command was timed out.");
                        closeSocket(socket);
                    }
                }
            }, COMMAND_WAITING_TIMEOUT);

            StringTokenizer parser;
            do {
                final String currentLine;
                try {
                    currentLine = input.readLine();
                } catch (IOException ex) {
                    System.err.println("ERROR: Unable to read from input stream of the given socket.");
                    return;
                }
                parser = (new StringTokenizer(currentLine));
            } while (!parser.hasMoreTokens());
            
            if (canThreadRun.getAndSet(false)) {
                timer.cancel();
            } else {
                return;
            }

            try {
                int sleepTime = substitutor.getSleepTime();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    System.err.println("ERROR: Sleeping was interrupted.");
                }

                final String commandName = parser.nextToken();
                if (commandName.equals("GET")) {
                    performGetAction(parser, output);
                } else if (commandName.equals("PUT")) {
                    performPutAction(parser, output);
                } else if (commandName.equals("SET")) {
                    performSetAction(parser, output);
                } else {
                    System.err.println("ERROR: Command " + commandName + " doesn't exist.");
                    output.write("ERROR: Command " + commandName + " doesn't exist.\n");
                }
                output.flush();
            } catch (IOException ex) {
                System.err.println("ERROR: Unable to write to the output stream of the socket.");
            }
        } finally {
            System.out.println("INFO: Connection with " + socket.getRemoteSocketAddress() + " is closing.");
            closeSocket(socket);
         }
    }

    protected void performGetAction(final StringTokenizer parser, final Writer output) throws IOException {
        System.out.println("INFO: Get action is performing.");
        
        if (parser.hasMoreTokens()) {
            final String key = parser.nextToken();
            String answer = substitutor.get(key);
            if ("".equals(answer) && parser.hasMoreTokens()) {
                answer = parser.nextToken();
            }
            output.write("VALUE\n");
            output.write(answer);
            output.write("\n");
        } else {
            System.err.println("ERROR: Command GET needs a key.");
            output.write("ERROR: Command GET needs a key.\n");
        }
    }

    protected void performPutAction(final StringTokenizer parser, final Writer output) throws IOException {
        System.out.println("INFO: Put action is performing.");

        if (parser.hasMoreTokens()) {
            final String key = parser.nextToken();
            if (parser.hasMoreTokens()) {
                final String value = parser.nextToken("").trim();
                substitutor.put(key, value);
                output.write("OK\n");
            } else {
                System.err.println("ERROR: Command PUT needs value for key " + key + ".");
                output.write("ERROR: Command PUT needs value for key " + key + ".\n");
            }
        } else {
            System.err.println("ERROR: Command PUT needs arguments.");
            output.write("ERROR: Command PUT needs arguments.\n");
        }
    }

    protected void performSetAction(final StringTokenizer parser, final Writer output) throws IOException {
        System.out.println("INFO: Set action is performing.");

        if (parser.hasMoreTokens()) {
            final String sleep = parser.nextToken();
            if ("SLEEP".equals(sleep)) {
                if (parser.hasMoreTokens()) {
                    final String timeSleepString = parser.nextToken();
                    try {
                        int timeSleep = Integer.parseInt(timeSleepString);
                        if (timeSleep < 0) {
                            System.err.println("ERROR: Command SET SLEEP requires a positive integer value.");
                            output.write("ERROR: Command SET SLEEP requires a positive integer value.\n");
                        } else {
                            substitutor.setSleepTime(timeSleep);
                            output.write("OK\n");
                        }
                    } catch (NumberFormatException ex) {
                        System.err.println("ERROR: Command SET SLEEP requires an integer value.");
                        output.write("ERROR: Command SET SLEEP requires an integer value.\n");
                    }

                } else {
                    System.err.println("ERROR: Command SET SLEEP requires value.");
                    output.write("ERROR: Command SET SLEEP requires value.\n");
                }
            } else {
                System.err.println("ERROR: Command SET " + sleep + " doesn't exist.");
                output.write("ERROR: Command SET " + sleep + " doesn't exist.\n");
            }
        } else {
            System.err.println("ERROR: Command SET requires arguments.");
            output.write("ERROR: Command SET requires arguments.\n");
        }
    }
}
