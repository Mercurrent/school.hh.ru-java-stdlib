package ru.hh.school.stdlib;

import java.io.*;
import java.net.Socket;

/**
 * Created by IntelliJ IDEA.
 * User: Tiana
 * Date: 09.01.12
 * Time: 0:08
 */
public class ConnectionHandler implements Runnable {
    protected Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        final Writer output;
        final Reader input;

        try{
            output = new OutputStreamWriter(socket.getOutputStream());
            input = new InputStreamReader(socket.getInputStream());
        } catch (IOException ex) {
            System.err.println("Unable to get input or output stream of the given socket.");
            return;
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        // todo to implement the handling.
    }
}
