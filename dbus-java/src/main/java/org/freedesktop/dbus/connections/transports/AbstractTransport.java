package org.freedesktop.dbus.connections.transports;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.util.Random;

import org.freedesktop.Hexdump;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.SASL;
import org.freedesktop.dbus.connections.SASL.SaslMode;
import org.freedesktop.dbus.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransport implements Closeable {
    private final Logger      logger       = LoggerFactory.getLogger(getClass());

    private final BusAddress  address;
    private final int         timeout;

    private SaslMode          mode         = SaslMode.CLIENT;
    private int               saslAuthMode = SASL.AUTH_NONE;

    private TransportThread   transportThread;
    
    AbstractTransport(BusAddress _address, int _timeout) {
        address = _address;
        timeout = _timeout;

        if (address.isListeningSocket()) {
            mode = SASL.SaslMode.SERVER;
        } else {
            mode = SASL.SaslMode.CLIENT;
        }
    }

    public static String genGUID() {
        Random r = new Random();
        byte[] buf = new byte[16];
        r.nextBytes(buf);
        String guid = Hexdump.toHex(buf);
        return guid.replaceAll(" ", "");
    }

   

    abstract SelectableChannel connect() throws IOException;
    
    protected void authenticate(OutputStream _out, InputStream _in, Socket _sock) throws IOException {
        if (!(new SASL()).auth(mode, saslAuthMode, address.getGuid(), _out, _in, _sock)) {
            _out.close();
            throw new IOException("Failed to auth");
        }
    }

    public void start(AbstractConnection _connection) throws IOException {
        SelectableChannel channel = connect();
        transportThread = new TransportThread(channel, _connection);
        
        transportThread.start();
    }
    
    protected int getSaslAuthMode() {
        return saslAuthMode;
    }

    protected void setSaslAuthMode(int _saslAuthMode) {
        saslAuthMode = _saslAuthMode;
    }

    protected SaslMode getMode() {
        return mode;
    }

    protected BusAddress getAddress() {
        return address;
    }

    protected int getTimeout() {
        return timeout;
    }

    public synchronized void disconnect() throws IOException {
        close();
    }
   
    protected Logger getLogger() {
        return logger;
    }

    public void writeMessage(Message _m) throws IOException {
        transportThread.writeMessage(_m);
    }

    
    @Override
    public void close() throws IOException {
        if (transportThread != null) {
            transportThread.interrupt();
        }
    }
    
}
