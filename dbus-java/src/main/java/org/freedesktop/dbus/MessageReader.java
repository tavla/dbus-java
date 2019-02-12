package org.freedesktop.dbus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.MessageProtocolVersionException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private byte[]       remainingBytesFromLastRead;
    private int          readBytes;

    public List<Message> readMessages(ByteBuffer _localBuf) throws IOException, DBusException {
        List<Message> messages = new ArrayList<>();
        readBytes = 0;

        int[] len = new int[4];

        Message readMessage = readMessage(_localBuf, len);
        if (readMessage != null) {
            messages.add(readMessage);
        }

        len = new int[4];

        while (readBytes < _localBuf.limit()) {
            ByteBuffer allocate = ByteBuffer.allocate(_localBuf.limit() - readBytes);
            allocate.put(_localBuf.array(), readBytes, allocate.capacity());

            allocate.flip();

            Message msg = readMessage(allocate, len);
            if (msg != null) {
                messages.add(msg);
            }
            len = new int[4];
        }

        logger.debug("Received {} messages", messages.size());
        return messages;
    }

    Message readMessage(ByteBuffer _localBuf, int[] _len) throws IOException, DBusException {
        ByteBuffer allocate;
        if (remainingBytesFromLastRead != null) {
            allocate = ByteBuffer.allocate(_localBuf.limit() + remainingBytesFromLastRead.length);
            allocate.put(remainingBytesFromLastRead);
            allocate.put(_localBuf);
            allocate.flip();
            remainingBytesFromLastRead = null;
        } else {
            allocate = _localBuf;
        }

        byte[] fixedHeader = null;
        byte[] tbuf = null;
        byte[] header = null;
        byte[] body = null;

        /* Read the 12 byte fixed header, retrying as neccessary */
        if (null == fixedHeader) {
            fixedHeader = new byte[12];
            _len[0] = 0;
        }
        if (_len[0] < 12) {
            allocate.get(fixedHeader, _len[0], 12 - _len[0]);
            _len[0] += 12 - _len[0];
        }
        if (_len[0] == 0) {
            return null;
        }
        if (_len[0] < 12) {
            logger.debug("Only got {} of 12 bytes of header", _len[0]);
            return null;
        }

        /* Parse the details from the header */
        byte endian = fixedHeader[0];
        byte type = fixedHeader[1];
        byte protover = fixedHeader[3];
        if (protover > Message.PROTOCOL) {
            fixedHeader = null;
            throw new MessageProtocolVersionException(String.format("Protocol version %s is unsupported", protover));
        }

        /* Read the length of the variable header */
        if (null == tbuf) {
            tbuf = new byte[4];
            _len[1] = 0;
        }
        if (_len[1] < 4) {
            allocate.get(tbuf, _len[1], 4 - _len[1]);
            _len[1] += 4 - _len[1];
        }
        if (_len[1] < 4) {
            logger.debug("Only got {} of 4 bytes of header", _len[1]);
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
            _len[2] = 0;
        }
        if (_len[2] < headerlen) {
            allocate.get(header, 8 + _len[2], headerlen - _len[2]);
            _len[2] += headerlen - _len[2];
        }
        if (_len[2] < headerlen) {
            logger.debug("Only got {} of {} bytes of header", _len[2], headerlen);
            return null;
        }

        /* Read the body */
        int bodylen = 0;
        if (null == body) {
            bodylen = (int) Message.demarshallint(fixedHeader, 4, endian, 4);
        }
        if (null == body) {
            body = new byte[bodylen];
            _len[3] = 0;
        }
        if (_len[3] < body.length) {
            if (allocate.remaining() < body.length) {
                logger.debug(
                        "Current buffer does not contain full messages, storing remaining and waiting for next read");
                remainingBytesFromLastRead = new byte[allocate.capacity()];
                // store the complete buffer, otherwise header information may be missing in second pass
                allocate.position(0);
                allocate.get(remainingBytesFromLastRead, 0, allocate.capacity());
                readBytes += remainingBytesFromLastRead.length;
                return null;

            }
            allocate.get(body, _len[3], body.length - _len[3]);

            _len[3] += body.length - _len[3];
        }
        if (_len[3] < body.length) {
            logger.debug("Only got {} of {} bytes of body", _len[3], body.length);
            return null;
        }

        Message m;
        try {
            m = MessageFactory.createMessage(type, fixedHeader, header, body);
        } catch (DBusException | RuntimeException _ex) {
            logger.debug("", _ex);
            throw _ex;
        }
        logger.debug("=> {}", m);
        readBytes += Arrays.stream(_len).sum();
        return m;

    }
}
