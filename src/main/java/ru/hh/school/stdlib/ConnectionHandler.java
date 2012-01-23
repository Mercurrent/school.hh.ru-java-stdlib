package ru.hh.school.stdlib;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionHandler implements Runnable {
    public static final int COMMAND_WAITING_TIMEOUT = 10000;

    protected final Socket socket;
    protected final Substitutor3000 substitutor;

    private final static String patternForGetCommand = "(GET)\\W+([^\\W]+)(?:\\W+(.+))?";
    private final static String patternForSetCommand = "(PUT)\\W+([^\\W]+)\\W+(.+)";
    private final static String patternForSetSleepCommand = "(SET)\\W+SLEEP\\W+([1-9]\\d{0,7}+)";
    
    private final static Pattern patternForAllCommands = Pattern.compile(
            "(?:" + patternForGetCommand + ")|(?:" + patternForSetCommand + ")|(?:" + patternForSetSleepCommand + ")");

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

            String currentLine;
            do {
                try {
                    currentLine = input.readLine();
                } catch (IOException ex) {
                    System.err.println("ERROR: Unable to read from input stream of the given socket.");
                    return;
                }
            } while (!currentLine.isEmpty());
            
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

                final Matcher matcher = patternForAllCommands.matcher(currentLine);
                if (matcher.matches()) {
                    final String commandId = matcher.group(1);

                    if ("GET".equalsIgnoreCase(commandId)) {
                        final String key = matcher.group(2);
                        final String defaultValue = (matcher.groupCount() >= 3) ? matcher.group(3) : null;

                        performGetAction(key, defaultValue, output);
                    } else if ("PUT".equalsIgnoreCase(commandId)) {
                        final String key = matcher.group(2);
                        final String value = matcher.group(3);
                        
                        performPutAction(key, value, output);
                    } else {
                        final int time = Integer.parseInt(matcher.group(2));
                        
                        performSetAction(time, output);
                    }
                } else {
                    System.err.println("ERROR: Command \"" + currentLine + "\" was unrecognized.");
                    output.write("ERROR: Command \"" + currentLine + "\" was unrecognized.");
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

    protected void performGetAction(final String key, final String defaultValue, final Writer output) throws IOException {
        System.out.println("INFO: Get action is performing.");
        
        String answer = substitutor.get(key);
        if ("".equals(answer) && defaultValue != null) {
            answer = defaultValue;            
        }
        output.write("VALUE\n");
        output.write(answer);
        output.write("\n");
    }

    protected void performPutAction(final String key, final String value, final Writer output) throws IOException {
        System.out.println("INFO: Put action is performing.");

        substitutor.put(key, value);
        output.write("OK\n");
    }

    protected void performSetAction(final int timeSleep, final Writer output) throws IOException {
        System.out.println("INFO: Set action is performing.");

        substitutor.setSleepTime(timeSleep);
        output.write("OK\n");
    }
}
