package ru.linachan.yggdrasil.common.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractBlockingClient implements Runnable {

    private enum State {STOPPED, STOPPING, RUNNING}

    private static short DEFAULT_MESSAGE_SIZE = 512;

    private final AtomicReference<State> state = new AtomicReference<>(State.STOPPED);
    private final InetAddress server;
    private final int port;
    private final int byteLength;
    private final MessageLength messageLength;
    private final int defaultBufferSize;
    private final AtomicReference<OutputStream> out = new AtomicReference<>();
    private final AtomicReference<InputStream> in = new AtomicReference<>();

    public AbstractBlockingClient(InetAddress server, int port) {
        this(server, port, new TwoByteMessageLength(), DEFAULT_MESSAGE_SIZE);
    }

    public AbstractBlockingClient(InetAddress server, int port, MessageLength messageLength, int defaultBufferSize) {
        this.server = server;
        this.port = port;
        this.messageLength = messageLength;
        this.defaultBufferSize = defaultBufferSize;
        this.byteLength = messageLength.byteLength();
    }

    public int getPort() {
        return port;
    }
    
    public InetAddress getServer() {
        return server;
    }
    
    public boolean isRunning() {
        return state.get() == State.RUNNING;
    }
    
    public boolean isStopped() {
        return state.get() == State.STOPPED;
    }

    public void run() {
        if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
            connected(true);
            return;
        }

        Socket socket = null;
        try {
            socket = new Socket(server, port);
            socket.setKeepAlive(true);
            out.set(socket.getOutputStream());
            in.set(socket.getInputStream());
            int limit = 0;
            byte[] inBuffer = new byte[defaultBufferSize];
            connected(false);
            while (state.get() == State.RUNNING) {
                limit += in.get().read(inBuffer, limit, inBuffer.length - limit);
                if (limit >= byteLength) {
                    int messageLen;
                    do {
                        byte[] lengthBytes = new byte[byteLength];
                        System.arraycopy(inBuffer, 0, lengthBytes, 0, byteLength);
                        messageLen = (int) messageLength.bytesToLength(lengthBytes);
                        if (limit >= messageLen) {
                            // enough data to extract the message
                            byte[] message = new byte[messageLen];
                            System.arraycopy(inBuffer, byteLength, message, 0, messageLen);
                            messageReceived(ByteBuffer.wrap(message));
                            // compact inBuffer
                            byte[] temp = new byte[inBuffer.length];
                            System.arraycopy(inBuffer, 0, temp, messageLen + byteLength, limit - messageLen - byteLength);
                            inBuffer = temp;
                            limit = limit - messageLen - byteLength;
                        } else if (messageLen > inBuffer.length) {
                            byte[] temp = new byte[messageLen + byteLength];
                            System.arraycopy(inBuffer, 0, temp, 0, inBuffer.length);
                            inBuffer = temp;
                        }
                    } while (messageLen<limit);
                }
            }
        } catch (ClosedByInterruptException ie) {
            // do nothing
        } catch (ConnectException ce) {
            throw new RuntimeException(ce.getMessage());
        } catch (SocketException se) {
            // do nothing
        } catch (IOException ioe) {
            throw new RuntimeException("Client failure: "+ioe.getMessage());
        } finally {
            try {
                socket.close();
                state.set(State.STOPPED);
                disconnected();
            } catch (Exception e) {
                // do nothing - server failed
            }
        }
    }

    public boolean stop() {
        if (state.compareAndSet(State.RUNNING, State.STOPPING)) {
            try {
				in.get().close();
			} catch (IOException e) {
				return false;
			}
            return true;
        }
        return false;
    }

    public synchronized boolean write(byte[] buffer) {
        int len = buffer.length;
        byte[] lengthBytes = messageLength.lengthToBytes(len);
        try {
            byte[] outBuffer = new byte[len + byteLength];
            System.arraycopy(lengthBytes, 0, outBuffer, 0, byteLength);
            System.arraycopy(buffer, 0, outBuffer, byteLength, len);
            out.get().write(outBuffer);
            return true;
        } catch (Exception e) {
            // socket is closed, message not sent
            stop();
            return false;
        }
    }

    protected abstract void messageReceived(ByteBuffer message);

    protected abstract void connected(boolean alreadyConnected);

    protected abstract void disconnected();
}
