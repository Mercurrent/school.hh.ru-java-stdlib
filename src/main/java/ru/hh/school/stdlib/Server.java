package ru.hh.school.stdlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    protected final InetSocketAddress addr;

    public Server(InetSocketAddress addr) {
        this.addr = addr;
    }

    public void run() throws IOException {

        final ServerSocket serverSocket = new ServerSocket(addr.getPort(), 0, addr.getAddress());
        final Substitutor3000 substitutor = new Substitutor3000();

        //noinspection InfiniteLoopStatement
        while (true) {
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

    public int getPort() {
        return addr.getPort();
    }
}
