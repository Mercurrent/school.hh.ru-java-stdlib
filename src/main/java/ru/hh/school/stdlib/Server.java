package ru.hh.school.stdlib;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    protected final InetSocketAddress addr;

    public Server(InetSocketAddress addr) {
        this.addr = addr;
    }

    public void run() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int getPort() {
        return addr.getPort();
    }
}
