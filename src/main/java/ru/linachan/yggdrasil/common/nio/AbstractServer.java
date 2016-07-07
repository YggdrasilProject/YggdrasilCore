package ru.linachan.yggdrasil.common.nio;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractServer implements Runnable {

    private enum State {STOPPED, STOPPING, RUNNING}

    private static short DEFAULT_MESSAGE_SIZE = 512;

    private final AtomicReference<State> state = new AtomicReference<>(State.STOPPED);
    private final int port;
    private final MessageLength messageLength;
    private final Map<SelectionKey, ByteBuffer> readBuffers = new HashMap<>();
    private final int defaultBufferSize;
    
    protected AbstractServer(int port) {
        this(port, new TwoByteMessageLength(), DEFAULT_MESSAGE_SIZE);
    }
    
    protected AbstractServer(int port, MessageLength messageLength, int defaultBufferSize) {
        this.port = port;
        this.messageLength = messageLength;
        this.defaultBufferSize = defaultBufferSize;
    }

    public int getPort() {
        return port;
    }
    
    public InetAddress getServer() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    public boolean isRunning() {
        return state.get() == State.RUNNING;
    }
    
    public boolean isStopped() {
        return state.get() == State.STOPPED;
    }

    public void run() {
        // ensure that the server is not started twice
        if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
            started(true);
            return;
        }

        Selector selector = null;
        ServerSocketChannel server = null;
        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            started(false);
            while (state.get() == State.RUNNING) {
                selector.select(100); // check every 100ms whether the server has been requested to stop
                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    SelectionKey key = i.next();
                    try {
                        i.remove();
                        if (key.isConnectable()) {
                            ((SocketChannel) key.channel()).finishConnect();
                        }
                        if (key.isAcceptable()) {
                            // accept connection
                            SocketChannel client = server.accept();
                            client.configureBlocking(false);
                            client.socket().setTcpNoDelay(true);
                            connection(client.register(selector, SelectionKey.OP_READ));
                        }
                        if (key.isReadable()) {
                            for (ByteBuffer message: readIncomingMessage(key)) {
                                messageReceived(message, key);
                            }
                        }
                    } catch (IOException ioe) {
                        resetKey(key);
                        disconnected(key);
                    }
                }
            }
        } catch (Throwable e) {
            LoggerFactory.getLogger(AbstractServer.class).error("Server failure: {}", e);
            throw new RuntimeException(e);
        } finally {
            try {
                selector.close();
                server.socket().close();
                server.close();
                state.set(State.STOPPED);
                stopped();
            } catch (Exception e) {
                // do nothing - server failed
            }
        }
    } 
    
    public boolean stop() {
        return state.compareAndSet(State.RUNNING, State.STOPPING);
    }
    
    protected void resetKey(SelectionKey key) {
        key.cancel();
        readBuffers.remove(key);
    }
   
    public void write(SelectionKey channelKey, byte[] buffer)  {
        if (buffer != null && state.get() == State.RUNNING) {
			short len = (short) buffer.length;
			byte[] lengthBytes = messageLength.lengthToBytes(len);

			// copying into byte buffer is actually faster than writing to channel twice over many (>10000) runs
			ByteBuffer writeBuffer = ByteBuffer.allocate(len + lengthBytes.length);
			writeBuffer.put(lengthBytes);
			writeBuffer.put(buffer);
			writeBuffer.flip();

            int bytesWritten;
            try {
				// only 1 thread can write to a channel at a time
                SocketChannel channel = (SocketChannel) channelKey.channel();
                synchronized (channel) { 
                    bytesWritten = channel.write(writeBuffer);
                }

                if (bytesWritten==-1) {
                    resetKey(channelKey);
                    disconnected(channelKey);
                }
            } catch (Exception e) {
                resetKey(channelKey);
                disconnected(channelKey);
            }
        }
    }

    private List<ByteBuffer> readIncomingMessage(SelectionKey key) throws IOException {
        ByteBuffer readBuffer = readBuffers.get(key);
        if (readBuffer==null) {
            readBuffer = ByteBuffer.allocate(defaultBufferSize);
            readBuffers.put(key, readBuffer);
        }
        if (((ReadableByteChannel) key.channel()).read(readBuffer) == -1) {
            throw new IOException("Read on closed key");
        }

        readBuffer.flip();
        List<ByteBuffer> result = new ArrayList<>();

        ByteBuffer msg = readMessage(key, readBuffer);
        while (msg != null) {
            result.add(msg);
            msg = readMessage(key, readBuffer);
        }

         return result;
    }

    private ByteBuffer readMessage(SelectionKey key, ByteBuffer readBuffer) {
        int bytesToRead;
        if (readBuffer.remaining() > messageLength.byteLength()) { // must have at least enough bytes to read the size of the message
            byte[] lengthBytes = new byte[messageLength.byteLength()];
            readBuffer.get(lengthBytes);
            bytesToRead = (int) messageLength.bytesToLength(lengthBytes);
            if ((readBuffer.limit() - readBuffer.position()) < bytesToRead) {
                // Not enough data - prepare for writing again
                if (readBuffer.limit() == readBuffer.capacity()) {
                    // message may be longer than buffer => resize buffer to message size
                    int oldCapacity = readBuffer.capacity();
                    ByteBuffer tmp = ByteBuffer.allocate(bytesToRead + messageLength.byteLength());
                    readBuffer.position(0);
                    tmp.put(readBuffer);
                    readBuffer = tmp;
                    readBuffer.position(oldCapacity);
                    readBuffer.limit(readBuffer.capacity());
                    readBuffers.put(key, readBuffer);
                    return null;
                } else {
                    // rest for writing
                    readBuffer.position(readBuffer.limit());
                    readBuffer.limit(readBuffer.capacity());
                    return null;
                }
            }
        } else {
            // Not enough data - prepare for writing again
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
            return null;
        }
        byte[] resultMessage = new byte[bytesToRead];
        readBuffer.get(resultMessage, 0, bytesToRead);
        // remove read message from buffer
        int remaining = readBuffer.remaining();
        readBuffer.limit(readBuffer.capacity());
        readBuffer.compact();
        readBuffer.position(0);
        readBuffer.limit(remaining);
        return ByteBuffer.wrap(resultMessage);
    } 
    
    protected abstract void messageReceived(ByteBuffer message, SelectionKey key);
       
    protected abstract void connection(SelectionKey key);

    protected abstract void disconnected(SelectionKey key);
    
    protected abstract void started(boolean alreadyStarted);
    
    protected abstract void stopped();
}
