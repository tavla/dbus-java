package org.freedesktop.dbus.connections.transports;

import java.io.IOException;

import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.SASL;

import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import jnr.unixsocket.UnixSocketOptions;

/**
 * Represents a transport connected to a UnixSocket.
 * @author hypfvieh
 * @since v3.2.0 - 2019-02-08
 */
public class UnixSockTransport extends AbstractTransport {

    private final UnixSocketAddress unixSocketAddress;
    private UnixServerSocketChannel unixServerSocket;

    UnixSockTransport(BusAddress _address, int _timeout) throws IOException {
        super(_address, _timeout); 
        
        if (_address.isAbstract()) {
            unixSocketAddress = new UnixSocketAddress("\0" + _address.getAbstract());
        } else if (_address.hasPath()) {
            unixSocketAddress = new UnixSocketAddress(_address.getPath());
        } else {
            throw new IOException("Unix socket url has to specify 'path' or 'abstract'");
        }
        
        setSaslAuthMode(SASL.AUTH_EXTERNAL);
    }

    
    @SuppressWarnings("resource")
    void connect() throws IOException {
        UnixSocketChannel us;
        if (getAddress().isListeningSocket()) {
            unixServerSocket = UnixServerSocketChannel.open();

            unixServerSocket.socket().bind(unixSocketAddress);
            us = unixServerSocket.accept();
        } else {
            us = UnixSocketChannel.open(unixSocketAddress);
            while (!us.finishConnect()) { // wait until connection established
                try {
                    Thread.sleep(100L); 
                } catch (InterruptedException _ex) {
                    us.close();
                    throw new IOException("Interupted while waiting for connection", _ex);
                } 
            }
            getLogger().debug("Client connection to {} established", unixSocketAddress);
        }
        
        us.setOption(UnixSocketOptions.SO_PASSCRED, true);

        getLogger().trace("Setting timeout to {} on unix socket", getTimeout());
        
        us.configureBlocking(true);
        
        if (getTimeout() == 1) {
            us.socket().setSoTimeout(0);
        } else {
            us.socket().setSoTimeout(getTimeout());
        }
        
        us.socket().setKeepAlive(true);
        
        setChannel(us);
        
        authenticate(us.socket().getOutputStream(), us.socket().getInputStream(), us.socket());
        us.configureBlocking(false);
    }

    @Override
    public void close() throws IOException {
        getLogger().debug("Disconnecting Transport");
        
        if (unixServerSocket != null && unixServerSocket.isOpen()) {
            unixServerSocket.close();
        }
        super.close();
    }
    
}
