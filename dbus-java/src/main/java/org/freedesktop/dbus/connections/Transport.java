/*
   D-Bus Java Implementation
   Copyright (c) 2005-2006 Matthew Johnson
   Copyright (c) 2017-2019 David M.

   This program is free software; you can redistribute it and/or modify it
   under the terms of either the GNU Lesser General Public License Version 2 or the
   Academic Free Licence Version 2.1.

   Full licence texts are included in the LICENSE file with this program.
*/

package org.freedesktop.dbus.connections;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channels;
import java.util.Random;

import org.freedesktop.Hexdump;
import org.freedesktop.dbus.MessageReader;
import org.freedesktop.dbus.MessageWriter;
import org.freedesktop.dbus.connections.BusAddress.AddressBusTypes;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import jnr.unixsocket.UnixSocketOptions;

public class Transport implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MessageReader min;
    private MessageWriter mout;

    private UnixServerSocketChannel unixServerSocket;

    public Transport() {
    }

    public static String genGUID() {
        Random r = new Random();
        byte[] buf = new byte[16];
        r.nextBytes(buf);
        String guid = Hexdump.toHex(buf);
        return guid.replaceAll(" ", "");
    }

    public Transport(BusAddress address) throws IOException {
        connect(address);
    }

    public Transport(String address) throws IOException, DBusException {
        connect(new BusAddress(address));
    }

    public Transport(String address, int timeout) throws IOException, DBusException {
        connect(new BusAddress(address), timeout);
    }

    public Transport(BusAddress address, int timeout) throws IOException, DBusException {
        connect(address, timeout);
    }

    public void writeMessage(Message message) throws IOException {
        if (mout != null) {
            mout.writeMessage(message);
        }
    }

    public Message readMessage() throws IOException, DBusException {
        if (min != null) {
            try {
                return min.readMessage();
            } catch (Exception _ex) {
                if (_ex instanceof EOFException) { return null; }
                logger.warn("Error while waiting for message: ", _ex);
            }
        }
        return null;
    }

    private void connect(BusAddress address) throws IOException {
        connect(address, 0);
    }

    private void connect(BusAddress address, int timeout) throws IOException {
        logger.debug("Connecting to {}", address);
        OutputStream out = null;
        InputStream in = null;
        UnixSocketChannel us = null;
        SASL.SaslMode mode = SASL.SaslMode.CLIENT;
        int types = 0;

        if (address.getBusType() == AddressBusTypes.UNIX) {
            types = SASL.AUTH_EXTERNAL;
            
            UnixSocketAddress unixSocketAddress;
            
            if (null != address.getParameter("abstract")) {
                unixSocketAddress = new UnixSocketAddress("\0" + address.getParameter("abstract"));
            } else if (null != address.getParameter("path")) {
                unixSocketAddress = new UnixSocketAddress(address.getParameter("path"));
            } else {
                throw new IOException("Unix socket url has to specify 'path' or 'abstract'");
            }
            
            if (null != address.getParameter("listen")) {
                mode = SASL.SaslMode.SERVER;
                unixServerSocket = UnixServerSocketChannel.open();

                unixServerSocket.socket().bind(unixSocketAddress);
                us = unixServerSocket.accept();
            } else {
                mode = SASL.SaslMode.CLIENT;
                us = UnixSocketChannel.open(unixSocketAddress);
            }
            
            us.setOption(UnixSocketOptions.SO_PASSCRED, true);

            in = Channels.newInputStream(us);
            out = Channels.newOutputStream(us);
        } else if (address.getBusType() == AddressBusTypes.TCP) {
            types = SASL.AUTH_SHA;
            Socket s;
            if (null != address.getParameter("listen")) {
                mode = SASL.SaslMode.SERVER;
                try (ServerSocket ss = new ServerSocket()) {
                    ss.bind(new InetSocketAddress(address.getParameter("host"), Integer.parseInt(address.getParameter("port"))));
                    s = ss.accept();
                }
            } else {
                mode = SASL.SaslMode.CLIENT;
                s = new Socket();
                s.connect(new InetSocketAddress(address.getParameter("host"), Integer.parseInt(address.getParameter("port"))));
            }
            in = s.getInputStream();
            out = s.getOutputStream();
            
            logger.trace("Setting timeout to {} on Socket", timeout);
            s.setSoTimeout(timeout);

        } else {
            throw new IOException("unknown address type " + address.getType());
        }

        if (!(new SASL()).auth(mode, types, address.getParameter("guid"), out, in, us.socket())) {
            out.close();
            throw new IOException("Failed to auth");
        }
        if (null != us) {
            logger.trace("Setting timeout to {} on Socket", timeout);
            if (timeout == 1) {
                us.configureBlocking(false);
            } else {
                us.socket().setSoTimeout(timeout);
            }
        }
        mout = new MessageWriter(out, address.getBusType() == AddressBusTypes.UNIX);
        min = new MessageReader(in);
    }

    public synchronized void disconnect() throws IOException {
        logger.debug("Disconnecting Transport");
        min.close();
        mout.close();
        if (unixServerSocket != null && unixServerSocket.isOpen()) {
            unixServerSocket.close();
        }
    }

    public boolean isConnected() {
        return min.isClosed() || mout.isClosed();
    }

    @Override
    public void close() throws IOException {
        disconnect();
    }

}
