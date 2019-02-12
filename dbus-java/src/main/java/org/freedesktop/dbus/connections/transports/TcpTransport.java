package org.freedesktop.dbus.connections.transports;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.SASL;

public class TcpTransport extends AbstractTransport {

    private Socket socket;
    
    TcpTransport(BusAddress _address, int _timeout) {
        super(_address, _timeout);
        setSaslAuthMode(SASL.AUTH_SHA);
    }

    @Override
    SelectableChannel connectImpl() throws IOException {
        SocketChannel socketChannel;
        if (getAddress().isListeningSocket()) {
            ServerSocketChannel ss = ServerSocketChannel.open();
            ss.bind(new InetSocketAddress(getAddress().getHost(), getAddress().getPort()));
            socketChannel = ss.accept();
        } else {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(getAddress().getHost(), getAddress().getPort()));
            
        }
        socketChannel.configureBlocking(true);

        getLogger().trace("Setting timeout to {} on Socket", getTimeout());
        socket.setSoTimeout(getTimeout());
        
        authenticate(socket.getOutputStream(), socket.getInputStream(), socket);
        
        socketChannel.configureBlocking(false);
        return socketChannel;
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
