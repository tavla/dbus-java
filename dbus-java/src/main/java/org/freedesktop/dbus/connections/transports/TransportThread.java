package org.freedesktop.dbus.connections.transports;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.freedesktop.dbus.MessageHandler;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.enxio.channels.NativeSelectorProvider;

public class TransportThread extends Thread {
    private final Logger                       logger;
    private final SelectableChannel            channel;
    private final AbstractConnection           connection;

    private final LinkedBlockingQueue<Message> outgoingQueue;
    
    TransportThread(SelectableChannel _channel, AbstractConnection _connection) {
        logger = LoggerFactory.getLogger(getClass());
        outgoingQueue = new LinkedBlockingQueue<>();
        channel = _channel;
        connection = _connection;
        setDaemon(true);
        setName("Transport Thread");
    }
    
    public synchronized void writeMessage(Message message) throws IOException {
        try {
            outgoingQueue.put(message);
        } catch (InterruptedException _ex) {
        }
    }
    
    @Override
    public void run() {
        logger.debug("Transport Thread starting");
        
        try (Selector sel = NativeSelectorProvider.getInstance().openSelector()) {
           
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

                    if (key.isAcceptable()) {
                        logger.trace("Acceptor channel, waiting for client");
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel socket = (SocketChannel) ssc.accept();
                        socket.configureBlocking(false);
                        logger.debug("New connection from {}", socket.getRemoteAddress());

                        socket.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);                            
                    }
                    
                    if (key.isReadable()) {
                        logger.trace("Received readable content on channel");
                        SocketChannel x = (SocketChannel) key.channel();
                        
                        ByteBuffer localBuf = ByteBuffer.allocate(512);
                        localBuf.clear();
                        int read = x.read(localBuf);
                        
                        if (read == -1) {
                            logger.error("Unexpected end of file");
                        } else {
                            localBuf.flip();
                            
                            List<Message> readMessages = MessageHandler.readMessages(localBuf);
                            for (Message message : readMessages) {
                                connection.handleMessage(message);
                                logger.trace("Handled incoming message with serial {}: {}",message.getSerial(), message);
                            }
                        }
                    }
                    
                    if (key.isWritable() && !outgoingQueue.isEmpty()) {
                        long queueSize = outgoingQueue.size();
                        logger.trace("Writing {} queued messages to channel", queueSize);

                        long cnt = 1;
                        while (!outgoingQueue.isEmpty()) {
                            Message msg = outgoingQueue.take();
                            logger.trace("Sending message {} of {}: {}", cnt++, queueSize, msg);
                            
                            ByteBuffer buf = MessageHandler.writeMessage(msg);
                            
                            SocketChannel x = (SocketChannel) key.channel();
                            while (buf.hasRemaining()) {
                                int writtenBytes = x.write(buf);
                                if (writtenBytes < buf.limit()) {
                                    logger.warn("Could not write complete message to channel, only {} bytes of {} bytes written", writtenBytes, buf.limit());
                                }
                            }
                        }
                    }
                }                
            }
            logger.trace("Leaving selector loop");
        }  catch (Exception _ex) {
            logger.error("Thread terminated", _ex);
        } 
    }
    
}