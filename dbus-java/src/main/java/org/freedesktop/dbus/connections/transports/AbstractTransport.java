/*
   D-Bus Java Implementation
   Copyright (c) 2005-2006 Matthew Johnson
   Copyright (c) 2017-2019 David M.

   This program is free software; you can redistribute it and/or modify it
   under the terms of either the GNU Lesser General Public License Version 2 or the
   Academic Free Licence Version 2.1.

   Full licence texts are included in the LICENSE file with this program.
*/

package org.freedesktop.dbus.connections.transports;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.freedesktop.Hexdump;
import org.freedesktop.dbus.MessageHandler;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.SASL;
import org.freedesktop.dbus.connections.SASL.SaslMode;
import org.freedesktop.dbus.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.enxio.channels.NativeSelectorProvider;

public abstract class AbstractTransport implements Closeable {
    private final Logger      logger       = LoggerFactory.getLogger(getClass());

    private MessageHandler     msgHandler;

    private final BusAddress  address;
    private final int         timeout;

    private SaslMode          mode         = SaslMode.CLIENT;
    private int               saslAuthMode = SASL.AUTH_NONE;
    private SelectableChannel channel;

    private Thread            readerThread;
    
    AbstractTransport(BusAddress _address, int _timeout) {
        address = _address;
        timeout = _timeout;

        if (address.isListeningSocket()) {
            mode = SASL.SaslMode.SERVER;
        } else {
            mode = SASL.SaslMode.CLIENT;
        }
        msgHandler = new MessageHandler();
    }

    public static String genGUID() {
        Random r = new Random();
        byte[] buf = new byte[16];
        r.nextBytes(buf);
        String guid = Hexdump.toHex(buf);
        return guid.replaceAll(" ", "");
    }

    public void writeMessage(Message message) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.clear();
        
        msgHandler.writeMessage(message, buf);
        buf.flip();
        
        while (buf.hasRemaining()) {
            ((SocketChannel) channel).write(buf);
        }
    }

    abstract void connect() throws IOException;
    
    protected void authenticate(OutputStream _out, InputStream _in, Socket _sock) throws IOException {
        if (!(new SASL()).auth(mode, saslAuthMode, address.getGuid(), _out, _in, _sock)) {
            _out.close();
            throw new IOException("Failed to auth");
        }
    }

    public void start(AbstractConnection _connection) throws IOException {
        connect();
        readerThread = new Thread(() -> {
            try {
                Selector sel = NativeSelectorProvider.getInstance().openSelector();
                SelectionKey acceptKey;
                if (channel instanceof ServerSocketChannel) { 
                    acceptKey = channel.register(sel, SelectionKey.OP_ACCEPT);
                } else {
                    acceptKey = channel.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);                    
                }

                while (acceptKey.selector().select() > 0) {
                    Set<SelectionKey> readyKeys = sel.selectedKeys();
                    Iterator<SelectionKey> it = readyKeys.iterator();

                    while (it.hasNext()) {
                        SelectionKey key = (SelectionKey) it.next();
                        it.remove();

                        if (key.isValid() && key.isAcceptable()) {
                            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                            SocketChannel socket = (SocketChannel) ssc.accept();
                            socket.configureBlocking(false);
                            logger.debug("New connection from {}", socket.getRemoteAddress());

                            socket.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);                            
                        }
                        
                        if (key.isValid() && key.isReadable()) {
                            SocketChannel x = (SocketChannel) key.channel();
                            MessageHandler mr = new MessageHandler();
                            
                            ByteBuffer localBuf = ByteBuffer.allocate(2048);
                            int read = x.read(localBuf);
                            
                            if (read == -1) {
                                logger.error("Unexpected end of file");
                            } else {
                                localBuf.flip();
                                Message readMessage = mr.readMessage(localBuf);
                                _connection.handleMessage(readMessage);
                            }
                        }
                    }                
                }
            }  catch (Exception _ex) {
                getLogger().error("Thread terminated", _ex);
            }
        }, "Transport Reader Thread");
        
        readerThread.setDaemon(true);
        readerThread.start();
    }

   
    
    protected SelectableChannel getChannel() {
        return channel;
    }

    protected void setChannel(SelectableChannel _channel) {
        channel = _channel;
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

    public boolean isConnected() {
        return channel.isOpen();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (readerThread != null) {
            readerThread.interrupt();
        }
    }
}
