package ru.hh.school.stdlib;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Tiana
 * Date: 09.01.12
 * Time: 0:08
 */
public class ConnectionHandler implements Runnable {
    protected final Socket socket;
    protected final Substitutor3000 substitutor;

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

            String currentLine;
            try{
                currentLine = input.readLine();
            } catch (IOException ex) {
                System.err.println("ERROR: Unable to read from input stream of the given socket.");
                return;
            }

            StringTokenizer parser = new StringTokenizer(currentLine);

            try{
                if (parser.hasMoreTokens()) {
                    String commandName = parser.nextToken();
                    if (commandName.equals("GET")) {
                        performGetAction(parser, output);
                    } else if (commandName.equals("PUT")) {
                        performPutAction(parser, output);
                    } else if (commandName.equals("SET")) {
                        performSetAction(parser, output);
                    } else {
                        System.err.println("ERROR: Command " + commandName + " doesn't exist.");
                    }
                    output.flush();
                } else {
                    System.err.println("ERROR: Empty input string.");
                }
            } catch (IOException ex) {
                System.err.println("ERROR: Unable to write to the output stream of the socket.");
            }
        } finally {
            try {
                System.out.println("INFO: Connection with " + socket.getRemoteSocketAddress() + " is closing.");

                socket.close();
            } catch (IOException ignored) {
            }
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
            output.write("VALUE");
            output.write(answer);
        } else {
            System.err.println("ERROR: Command GET needs a key.");
        }
    }

    protected void performPutAction(final StringTokenizer parser, final Writer output) throws IOException {
        System.out.println("INFO: Put action is performing.");

        if (parser.hasMoreTokens()) {
            final String key = parser.nextToken();
            if (parser.hasMoreTokens()) {
                final String value = parser.nextToken("");
                substitutor.put(key, value);
                output.write("OK");
            } else {
                System.err.println("ERROR: Command PUT needs value for key " + key + ".");
            }
        } else {
            System.err.println("ERROR: Command PUT needs arguments.");
        }
    }

    protected void performSetAction(final StringTokenizer parser, final Writer output) throws IOException {
        System.out.println("INFO: Set action is performing.");

        // todo to implement.
    }
}
