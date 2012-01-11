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
                System.err.println("Unable to get input or output stream of the given socket.");
                return;
            }

            String currentLine;
            try{
                currentLine = input.readLine();
            } catch (IOException ex) {
                System.err.println("Unable to read from input stream of the given socket.");
                return;
            }

            StringTokenizer parser = new StringTokenizer(currentLine);

            try{
                if (parser.hasMoreTokens()) {
                    String commandName = parser.nextToken();
                    if (commandName.equals("GET")) {
                        if (parser.hasMoreTokens()) {
                            String key = parser.nextToken();
                            String getAnswer = substitutor.get(key);
                            if (getAnswer.equals("") && parser.hasMoreTokens()) {
                                String defaultValue = parser.nextToken();
                                substitutor.put(key, defaultValue);
                            }
                            output.write("VALUE");
                            output.write(getAnswer);
                        } else {
                            System.err.println("Command GET needs a key.");
                        }
                    } else if (commandName.equals("PUT")) {
                        if (parser.hasMoreTokens()) {
                            String key = parser.nextToken();
                            if (parser.hasMoreTokens()) {
                                String value = parser.nextToken("");
                                substitutor.put(key, value);
                                output.write("OK");
                            } else {
                                System.err.println("Command PUT needs value for key " + key + ".");
                            }
                        } else {
                            System.err.println("Command PUT needs arguments.");
                        }

                    } else if (commandName.equals("SET")) {

                    } else {
                        System.err.println("Command " + commandName + " doesn't exist.");
                    }
                } else {
                    System.err.println("Empty input string.");
                }
            } catch (IOException ex) {
                System.err.println("Unable to write to output stream of socket.");
            }
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
