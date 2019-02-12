/*
   D-Bus Java Implementation
   Copyright (c) 2005-2006 Matthew Johnson
   Copyright (c) 2017-2019 David M.

   This program is free software; you can redistribute it and/or modify it
   under the terms of either the GNU Lesser General Public License Version 2 or the
   Academic Free Licence Version 2.1.

   Full licence texts are included in the LICENSE file with this program.
*/

package org.freedesktop.dbus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.freedesktop.Hexdump;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.MessageProtocolVersionException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    private MessageHandler() {
        
    }
    
    public static Message readMessage(ByteBuffer _localBuf) throws IOException, DBusException {
        byte[]      buf    = null;
        byte[]      tbuf   = null;
        byte[]      header = null;
        byte[]      body   = null;
        int[]       len    = new int[4];
            
            /* Read the 12 byte fixed header, retrying as neccessary */
            if (null == buf) {
                buf = new byte[12];
                len[0] = 0;
            }
            if (len[0] < 12) {
                _localBuf.get(buf, len[0], 12 - len[0]);
                len[0] += 12 - len[0];
            }
            if (len[0] == 0) {
                return null;
            }
            if (len[0] < 12) {
                LOGGER.debug("Only got {} of 12 bytes of header", len[0]);
                return null;
            }
    
            /* Parse the details from the header */
            byte endian = buf[0];
            byte type = buf[1];
            byte protover = buf[3];
            if (protover > Message.PROTOCOL) {
                buf = null;
                throw new MessageProtocolVersionException(String.format("Protocol version %s is unsupported", protover));
            }
    
            /* Read the length of the variable header */
            if (null == tbuf) {
                tbuf = new byte[4];
                len[1] = 0;
            }
            if (len[1] < 4) {
                _localBuf.get(tbuf, len[1], 4 - len[1]);
                len[1] += 4 - len[1];
            }
            if (len[1] < 4) {
                LOGGER.debug("Only got {} of 4 bytes of header", len[1]);
                return null;
            }
    
            /* Parse the variable header length */
            int headerlen = 0;
            headerlen = (int) Message.demarshallint(tbuf, 0, endian, 4);
            if (0 != headerlen % 8) {
                headerlen += 8 - (headerlen % 8);
            }
    
            /* Read the variable header */
            if (null == header) {
                header = new byte[headerlen + 8];
                System.arraycopy(tbuf, 0, header, 0, 4);
                len[2] = 0;
            }
            if (len[2] < headerlen) {
                _localBuf.get(header, 8 + len[2], headerlen - len[2]);
                len[2] += headerlen - len[2];
            }
            if (len[2] < headerlen) {
                LOGGER.debug("Only got {} of {} bytes of header", len[2], headerlen);
                return null;
            }
    
            /* Read the body */
            int bodylen = 0;
            if (null == body) {
                bodylen = (int) Message.demarshallint(buf, 4, endian, 4);
            }
            if (null == body) {
                body = new byte[bodylen];
                len[3] = 0;
            }
            if (len[3] < body.length) {
                _localBuf.get(body, len[3], body.length - len[3]);
    
                len[3] += body.length - len[3];
            }
            if (len[3] < body.length) {
                LOGGER.debug("Only got {} of {} bytes of body", len[3], body.length);
                return null;
            }
    
            Message m;
            try {
                m = MessageFactory.createMessage(type, buf, header, body);
            } catch (DBusException | RuntimeException _ex) {
                LOGGER.debug("", _ex);
                throw _ex;
            }
            LOGGER.debug("=> {}", m);
            return m;
        
    }
    
    public static ByteBuffer writeMessage(Message _msg) throws IOException {
        LOGGER.debug("<= {}", _msg);
        if (null == _msg) {
            return null;
        }
        if (null == _msg.getWireData()) {
            LOGGER.warn("Message {} wire-data was null!", _msg);
            return null;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.debug("Writing all {} buffers simultaneously to Unix Socket", _msg.getWireData().length );
            for (byte[] buf : _msg.getWireData()) {
                LOGGER.trace("({}):{}", buf, (null == buf ? "" : Hexdump.format(buf)));
            }
        }
        
        int bufferSize = Arrays.stream(_msg.getWireData())
                .filter(w -> w != null)
                .mapToInt(w -> w.length)
                .sum();
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.clear();
        
        for (byte[] buf : _msg.getWireData()) {
            LOGGER.trace("({}):{}", buf, (null == buf ? "" : Hexdump.format(buf)));
            if (null == buf) {
                break;
            }
            buffer.put(buf);
        }
        
        buffer.flip();
        return buffer;
   }
    
}
