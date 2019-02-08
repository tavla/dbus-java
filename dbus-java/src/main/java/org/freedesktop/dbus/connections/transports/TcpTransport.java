package org.freedesktop.dbus.connections.transports;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.SASL;

public class TcpTransport extends AbstractTransport {

    private Socket socket;
    
    TcpTransport(BusAddress _address, int _timeout) {
        super(_address, _timeout);
        setSaslAuthMode(SASL.AUTH_SHA);
    }

    void connect() throws IOException {
        
        if (getAddress().isListeningSocket()) {
            try (ServerSocket ss = new ServerSocket()) {
                ss.bind(new InetSocketAddress(getAddress().getHost(), getAddress().getPort()));
                socket = ss.accept();
            }
        } else {
            socket = new Socket();
            socket.connect(new InetSocketAddress(getAddress().getHost(), getAddress().getPort()));
        }

        getLogger().trace("Setting timeout to {} on Socket", getTimeout());
        socket.setSoTimeout(getTimeout());

        authenticate(socket.getOutputStream(), socket.getInputStream(), socket);
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
