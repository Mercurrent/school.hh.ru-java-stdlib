package ru.hh.school.stdlib;

import org.junit.Assert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BaseFunctionalTest {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9129;
    
    protected Server getNewWorkingServer() {
        final Server server = new Server(new InetSocketAddress(HOST, PORT));
        new Thread() {
            public void run() {
                try {
                    server.run();
                } catch (IOException e) {
                    Assert.fail();
                }
            }
        }.start();

        return server;
    }
  
    protected Socket connect() throws IOException {
        return new Socket(HOST, PORT);
    }
}
