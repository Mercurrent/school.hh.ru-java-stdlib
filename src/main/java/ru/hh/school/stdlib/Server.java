package ru.hh.school.stdlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    protected final InetSocketAddress addr;
    protected ServerSocket serverSocket;

    private boolean shouldRun = true;

    public Server(InetSocketAddress addr) {
        this.addr = addr;
    }

    public void run() throws IOException {
        synchronized (this) {
            if (serverSocket != null) {
                serverSocket = new ServerSocket(addr.getPort(), 0, addr.getAddress());
            } else {
                System.err.println("ERROR: Duplicate running of the same server object.");
                return;
            }
        }
        final Substitutor3000 substitutor = new Substitutor3000();

        //noinspection InfiniteLoopStatement
        while (shouldRun) {
            final Socket clientSocket;

            try {
                clientSocket = serverSocket.accept();
            } catch (IOException ex) {
                System.err.println("ERROR: An I/O error occurs when waiting for a connection: " + ex.getMessage() + ".");
                continue;
            } catch (SecurityException ex) {
                System.err.println("ERROR: Security manager doesn't allow to open the connection: " + ex.getMessage() + ".");
                continue;
            }
            System.out.println("INFO: Connection with " + clientSocket.getRemoteSocketAddress() + " is established.");

            new Thread(new ConnectionHandler(clientSocket, substitutor)).start();
        }
    }

    public synchronized void stop() {
        shouldRun = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.err.println("ERROR: Unable to close the server socket.");
            }
        }
    }
}
