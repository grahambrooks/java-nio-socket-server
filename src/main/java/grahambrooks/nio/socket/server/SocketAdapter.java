package grahambrooks.nio.socket.server;

import java.nio.channels.SocketChannel;

public class SocketAdapter {
    private final SocketChannel socketChannel;

    public SocketAdapter(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
